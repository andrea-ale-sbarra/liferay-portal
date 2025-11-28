# Copyright 2025 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Customer Service Agent for Liferay Commerce order inquiries."""

import json
from typing import Dict, List, Any, Optional
from google.adk.agents import LlmAgent
import sys
import os

# Add the parent directory to the path to import liferay_client
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '../..'))
from liferay_client import LiferayAPIClient, Order, Customer

class CustomerServiceAgent:
    """Customer service agent for handling order inquiries"""
    
    def __init__(self):
        self.name = "LiferayCustomerServiceAgent"
        self.description = "Customer service agent for Liferay Commerce order inquiries and support"
        self.liferay_client = LiferayAPIClient()
        # Auto-discover available channels and accounts
        self._discover_available_resources()
    
    def _discover_available_resources(self):
        """Discover available channels and accounts automatically"""
        try:
            print("🔍 Auto-discovering available channels and accounts...")
            result = self.liferay_client.auto_discover_and_test()
            
            if result['success']:
                self.channel_id = result['test_channel']['id']
                self.account_id = result['test_account']['id']
                self.available_channels = result['channels']
                self.available_accounts = result['accounts']
                
                print(f"✅ Discovered Channel: {result['test_channel']['name']} (ID: {self.channel_id})")
                print(f"✅ Discovered Account: {result['test_account']['name']} (ID: {self.account_id})")
                print(f"✅ Total channels available: {len(self.available_channels)}")
                print(f"✅ Total accounts available: {len(self.available_accounts)}")
            else:
                print(f"⚠️  Auto-discovery failed: {result['message']}")
                # Fallback to manual discovery
                self._manual_discovery_fallback()
                
        except Exception as e:
            print(f"❌ Auto-discovery error: {e}")
            self._manual_discovery_fallback()
    
    def _manual_discovery_fallback(self):
        """Fallback method if auto-discovery fails"""
        try:
            # Try to get available channels
            channels = self.liferay_client.get_available_channels()
            if channels:
                self.channel_id = channels[0]['id']
                self.available_channels = channels
                print(f"✅ Fallback: Using channel {channels[0]['name']} (ID: {self.channel_id})")
                
                # Try to get accounts for this channel
                accounts = self.liferay_client.get_available_accounts(self.channel_id)
                if accounts:
                    self.account_id = accounts[0]['id']
                    self.available_accounts = accounts
                    print(f"✅ Fallback: Using account {accounts[0]['name']} (ID: {self.account_id})")
                else:
                    print("⚠️  No accounts found for this channel")
            else:
                print("⚠️  No channels found - some functionality may be limited")
                
        except Exception as e:
            print(f"❌ Fallback discovery failed: {e}")
            self.channel_id = None
            self.account_id = None
            self.available_channels = []
            self.available_accounts = []
    
    def handle_customer_inquiry(self, inquiry: str) -> str:
        """Handle any customer inquiry using natural language processing"""
        inquiry = inquiry.lower().strip()
        
        # System status and discovery requests
        if any(word in inquiry for word in ['status', 'system', 'health', 'available', 'channels', 'accounts']):
            if any(word in inquiry for word in ['channel', 'account', 'list', 'show', 'what']):
                return self.list_available_channels_and_accounts()
            else:
                return self.get_system_status()
        
        # Order number lookup
        if any(word in inquiry for word in ['order', 'ord-', 'ord_']):
            # Extract order number if present
            import re
            order_match = re.search(r'ord[_-]?(\d+)', inquiry, re.IGNORECASE)
            if order_match:
                order_number = f"ORD-{order_match.group(1)}"
                return self.get_order_by_number(order_number)
            else:
                return "I can help you with order inquiries. Please provide the order number (e.g., ORD-12345) or tell me what you need help with."
        
        # Email-based customer lookup
        elif '@' in inquiry:
            return self.get_customer_orders(inquiry)
        
        # Status check
        elif any(word in inquiry for word in ['status', 'where', 'tracking', 'delivery']):
            if 'order' in inquiry:
                return "I can check your order status. Please provide the order number (e.g., ORD-12345)."
            else:
                return "I can help you check order status. Please provide the order number."
        
        # General help
        elif any(word in inquiry for word in ['help', 'what can you do', 'how do you work']):
            return self.get_help()
        
        # Customer name search
        elif any(word in inquiry for word in ['customer', 'name', 'person']):
            return "I can search for orders by customer name. Please provide the customer's name or email address."
        
        # Default response
        else:
            return """I'm here to help with your order inquiries! I can:
• Look up orders by order number (e.g., ORD-12345)
• Find orders by customer email
• Check order status and details
• Search for customer orders
• Show system status and available resources
• List available channels and accounts

What would you like to know about your orders or system resources?"""
    
    def search_orders(self, query: str) -> str:
        """Search for orders using various criteria"""
        try:
            # First try the enhanced search with discovered IDs
            if self.channel_id and self.account_id:
                # Use the new placed orders endpoint for better results
                try:
                    orders = self.liferay_client.get_all_placed_orders_by_account(self.channel_id, self.account_id)
                    
                    if orders:
                        # Filter orders based on the query
                        matching_orders = self._filter_orders_by_query(orders, query)
                        
                        if matching_orders:
                            if len(matching_orders) == 1:
                                return self._format_placed_order(matching_orders[0])
                            else:
                                return self._format_multiple_placed_orders(matching_orders)
                        else:
                            return f"I couldn't find any orders matching '{query}' in the available data. Please check the information and try again."
                    else:
                        return "I couldn't retrieve any orders at the moment. Please try again later."
                        
                except Exception as e:
                    print(f"Enhanced search failed, falling back to basic search: {e}")
            
            # Fallback to basic search
            orders = self.liferay_client.search_orders(query)
            
            if not orders:
                return f"I couldn't find any orders matching '{query}'. Please check the information and try again."
            
            if len(orders) == 1:
                return self._format_single_order(orders[0])
            else:
                return self._format_multiple_orders(orders)
                
        except Exception as e:
            return f"I encountered an error while searching for orders: {str(e)}"
    
    def _filter_orders_by_query(self, orders: List[Dict[str, Any]], query: str) -> List[Dict[str, Any]]:
        """Filter orders based on search query"""
        query_lower = query.lower()
        matching_orders = []
        
        for order in orders:
            # Check various fields for matches
            order_number = str(order.get('id', '')).lower()
            external_ref = str(order.get('externalReferenceCode', '')).lower()
            customer_name = str(order.get('author', '')).lower()
            account_name = str(order.get('account', '')).lower()
            status = str(order.get('status', '')).lower()
            
            if (query_lower in order_number or 
                query_lower in external_ref or 
                query_lower in customer_name or 
                query_lower in account_name or 
                query_lower in status):
                matching_orders.append(order)
        
        return matching_orders
    
    def get_order_by_number(self, order_number: str) -> str:
        """Get specific order details by order number"""
        try:
            # First try to find the order in the placed orders data
            if self.channel_id and self.account_id:
                try:
                    all_orders = self.liferay_client.get_all_placed_orders_by_account(self.channel_id, self.account_id)
                    
                    # Look for order by ID or external reference
                    order_id = None
                    if order_number.isdigit():
                        order_id = order_number
                    else:
                        # Try to extract ID from order number format
                        import re
                        id_match = re.search(r'(\d+)', order_number)
                        if id_match:
                            order_id = id_match.group(1)
                    
                    if order_id:
                        for order in all_orders:
                            if str(order.get('id')) == order_id:
                                return self._format_placed_order(order)
                        
                        # Try external reference
                        for order in all_orders:
                            if order.get('externalReferenceCode', '').lower() == order_number.lower():
                                return self._format_placed_order(order)
                
                except Exception as e:
                    print(f"Enhanced order lookup failed, falling back to basic search: {e}")
            
            # Fallback to basic search
            order = self.liferay_client.get_orders_by_order_number(order_number)
            
            if not order:
                return f"I couldn't find an order with number '{order_number}'. Please check the order number and try again."
            
            return self._format_single_order(order)
            
        except Exception as e:
            return f"I encountered an error while retrieving order {order_number}: {str(e)}"
    
    def get_customer_orders(self, email: str) -> str:
        """Get all orders for a specific customer"""
        try:
            # First try to find customer in the available accounts
            if self.available_accounts:
                for account in self.available_accounts:
                    # This is a simplified check - in a real scenario you'd want to match by email
                    if email.lower() in account.get('name', '').lower():
                        # Found matching account, get orders for this account
                        if self.channel_id:
                            orders = self.liferay_client.get_all_placed_orders_by_account(self.channel_id, account['id'])
                            if orders:
                                return self._format_multiple_placed_orders(orders)
            
            # Fallback to basic customer search
            orders = self.liferay_client.get_orders_by_customer_email(email)
            
            if not orders:
                return f"I couldn't find any orders for the email address '{email}'. Please check the email and try again."
            
            return self._format_multiple_orders(orders)
            
        except Exception as e:
            return f"I encountered an error while retrieving orders for {email}: {str(e)}"
    
    def get_order_status(self, order_number: str) -> str:
        """Get a quick status summary for an order"""
        try:
            order = self.liferay_client.get_orders_by_order_number(order_number)
            
            if not order:
                return f"I couldn't find an order with number '{order_number}'. Please check the order number and try again."
            
            summary = self.liferay_client.get_order_status_summary(order.id)
            
            return f"""
**Order Status Summary for {summary['order_number']}**
- **Status**: {summary['status']}
- **Order Date**: {summary['order_date']}
- **Customer**: {summary['customer_name']}
- **Total Amount**: {summary['total_amount']}
- **Items**: {summary['item_count']} items
            """.strip()
            
        except Exception as e:
            return f"I encountered an error while retrieving status for order {order_number}: {str(e)}"
    
    def _format_single_order(self, order: Order) -> str:
        """Format a single order for display"""
        items_text = "\n".join([
            f"  • {item.name} (Qty: {item.quantity}, Price: ${item.total_price:.2f})"
            for item in order.items
        ])
        
        return f"""
**Order Details for {order.order_number}**
- **Status**: {order.status}
- **Order Date**: {order.order_date.strftime('%Y-%m-%d %H:%M:%S')}
- **Customer**: {order.customer_name}
- **Total Amount**: ${order.total_amount:.2f}
- **Items**:
{items_text}
        """.strip()
    
    def _format_multiple_orders(self, orders: List[Order]) -> str:
        """Format multiple orders for display"""
        if len(orders) == 1:
            return self._format_single_order(orders[0])
        
        summary = f"I found {len(orders)} orders:\n\n"
        
        for i, order in enumerate(orders[:10], 1):  # Limit to first 10 orders
            summary += f"{i}. **{order.order_number}** - {order.customer_name} - ${order.total_amount:.2f} - {order.status}\n"
        
        if len(orders) > 10:
            summary += f"\n... and {len(orders) - 10} more orders."
        
        summary += "\n\nTo get detailed information about a specific order, please provide the order number."
        
        return summary
    
    def _format_placed_order(self, order: Dict[str, Any]) -> str:
        """Format a placed order for display"""
        order_id = order.get('id', 'N/A')
        external_ref = order.get('externalReferenceCode', 'N/A')
        status = order.get('status', 'N/A')
        order_status = order.get('orderStatusInfo', {}).get('label', 'N/A')
        create_date = order.get('createDate', 'N/A')
        customer_name = order.get('author', 'N/A')
        account_name = order.get('account', 'N/A')
        
        # Get order summary
        summary = order.get('summary', {})
        total_amount = summary.get('totalFormatted', 'N/A')
        items_count = summary.get('itemsQuantity', 'N/A')
        currency = summary.get('currency', 'N/A')
        
        # Get order items if available
        items_text = "Items details not available"
        try:
            if self.channel_id:
                order_items = self.liferay_client.get_placed_order_items(str(order_id))
                if order_items:
                    items_text = "\n".join([
                        f"  • {item.get('name', 'N/A')} (Qty: {item.get('quantity', 'N/A')}, SKU: {item.get('sku', 'N/A')})"
                        for item in order_items[:5]  # Show first 5 items
                    ])
                    if len(order_items) > 5:
                        items_text += f"\n  ... and {len(order_items) - 5} more items"
        except Exception as e:
            items_text = f"Items details unavailable (Error: {e})"
        
        return f"""
**Order Details for {external_ref} (ID: {order_id})**
- **Status**: {status} ({order_status})
- **Order Date**: {create_date}
- **Customer**: {customer_name}
- **Account**: {account_name}
- **Total Amount**: {total_amount} ({currency})
- **Items**: {items_count} items

**Order Items**:
{items_text}

**Order Summary**:
- **Subtotal**: {summary.get('subtotalFormatted', 'N/A')}
- **Shipping**: {summary.get('shippingValueFormatted', 'N/A')}
- **Tax**: {summary.get('taxValueFormatted', 'N/A')}
- **Total**: {total_amount}
        """.strip()
    
    def _format_multiple_placed_orders(self, orders: List[Dict[str, Any]]) -> str:
        """Format multiple placed orders for display"""
        if len(orders) == 1:
            return self._format_placed_order(orders[0])
        
        summary = f"I found {len(orders)} orders:\n\n"
        
        for i, order in enumerate(orders[:10], 1):  # Limit to first 10 orders
            order_id = order.get('id', 'N/A')
            external_ref = order.get('externalReferenceCode', 'N/A')
            customer_name = order.get('author', 'N/A')
            total_amount = order.get('summary', {}).get('totalFormatted', 'N/A')
            status = order.get('status', 'N/A')
            
            summary += f"{i}. **{external_ref}** (ID: {order_id}) - {customer_name} - {total_amount} - {status}\n"
        
        if len(orders) > 10:
            summary += f"\n... and {len(orders) - 10} more orders."
        
        summary += "\n\nTo get detailed information about a specific order, please provide the order number or ID."
        
        return summary
    
    def get_help(self) -> str:
        """Provide help information for the customer service agent"""
        # Include discovery information
        discovery_info = ""
        if hasattr(self, 'channel_id') and self.channel_id:
            discovery_info = f"\n**Current Channel**: {self.channel_id}"
        if hasattr(self, 'account_id') and self.account_id:
            discovery_info += f"\n**Current Account**: {self.account_id}"
        
        return f"""
**Customer Service Help**

I can help you with the following:

1. **Search for orders** - Provide an order number, customer email, or customer name
2. **Get order details** - Provide a specific order number for detailed information
3. **Check order status** - Get a quick status summary for any order
4. **Find customer orders** - Provide a customer email to see all their orders
5. **Enhanced order lookup** - Using the latest Liferay Commerce APIs for better results
6. **System status** - Check available channels, accounts, and data

**Examples:**
- "Find order ORD-12345"
- "What's the status of order 12345?"
- "Show me orders for john.doe@example.com"
- "Search for orders by John Smith"
- "Find order with ID 73048"
- "Look up order order-from-app-00EXVKHJ"
- "What channels and accounts are available?"
- "Show me system status"

**Available Resources**:
{discovery_info}

**Enhanced Features**:
- ✅ Auto-discovered channels and accounts
- ✅ Rich order data with items and shipping
- ✅ Multiple search methods (ID, external reference, customer)
- ✅ Fallback to basic search if enhanced search fails

How can I help you today?
        """.strip()
    
    def get_system_status(self) -> str:
        """Get system status and available resources"""
        try:
            status = "**System Status Report**\n\n"
            
            # Channel information
            if hasattr(self, 'channel_id') and self.channel_id:
                status += f"✅ **Channel**: {self.channel_id}\n"
                if hasattr(self, 'available_channels') and self.available_channels:
                    for channel in self.available_channels:
                        if str(channel.get('id')) == str(self.channel_id):
                            status += f"   - Name: {channel.get('name', 'N/A')}\n"
                            status += f"   - Type: {channel.get('type', 'N/A')}\n"
                            break
            else:
                status += "❌ **Channel**: Not discovered\n"
            
            # Account information
            if hasattr(self, 'account_id') and self.account_id:
                status += f"✅ **Account**: {self.account_id}\n"
                if hasattr(self, 'available_accounts') and self.available_accounts:
                    for account in self.available_accounts:
                        if str(account.get('id')) == str(self.account_id):
                            status += f"   - Name: {account.get('name', 'N/A')}\n"
                            status += f"   - Type: {account.get('type', 'N/A')}\n"
                            break
            else:
                status += "❌ **Account**: Not discovered\n"
            
            # Available resources
            if hasattr(self, 'available_channels') and self.available_channels:
                status += f"\n📊 **Available Resources**:\n"
                status += f"   - Channels: {len(self.available_channels)}\n"
                status += f"   - Accounts: {len(self.available_accounts) if hasattr(self, 'available_accounts') else 0}\n"
            
            # Test data availability
            if self.channel_id and self.account_id:
                try:
                    orders = self.liferay_client.get_placed_orders_by_account(self.channel_id, self.account_id, page=1, page_size=5)
                    if orders and 'items' in orders:
                        status += f"   - Orders: {orders.get('totalCount', 'Unknown')} available\n"
                    else:
                        status += "   - Orders: No data available\n"
                        
                    products = self.liferay_client.get_products_by_channel(self.channel_id, self.account_id, page=1, page_size=5)
                    if products and 'items' in products:
                        status += f"   - Products: {products.get('totalCount', 'Unknown')} available\n"
                    else:
                        status += "   - Products: No data available\n"
                        
                except Exception as e:
                    status += f"   - Data test: Error - {e}\n"
            
            status += "\n**Status**: ✅ System operational with enhanced discovery capabilities"
            return status
            
        except Exception as e:
            return f"**System Status Error**: {str(e)}"
    
    def list_available_channels_and_accounts(self) -> str:
        """List all available channels and accounts"""
        try:
            result = "**Available Channels and Accounts**\n\n"
            
            if hasattr(self, 'available_channels') and self.available_channels:
                result += "**📡 Channels:**\n"
                for i, channel in enumerate(self.available_channels, 1):
                    result += f"{i}. **{channel.get('name', 'N/A')}** (ID: {channel.get('id')})\n"
                    result += f"   - Type: {channel.get('type', 'N/A')}\n"
                    result += f"   - Active: {channel.get('active', 'N/A')}\n\n"
                
                # Show accounts for the current channel
                if hasattr(self, 'available_accounts') and self.available_accounts:
                    result += f"**👥 Accounts for Channel {self.channel_id}:**\n"
                    for i, account in enumerate(self.available_accounts, 1):
                        result += f"{i}. **{account.get('name', 'N/A')}** (ID: {account.get('id')})\n"
                        result += f"   - Type: {account.get('type', 'N/A')}\n"
                        result += f"   - Status: {account.get('status', 'N/A')}\n\n"
                else:
                    result += "**⚠️  No accounts found for the current channel**\n\n"
            else:
                result += "**❌ No channels available**\n\n"
            
            result += "**💡 How to use:**\n"
            result += "- Use these IDs in your API calls\n"
            result += "- Ask me to 'Find orders for account [ID]'\n"
            result += "- Ask me to 'Show products for channel [ID]'\n"
            
            return result
            
        except Exception as e:
            return f"**Error listing resources**: {str(e)}"

# Create the customer service agent instance
customer_service_agent = CustomerServiceAgent()
