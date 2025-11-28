# Copyright 2025 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is software
# distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Customer Service Tools for the Liferay Agent."""

from google.adk.tools import FunctionTool
from typing import Optional
import sys
import os

# Create a compatibility wrapper for FunctionTool
class CompatibleFunctionTool(FunctionTool):
    """FunctionTool wrapper that adds missing _require_confirmation attribute for deployed environment compatibility."""
    def __init__(self, func, _require_confirmation=False):
        super().__init__(func)
        self._require_confirmation = _require_confirmation

# Add the parent directory to the path to import liferay_client
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from liferay_client import LiferayAPIClient

# Initialize the Liferay client
liferay_client = LiferayAPIClient()

def get_system_status() -> str:
    """Get system status and available resources for Liferay Commerce"""
    try:
        # Auto-discover available channels and accounts
        result = liferay_client.auto_discover_and_test()
        
        if result['success']:
            channel_id = result['test_channel']['id']
            account_id = result['test_account']['id']
            channels = result['channels']
            accounts = result['accounts']
            
            status = "**System Status Report**\n\n"
            status += f"✅ **Channel**: {channel_id}\n"
            status += f"   - Name: {result['test_channel']['name']}\n"
            status += f"   - Type: {result['test_channel']['type']}\n"
            
            status += f"✅ **Account**: {account_id}\n"
            status += f"   - Name: {result['test_account']['name']}\n"
            status += f"   - Type: {result['test_account']['type']}\n"
            
            status += f"\n📊 **Available Resources**:\n"
            status += f"   - Channels: {len(channels)}\n"
            status += f"   - Accounts: {len(accounts)}\n"
            
            # Test data availability
            try:
                orders = liferay_client.get_placed_orders_by_account(channel_id, account_id, page=1, page_size=5)
                if orders and 'items' in orders:
                    status += f"   - Orders: {orders.get('totalCount', 'Unknown')} available\n"
                else:
                    status += "   - Orders: No data available\n"
                    
                products = liferay_client.get_products_by_channel(channel_id, account_id, page=1, page_size=5)
                if products and 'items' in products:
                    status += f"   - Products: {products.get('totalCount', 'Unknown')} available\n"
                else:
                    status += "   - Products: No data available\n"
                    
            except Exception as e:
                status += f"   - Data test: Error - {e}\n"
            
            status += "\n**Status**: ✅ System operational with enhanced discovery capabilities"
            return status
        else:
            return f"**System Status Error**: {result['message']}"
            
    except Exception as e:
        return f"**System Status Error**: {str(e)}"

def list_available_channels_and_accounts() -> str:
    """List all available channels and accounts in Liferay Commerce"""
    try:
        channels = liferay_client.get_available_channels()
        result = "**Available Channels and Accounts**\n\n"
        
        if channels:
            result += "**📡 Channels:**\n"
            for i, channel in enumerate(channels, 1):
                result += f"{i}. **{channel.get('name', 'N/A')}** (ID: {channel.get('id')})\n"
                result += f"   - Type: {channel.get('type', 'N/A')}\n"
                result += f"   - Active: {channel.get('active', 'N/A')}\n\n"
            
            # Show accounts for the first channel
            if channels:
                first_channel = channels[0]
                accounts = liferay_client.get_available_accounts(first_channel['id'])
                
                if accounts:
                    result += f"**👥 Accounts for Channel {first_channel['id']}:**\n"
                    for i, account in enumerate(accounts, 1):
                        result += f"{i}. **{account.get('name', 'N/A')}** (ID: {account.get('id')})\n"
                        result += f"   - Type: {account.get('type', 'N/A')}\n"
                        result += f"   - Status: {account.get('status', 'N/A')}\n\n"
                else:
                    result += "**⚠️  No accounts found for this channel**\n\n"
        else:
            result += "**❌ No channels available**\n\n"
        
        result += "**💡 How to use:**\n"
        result += "- Use these IDs in your API calls\n"
        result += "- Ask me to 'Find orders for account [ID]'\n"
        result += "- Ask me to 'Show products for channel [ID]'\n"
        
        return result
        
    except Exception as e:
        return f"**Error listing resources**: {str(e)}"

def find_order(order_id: str) -> str:
    """Find order details by order ID"""
    try:
        # First try to get order details
        order_details = liferay_client.get_order_details(order_id)
        
        if order_details:
            return f"**Order Found**: {order_details}"
        else:
            # Try to find in placed orders using auto-discovery
            try:
                # Auto-discover available channels and accounts
                channels = liferay_client.get_available_channels()
                if not channels:
                    return "No channels available in the system."
                
                channel_id = channels[0]['id']  # Use first available channel
                
                # Get all accounts for this channel
                accounts = liferay_client.get_available_accounts(channel_id)
                if not accounts:
                    return f"No accounts available for channel {channel_id}."
                
                # Search across all accounts for the order
                order_found = None
                found_in_account = None
                
                for account in accounts:
                    try:
                        orders = liferay_client.get_all_placed_orders_by_account(channel_id, account['id'])
                        for order in orders:
                            if str(order.get('id')) == order_id:
                                order_found = order
                                found_in_account = account
                                break
                        if order_found:
                            break
                    except:
                        continue
                
                if not order_found:
                    return f"I couldn't find an order with ID '{order_id}' in any available account. Please check the order ID and try again."
                
                # Format the order details
                order_id_val = order_found.get('id', 'N/A')
                external_ref = order_found.get('externalReferenceCode', 'N/A')
                status = order_found.get('status', 'N/A')
                order_status = order_found.get('orderStatusInfo', {}).get('label', 'N/A')
                create_date = order_found.get('createDate', 'N/A')
                customer_name = order_found.get('author', 'N/A')
                account_name = found_in_account.get('name', 'N/A')
                
                summary = order_found.get('summary', {})
                total_amount = summary.get('totalFormatted', 'N/A')
                items_count = summary.get('itemsQuantity', 'N/A')
                
                # Try to get detailed order items
                order_items = []
                try:
                    items_response = liferay_client.get_placed_order_items(order_id)
                    if items_response and 'items' in items_response:
                        order_items = items_response['items']
                except Exception as e:
                    # If we can't get items, continue without them
                    pass
                
                # Build the response
                result = f"""
**Order Details for Order {order_id_val}**
- **Reference**: {external_ref}
- **Status**: {status} ({order_status})
- **Order Date**: {create_date}
- **Customer**: {customer_name}
- **Account**: {account_name}
- **Total Amount**: {total_amount}
- **Items**: {items_count} items
                """.strip()
                
                # Add shipping information if available
                summary = order_found.get('summary', {})
                shipping_value = summary.get('shippingValueFormatted', 'N/A')
                tax_value = summary.get('taxValueFormatted', 'N/A')
                subtotal = summary.get('subtotalFormatted', 'N/A')
                
                if shipping_value != 'N/A' or tax_value != 'N/A':
                    result += f"\n**💰 Cost Breakdown:**\n"
                    result += f"- **Subtotal**: {subtotal}\n"
                    result += f"- **Shipping**: {shipping_value}\n"
                    result += f"- **Tax**: {tax_value}\n"
                    result += f"- **Total**: {total_amount}\n"
                
                # Add shipping address preview if available
                shipping_address = order_found.get('shippingAddress', {})
                if shipping_address:
                    result += f"\n**📍 Shipping Address:**\n"
                    result += f"- **Name**: {shipping_address.get('name', 'N/A')}\n"
                    result += f"- **City**: {shipping_address.get('city', 'N/A')}, {shipping_address.get('regionISOCode', 'N/A')}\n"
                    result += f"- **Country**: {shipping_address.get('countryISOCode', 'N/A')}\n"
                    result += f"\n💡 **For complete shipping details, ask**: 'Get shipping info for order {order_id}'"
                
                # Add item details if available
                if order_items:
                    result += f"\n\n**📦 Order Items:**\n"
                    for i, item in enumerate(order_items[:10], 1):  # Show first 10 items
                        item_name = item.get('name', 'Unknown Product')
                        item_sku = item.get('sku', 'N/A')
                        item_quantity = item.get('quantity', 'N/A')
                        item_price = item.get('finalPriceFormatted', 'N/A')
                        
                        result += f"{i}. **{item_name}**\n"
                        result += f"   SKU: {item_sku} | Qty: {item_quantity} | Price: {item_price}\n"
                    
                    if len(order_items) > 10:
                        result += f"\n... and {len(order_items) - 10} more items"
                else:
                    result += f"\n\n**📦 Order Items:** Unable to retrieve item details at this time."
                    result += f"\n💡 **Tip**: Try asking 'Get order items for order {order_id}' for detailed item information."
                
                return result
                
            except Exception as e:
                return f"Error searching placed orders: {str(e)}"
    
    except Exception as e:
        return f"Error finding order: {str(e)}"

def search_orders_by_date_range(start_date: str, end_date: Optional[str] = None, user_email: Optional[str] = None) -> str:
    """Search for orders within a specific date range for a specific user"""
    try:
        from datetime import datetime, timedelta
        import re
        
        # Check if user email is provided
        if not user_email:
            return """
**❌ User Identification Required**

To search for orders by date range, I need to know which customer you are.

**Please provide your email address:**
- "Search my orders from 2024-01-01 to 2024-01-31 using your.email@example.com"
- "Find orders since 2024-01-01 for customer@company.com"
- "Show orders from last 7 days using myemail@domain.com"

**💡 Use the same email address you used when placing your orders.**
            """.strip()
        
        # Look up account information by email using Liferay API
        try:
            print(f"DEBUG: Looking up account for email: {user_email}")
            
            # First try to find customer by email
            customer = liferay_client.get_customer_by_email(user_email)
            
            if customer:
                # If customer found, use their account ID
                account_id = customer.id
                account_name = f"{customer.first_name} {customer.last_name}".strip() or customer.email
                print(f"DEBUG: Found customer account - ID: {account_id}, Name: {account_name}")
            else:
                # If no customer found, try to find account by email in account list
                print(f"DEBUG: Customer not found, searching accounts by email...")
                
                # Get all accounts and search for one with matching email
                channels = liferay_client.get_available_channels()
                if not channels:
                    return "No channels available in the system."
                
                channel_id = channels[0]['id']
                accounts = liferay_client.get_available_accounts(channel_id)
                
                account_found = None
                for account in accounts:
                    # Check if account name or other fields contain the email
                    account_name = account.get('name', '').lower()
                    if user_email.lower() in account_name or account_name in user_email.lower():
                        account_found = account
                        break
                
                if not account_found:
                    # Try hardcoded mapping for test emails
                    test_email_mapping = {
                        "john.smith@betavehicle.com": {"id": "36650", "name": "Beta Vehicle Supply"},
                        "john.smith@nexusaccessories.com": {"id": "36558", "name": "Nexus Accessories"},
                        "john.smith@spiritsecurity.com": {"id": "36573", "name": "Spirit Security"},
                        "john.smith@dynamicfluids.com": {"id": "36584", "name": "Dynamic Fluids"},
                        "john.smith@sparkfabrication.com": {"id": "36595", "name": "Spark Fabrication"},
                        "john.smith@biogarage.com": {"id": "36606", "name": "Bio Garage"},
                        "john.smith@novalubricants.com": {"id": "36617", "name": "Nova Lubricants"},
                        "john.smith@crestviewinteriors.com": {"id": "36628", "name": "Crestview Interiors"},
                        "john.smith@velocityrecovery.com": {"id": "36639", "name": "Velocity Recovery"},
                        "john.smith@firstrestoration.com": {"id": "36661", "name": "First Restoration"},
                    }
                    
                    if user_email.lower() in test_email_mapping:
                        account_found = test_email_mapping[user_email.lower()]
                        print(f"DEBUG: Found test account mapping - ID: {account_found['id']}, Name: {account_found['name']}")
                
                if not account_found:
                    return f"""
**❌ Account not found**

The email '{user_email}' was not found in our system.

**Please check:**
- Email spelling and format
- If you have an account with us
- Contact customer support if you believe this is an error

**💡 Tip:** Make sure you're using the same email address you used when placing your orders.
                    """.strip()
            
            account_id = account_found['id']
            account_name = account_found['name']
            print(f"DEBUG: Found account - ID: {account_id}, Name: {account_name}")
        
        except Exception as e:
            print(f"DEBUG: Exception during account lookup: {e}")
            return f"""
**❌ Error looking up account**

There was an error looking up the email '{user_email}': {str(e)}

**Please try:**
- Check your email address spelling
- Contact customer support if the problem persists
            """.strip()
        
        # Parse date inputs - support various formats
        def parse_date(date_str):
            date_str = date_str.strip()
            # Try common formats
            formats = [
                '%Y-%m-%d',      # 2024-01-15
                '%m/%d/%Y',      # 01/15/2024
                '%m-%d-%Y',      # 01-15-2024
                '%Y-%m-%d %H:%M:%S',  # 2024-01-15 10:30:00
                '%m/%d/%Y %H:%M:%S',  # 01/15/2024 10:30:00
            ]
            
            for fmt in formats:
                try:
                    return datetime.strptime(date_str, fmt)
                except ValueError:
                            continue
            
            # Try relative dates
            if date_str.lower() in ['today', 'now']:
                return datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
            elif date_str.lower() == 'yesterday':
                return (datetime.now() - timedelta(days=1)).replace(hour=0, minute=0, second=0, microsecond=0)
            elif date_str.lower().startswith('last '):
                # Handle "last 7 days", "last 30 days", etc.
                match = re.search(r'last (\d+) days?', date_str.lower())
                if match:
                    days = int(match.group(1))
                    return (datetime.now() - timedelta(days=days)).replace(hour=0, minute=0, second=0, microsecond=0)
            
            raise ValueError(f"Unable to parse date: {date_str}")
        
        # Parse start and end dates
        try:
            start_dt = parse_date(start_date)
            end_dt = parse_date(end_date) if end_date else datetime.now()
            
            # Ensure end date is at end of day
            if end_dt.hour == 0 and end_dt.minute == 0:
                end_dt = end_dt.replace(hour=23, minute=59, second=59)
                
        except ValueError as e:
            return f"**❌ Date Parsing Error**: {str(e)}\n\n**Supported formats:**\n- YYYY-MM-DD (2024-01-15)\n- MM/DD/YYYY (01/15/2024)\n- MM-DD-YYYY (01-15-2024)\n- today, yesterday\n- last X days (last 7 days)\n\n**Example:** 'Search orders from 2024-01-01 to 2024-01-31'"
        
        # Get orders for the specific user's account with server-side date filtering
        try:
            channels = liferay_client.get_available_channels()
            if not channels:
                return "No channels available in the system."
            
            channel_id = channels[0]['id']
            
            # Convert dates to ISO format for API filtering
            start_date_iso = start_dt.strftime('%Y-%m-%dT%H:%M:%SZ')
            end_date_iso = end_dt.strftime('%Y-%m-%dT%H:%M:%SZ')
            
            # Use optimized method with server-side filtering and pagination limits
            matching_orders = liferay_client.get_placed_orders_by_account_with_filters(
                channel_id, account_id, 
                start_date=start_date_iso, 
                end_date=end_date_iso,
                page_size=50,  # Reasonable page size
                max_pages=3    # Limit to 3 pages (150 orders max)
            )
                
        except Exception as e:
            return f"**❌ Error retrieving orders for {user_email}**: {str(e)}"
        
        if not matching_orders:
            return f"**No orders found** for {user_email} between {start_dt.strftime('%Y-%m-%d')} and {end_dt.strftime('%Y-%m-%d')}"
        
        # Sort by date (newest first)
        matching_orders.sort(key=lambda x: x.get('createDate', ''), reverse=True)
        
        result = f"**📅 Orders Found: {len(matching_orders)} orders for {user_email}**\n"
        result += f"**Customer**: {account_name}\n"
        result += f"**Date Range**: {start_dt.strftime('%Y-%m-%d')} to {end_dt.strftime('%Y-%m-%d')}\n\n"
        
        # Show first 20 orders
        for i, order in enumerate(matching_orders[:20], 1):
            order_id = order.get('id', 'N/A')
            external_ref = order.get('externalReferenceCode', 'N/A')
            create_date = order.get('createDate', 'N/A')
            status = order.get('status', 'N/A')
            total = order.get('summary', {}).get('totalFormatted', 'N/A')
            
            # Format date for display
            try:
                if create_date:
                    display_date = datetime.fromisoformat(create_date.replace('Z', '+00:00')).strftime('%Y-%m-%d %H:%M')
                else:
                    display_date = 'N/A'
            except:
                display_date = create_date
                
            result += f"{i}. **Order {order_id}** - {external_ref}\n"
            result += f"   Date: {display_date}, Status: {status}, Total: {total}\n\n"
        
        if len(matching_orders) > 20:
            result += f"... and {len(matching_orders) - 20} more orders.\n"
        
        result += f"\n**💡 Tips:**\n"
        result += f"- Use 'Find order [ID]' for detailed order information\n"
        result += f"- Use 'Get order items for order [ID]' for item details\n"
        result += f"- Use 'Get shipping info for order [ID]' for shipping details\n"
        
        return result.strip()
        
    except Exception as e:
        return f"**❌ Error searching orders by date**: {str(e)}"

def search_orders(query: str) -> str:
    """Search for orders using various criteria with intelligent routing to fastest tools"""
    try:
        import re
        
        query_lower = query.lower()
        
        # Performance-optimized routing: Try fastest tools first
        
        # 1. Check for direct order ID lookup (FASTEST - 1s)
        order_id_pattern = r'\b\d{4,6}\b'  # 4-6 digit order IDs
        order_id_match = re.search(order_id_pattern, query)
        if order_id_match:
            order_id = order_id_match.group()
            return f"""
**🚀 Fast Route: Direct Order Lookup**

I found what looks like an order ID ({order_id}) in your query. 
Let me get that order directly for you - this will be much faster than searching.

{find_order(order_id)}
            """.strip()
        
        # 2. Check for date range search (GOOD - 6s)
        if any(phrase in query_lower for phrase in ['from', 'between', 'since', 'after', 'before', 'date range', 'last ']):
            return f"""
**🚀 Fast Route: Date Range Search**

I detected a date-related search. Let me route this to the optimized date range search tool for better performance.

{search_orders_by_date_range.__doc__}

**Please provide your email address to continue:**
- "Search my orders from 2024-01-01 to 2024-01-31 using your.email@example.com"
- "Find orders since 2024-01-01 for customer@company.com"
            """.strip()
        
        # 3. Check for product search (SLOW - 1m 47s)
        if any(phrase in query_lower for phrase in ['product', 'item', 'contains', 'battery', 'tire', 'brake', 'oil']):
            return f"""
**⚠️ Slow Route: Product Search**

I detected a product-related search. This will take about 1-2 minutes to complete.

**Available options:**
1. **Fast alternative**: If you know the order ID, use "Find order [ID]" (1 second)
2. **Continue with product search**: Provide your email address

**Please provide your email address:**
- "Search my orders for brake pads using your.email@example.com"
            """.strip()
        
        # 4. Check for status search (SLOW - 1m 45s)
        status_keywords = ['pending', 'shipped', 'completed', 'canceled', 'processing', 'hold', 'status']
        if any(keyword in query_lower for keyword in status_keywords):
            return f"""
**⚠️ Slow Route: Status Search**

I detected a status-related search. This will take about 1-2 minutes to complete.

**Available options:**
1. **Fast alternative**: If you know the order ID, use "Find order [ID]" (1 second)
2. **Continue with status search**: Provide your email address

**Please provide your email address:**
- "Search my pending orders using your.email@example.com"
            """.strip()
        
        # 5. Check for address search (EXTREMELY SLOW - 5m+)
        address_keywords = ['address', 'shipped to', 'delivery', 'city', 'state', 'zip', 'postal']
        if any(keyword in query_lower for keyword in address_keywords):
            return f"""
**🐌 Extremely Slow Route: Address Search**

I detected an address-related search. This will take 5+ minutes to complete.

**Available options:**
1. **Fast alternative**: If you know the order ID, use "Find order [ID]" (1 second)
2. **Medium speed**: Try date range search if you know the timeframe (6 seconds)
3. **Continue with address search**: Provide your email address (5+ minutes)

**Please provide your email address:**
- "Search orders shipped to New York using your.email@example.com"
            """.strip()
        
        # 6. Default fallback with performance guidance
        return f"""
**🔍 General Order Search**

I can help you search for orders, but I need more specific information to route you to the fastest tool.

**🚀 Fastest Options (1-6 seconds):**
- **Direct order lookup**: "Find order 12345" (1 second)
- **Date range search**: "Orders from 2024-01-01 to 2024-01-31" (6 seconds)
- **Customer orders**: "Get orders for customer@email.com" (6 seconds)

**⚠️ Slower Options (1-5 minutes):**
- **Product search**: "Orders containing 'battery'" (1m 47s)
- **Status search**: "Pending orders" (1m 45s)
- **Address search**: "Orders shipped to Dallas" (5m+)

**Please specify:**
1. What type of search you need
2. Your email address (if searching your orders)
3. Any specific criteria (dates, products, status, etc.)
        """.strip()
        
        # This code is now unreachable due to early returns above
        # The function now uses intelligent routing instead of pattern matching
    
    except Exception as e:
        return f"Error searching orders: {str(e)}"

def get_customer_orders(email: str) -> str:
    """Get orders for a specific customer email"""
    try:
        # Look up account information by email using Liferay API
        try:
            print(f"DEBUG: Looking up account for email: {email}")
            
            # First try to find customer by email
            customer = liferay_client.get_customer_by_email(email)
            
            if customer:
                # If customer found, use their account ID
                account_id = customer.id
                account_name = f"{customer.first_name} {customer.last_name}".strip() or customer.email
                print(f"DEBUG: Found customer account - ID: {account_id}, Name: {account_name}")
            else:
                # If no customer found, try to find account by email in account list
                print(f"DEBUG: Customer not found, searching accounts by email...")
                
                # Get all accounts and search for one with matching email
                channels = liferay_client.get_available_channels()
                if not channels:
                    return "No channels available in the system."
                
                channel_id = channels[0]['id']
                accounts = liferay_client.get_available_accounts(channel_id)
                
                account_found = None
                for account in accounts:
                    # Check if account name or other fields contain the email
                    account_name = account.get('name', '').lower()
                    if email.lower() in account_name or account_name in email.lower():
                        account_found = account
                        break
                
                if not account_found:
                    # Try hardcoded mapping for test emails
                    test_email_mapping = {
                        "john.smith@betavehicle.com": {"id": "36650", "name": "Beta Vehicle Supply"},
                        "john.smith@nexusaccessories.com": {"id": "36558", "name": "Nexus Accessories"},
                        "john.smith@spiritsecurity.com": {"id": "36573", "name": "Spirit Security"},
                        "john.smith@dynamicfluids.com": {"id": "36584", "name": "Dynamic Fluids"},
                        "john.smith@sparkfabrication.com": {"id": "36595", "name": "Spark Fabrication"},
                        "john.smith@biogarage.com": {"id": "36606", "name": "Bio Garage"},
                        "john.smith@novalubricants.com": {"id": "36617", "name": "Nova Lubricants"},
                        "john.smith@crestviewinteriors.com": {"id": "36628", "name": "Crestview Interiors"},
                        "john.smith@velocityrecovery.com": {"id": "36639", "name": "Velocity Recovery"},
                        "john.smith@firstrestoration.com": {"id": "36661", "name": "First Restoration"},
                    }
                    
                    if email.lower() in test_email_mapping:
                        account_found = test_email_mapping[email.lower()]
                        print(f"DEBUG: Found test account mapping - ID: {account_found['id']}, Name: {account_found['name']}")
                
                if not account_found:
                    return f"""
**❌ Account not found**

The email '{email}' was not found in our system.

**Please check:**
- Email spelling and format
- If you have an account with us
- Contact customer support if you believe this is an error

**💡 Tip:** Make sure you're using the same email address you used when placing your orders.
                    """.strip()
                
                account_id = account_found['id']
                account_name = account_found['name']
                print(f"DEBUG: Found account - ID: {account_id}, Name: {account_name}")
            
        except Exception as e:
            print(f"DEBUG: Exception during account lookup: {e}")
            return f"""
**❌ Error looking up account**

There was an error looking up the email '{email}': {str(e)}

**Please try:**
- Check your email address spelling
- Contact customer support if the problem persists
            """.strip()
            
            # Get orders for this account
        try:
            channels = liferay_client.get_available_channels()
            if not channels:
                return "No channels available in the system."
            
            channel_id = channels[0]['id']
            orders = liferay_client.get_all_placed_orders_by_account(channel_id, account_id)
            
            if not orders:
                return f"I found the account for '{email}' but no orders are available."
            
        except Exception as e:
            return f"**❌ Error retrieving orders for {email}**: {str(e)}"
        
        # Sort orders by date (newest first)
        orders.sort(key=lambda x: x.get('createDate', ''), reverse=True)
        
        # Format the response
        if len(orders) == 1:
            order = orders[0]
            return f"""
**Customer Orders for {email}**
**Account**: {account_name} (ID: {account_id})

**Found 1 order:**
- **Order ID**: {order.get('id', 'N/A')}
- **Reference**: {order.get('externalReferenceCode', 'N/A')}
- **Date**: {order.get('createDate', 'N/A')}
- **Status**: {order.get('status', 'N/A')}
- **Total**: {order.get('summary', {}).get('totalFormatted', 'N/A')}
            """.strip()
        else:
            result = f"""**Customer Orders for {email}**
**Account**: {account_name} (ID: {account_id})

**Found {len(orders)} orders:**"""
            
            # Show first 10 orders
            for i, order in enumerate(orders[:10], 1):
                order_id = order.get('id', 'N/A')
                external_ref = order.get('externalReferenceCode', 'N/A')
                create_date = order.get('createDate', 'N/A')
                status = order.get('status', 'N/A')
                total = order.get('summary', {}).get('totalFormatted', 'N/A')
                
                result += f"\n{i}. **Order {order_id}** - {order_id}"
                result += f"\n   Date: {create_date}, Status: {status}, Total: {total}"
            
            if len(orders) > 10:
                result += f"\n\n... and {len(orders) - 10} more orders."
            
            result += f"\n\n**Total Orders**: {len(orders)}"
            result += f"\n**Account**: {account_name}"
            
            return result
    
    except Exception as e:
        return f"Error getting customer orders: {str(e)}"

def search_orders_by_product(product_description: str, user_email: Optional[str] = None) -> str:
    """Search for orders containing specific products or product descriptions"""
    try:
        from datetime import datetime
        
        # Check if user email is provided
        if not user_email:
            return """
**❌ User Identification Required**

To search for orders by product, I need to know which customer you are.

**Please provide your email address:**
- "Search my orders for brake pads using your.email@example.com"
- "Find orders containing 'tires' for customer@company.com"
- "Show me orders with 'oil' using myemail@domain.com"

**💡 Use the same email address you used when placing your orders.**
            """.strip()
        
        # Look up account information by email using Liferay API
        try:
            print(f"DEBUG: Looking up account for email: {user_email}")
            
            # First try to find customer by email
            customer = liferay_client.get_customer_by_email(user_email)
            
            if customer:
                # If customer found, use their account ID
                account_id = customer.id
                account_name = f"{customer.first_name} {customer.last_name}".strip() or customer.email
                print(f"DEBUG: Found customer account - ID: {account_id}, Name: {account_name}")
            else:
                # If no customer found, try to find account by email in account list
                print(f"DEBUG: Customer not found, searching accounts by email...")
                
                # Get all accounts and search for one with matching email
                channels = liferay_client.get_available_channels()
                if not channels:
                    return "No channels available in the system."
                
                channel_id = channels[0]['id']
                accounts = liferay_client.get_available_accounts(channel_id)
                
                account_found = None
                for account in accounts:
                    # Check if account name or other fields contain the email
                    account_name = account.get('name', '').lower()
                    if user_email.lower() in account_name or account_name in user_email.lower():
                        account_found = account
                        break
                
                if not account_found:
                    # Try hardcoded mapping for test emails
                    test_email_mapping = {
                        "john.smith@betavehicle.com": {"id": "36650", "name": "Beta Vehicle Supply"},
                        "john.smith@nexusaccessories.com": {"id": "36558", "name": "Nexus Accessories"},
                        "john.smith@spiritsecurity.com": {"id": "36573", "name": "Spirit Security"},
                        "john.smith@dynamicfluids.com": {"id": "36584", "name": "Dynamic Fluids"},
                        "john.smith@sparkfabrication.com": {"id": "36595", "name": "Spark Fabrication"},
                        "john.smith@biogarage.com": {"id": "36606", "name": "Bio Garage"},
                        "john.smith@novalubricants.com": {"id": "36617", "name": "Nova Lubricants"},
                        "john.smith@crestviewinteriors.com": {"id": "36628", "name": "Crestview Interiors"},
                        "john.smith@velocityrecovery.com": {"id": "36639", "name": "Velocity Recovery"},
                        "john.smith@firstrestoration.com": {"id": "36661", "name": "First Restoration"},
                    }
                    
                    if user_email.lower() in test_email_mapping:
                        account_found = test_email_mapping[user_email.lower()]
                        print(f"DEBUG: Found test account mapping - ID: {account_found['id']}, Name: {account_found['name']}")
                
                if not account_found:
                    return f"""
**❌ Account not found**

The email '{user_email}' was not found in our system.

**Please check:**
- Email spelling and format
- If you have an account with us
- Contact customer support if you believe this is an error

**💡 Tip:** Make sure you're using the same email address you used when placing your orders.
                """.strip()
                
                account_id = account_found['id']
                account_name = account_found['name']
                print(f"DEBUG: Found account - ID: {account_id}, Name: {account_name}")
            
        except Exception as e:
            print(f"DEBUG: Exception during account lookup: {e}")
            return f"""
**❌ Error looking up account**

There was an error looking up the email '{user_email}': {str(e)}

**Please try:**
- Check your email address spelling
- Contact customer support if the problem persists
            """.strip()
        
        # Get all orders for this account
        try:
            channels = liferay_client.get_available_channels()
            if not channels:
                return "No channels available in the system."
            
            channel_id = channels[0]['id']
            all_orders = liferay_client.get_placed_orders_by_account_limited(channel_id, account_id, max_orders=100)
            print(f"DEBUG: Found {len(all_orders)} total orders for account {account_id}")
            
        except Exception as e:
            print(f"DEBUG: Exception getting orders: {e}")
            return f"Error retrieving orders: {str(e)}"
        
        if not all_orders:
            return f"**No orders found** for {user_email}"
        
        # Search through orders for products matching the description
        matching_orders = []
        product_description_lower = product_description.lower()
        
        for order in all_orders:
            try:
                order_id = order.get('id', 'N/A')
                print(f"DEBUG: Checking order {order_id} for products...")
                
                # Get order items for this order
                try:
                    order_items = liferay_client.get_placed_order_items(order_id)
                    
                    # Handle both list and dict responses
                    if isinstance(order_items, list):
                        items = order_items
                    elif isinstance(order_items, dict) and 'items' in order_items:
                        items = order_items['items']
                    else:
                        items = []
                except Exception as item_error:
                    print(f"DEBUG: Error getting items for order {order_id}: {item_error}")
                    items = []
                
                # Check if any item matches the product description
                matching_items = []
                for item in items:
                    item_name = item.get('name', '').lower()
                    item_sku = item.get('sku', '').lower()
                    
                    if (product_description_lower in item_name or 
                        product_description_lower in item_sku or
                        item_name in product_description_lower):
                        matching_items.append(item)
                
                if matching_items:
                    # Add order with matching items
                    order['matching_items'] = matching_items
                    matching_orders.append(order)
                    print(f"DEBUG: Found {len(matching_items)} matching items in order {order_id}")
                        
            except Exception as order_error:
                print(f"DEBUG: Error processing order {order_id}: {order_error}")
                continue
        
        # Sort orders by date (newest first)
        matching_orders.sort(key=lambda x: x.get('createDate', ''), reverse=True)
        
        # Format results
        if not matching_orders:
                return f"""
**🔍 No Orders Found**

No orders found containing products matching: **"{product_description}"**

**Customer**: {account_name}
**Search Term**: {product_description}

**💡 Try:**
- Different product names or keywords
- Partial product names (e.g., "brake" instead of "brake pads")
- SKU codes if you know them
- More general terms (e.g., "parts" instead of specific part names)
                """.strip()
    
        result = f"**🔍 Product Search Results: {len(matching_orders)} orders containing '{product_description}'**\n"
        result += f"**Customer**: {account_name}\n"
        result += f"**Search Term**: {product_description}\n\n"
        
        # Show first 10 matching orders
        for i, order in enumerate(matching_orders[:10], 1):
            order_id = order.get('id', 'N/A')
            external_ref = order.get('externalReferenceCode', 'N/A')
            create_date = order.get('createDate', 'N/A')
            status = order.get('status', 'N/A')
            total = order.get('summary', {}).get('totalFormatted', 'N/A')
            
            # Format date for display
            try:
                if create_date:
                    display_date = datetime.fromisoformat(create_date.replace('Z', '+00:00')).strftime('%Y-%m-%d %H:%M')
                else:
                    display_date = 'N/A'
            except:
                display_date = create_date
            
            result += f"{i}. **Order {order_id}** - {external_ref}\n"
            result += f"   Date: {display_date}, Status: {status}, Total: {total}\n"
            
            # Show matching items
            matching_items = order.get('matching_items', [])
            if matching_items:
                result += f"   **Matching Products:**\n"
                for item in matching_items[:3]:  # Show first 3 matching items
                    item_name = item.get('name', 'Unknown Product')
                    item_sku = item.get('sku', 'N/A')
                    quantity = item.get('quantity', 1)
                    result += f"   - {item_name} (SKU: {item_sku}) x{quantity}\n"
                
                if len(matching_items) > 3:
                    result += f"   - ... and {len(matching_items) - 3} more items\n"
            
            result += "\n"
        
        if len(matching_orders) > 10:
            result += f"... and {len(matching_orders) - 10} more orders.\n\n"
        
        result += f"**Total Orders Found**: {len(matching_orders)}\n"
        result += f"**Customer**: {account_name}\n\n"
        result += "**💡 Tips:**\n"
        result += "- Use 'Find order [ID]' for detailed order information\n"
        result += "- Use 'Get order items for order [ID]' for complete item details\n"
        result += "- Try different search terms for better results\n"
        
        return result
    
    except Exception as e:
        return f"Error searching orders by product: {str(e)}"

def search_orders_by_shipping_address_enhanced(address_query: str, user_email: Optional[str] = None) -> str:
    """Enhanced search for orders by shipping address using shipments endpoint"""
    if not user_email:
        return """
**❌ User Identification Required**

To search for orders by shipping address, I need to know which customer you are.

**Please provide your email address:**
- "Search orders shipped to New York using your.email@example.com"
- "Find orders with address containing 'Main Street' for customer@company.com"
- "Show me orders shipped to California using myemail@domain.com"

**Available Address Search Options:**
- Street name or number
- City name
- State/Province
- Postal/ZIP code
- Country
- Partial address matches

**💡 Use the same email address you used when placing your orders.**
        """.strip()
    
    # Look up account information by email using Liferay API
    try:
        print(f"DEBUG: Looking up account for email: {user_email}")
        customer = liferay_client.get_customer_by_email(user_email)
        
        if customer:
            account_id = customer.id
            account_name = f"{customer.first_name} {customer.last_name}".strip() or customer.email
            print(f"DEBUG: Found customer - ID: {account_id}, Name: {account_name}")
        else:
            print("DEBUG: Customer not found, searching accounts by email...")
            channels = liferay_client.get_available_channels()
            if not channels:
                return "No channels available in the system."
            
            channel_id = channels[0]['id']
            accounts = liferay_client.get_available_accounts(channel_id)
            
            account_found = None
            for account in accounts:
                account_name_lower = account.get('name', '').lower()
                if user_email.lower() in account_name_lower or account_name_lower in user_email.lower():
                    account_found = account
                    break
            
            if not account_found:
                # Fallback to test email mapping
                test_email_mapping = {
                    "john.smith@betavehicle.com": {"id": "36650", "name": "Beta Vehicle Supply"},
                    "sarah.jones@betavehicle.com": {"id": "36651", "name": "Sarah Jones"},
                    "mike.wilson@betavehicle.com": {"id": "36652", "name": "Mike Wilson"},
                    "lisa.brown@betavehicle.com": {"id": "36653", "name": "Lisa Brown"},
                    "david.garcia@betavehicle.com": {"id": "36654", "name": "David Garcia"}
                }
                
                if user_email.lower() in test_email_mapping:
                    account_found = test_email_mapping[user_email.lower()]
                    print(f"DEBUG: Found test account mapping - ID: {account_found['id']}, Name: {account_found['name']}")
            
            if not account_found:
                return f"**❌ Account not found**\n\nThe email '{user_email}' was not found in our system."
            
            account_id = account_found['id']
            account_name = account_found['name']
            print(f"DEBUG: Found account - ID: {account_id}, Name: {account_name}")
        
        # Get all orders for this account
        try:
            channels = liferay_client.get_available_channels()
            if not channels:
                return "No channels available in the system."
            
            channel_id = channels[0]['id']
            all_orders = liferay_client.get_placed_orders_by_account_limited(channel_id, account_id, max_orders=100)
            print(f"DEBUG: Found {len(all_orders)} total orders for account {account_id}")
            
        except Exception as e:
            print(f"DEBUG: Exception getting orders: {e}")
            return f"Error retrieving orders: {str(e)}"
        
        if not all_orders:
            return f"**No orders found** for {user_email}"
        
        # Enhanced search using shipments endpoint
        matching_orders = []
        address_query_lower = address_query.lower().strip()
        
        print(f"DEBUG: Searching for address containing: '{address_query}'")
        
        for order in all_orders:
            order_id = order.get('id')
            if not order_id:
                continue
            
            # Get shipments for this order (contains complete address info)
            shipments = liferay_client.get_order_shipments(str(order_id))
            
            for shipment in shipments:
                one_line_address = shipment.get('oneLineAddress', '')
                if not one_line_address:
                    continue
                
                # Check if address contains the query
                if address_query_lower in one_line_address.lower():
                    # Get detailed order information for additional context
                    detailed_order = liferay_client.get_direct_order_details(str(order_id))
                    
                    matching_orders.append({
                        'id': order_id,
                        'createDate': order.get('createDate', 'N/A'),
                        'oneLineAddress': one_line_address,
                        'shipmentStatus': shipment.get('status', {}).get('label', 'N/A'),
                        'shippingDate': shipment.get('shippingDate', 'N/A'),
                        'expectedDate': shipment.get('expectedDate', 'N/A'),
                        'trackingNumber': shipment.get('trackingNumber', 'N/A'),
                        'carrier': shipment.get('carrier', 'N/A'),
                        'totalFormatted': detailed_order.get('totalFormatted', 'N/A') if detailed_order else order.get('summary', {}).get('totalFormatted', 'N/A')
                    })
                    print(f"DEBUG: Found matching order {order_id} - Address: {one_line_address}")
                    break  # Only add each order once, even if it has multiple matching shipments
        
        # Sort orders by date (newest first)
        matching_orders.sort(key=lambda x: x.get('createDate', ''), reverse=True)
        
        # Format results
        if not matching_orders:
            return f"""
**🔍 No Orders Found**

No orders found with shipping address containing: **"{address_query}"**

**Customer**: {account_name}
**Address Search**: {address_query}

**💡 Try These Search Terms:**
- **City**: "New York", "Los Angeles", "Chicago", "Dallas"
- **State**: "CA", "NY", "TX", "California", "New York", "Texas"
- **Street**: "Main Street", "Oak Avenue", "123", "Joan"
- **Postal Code**: "90210", "10001", "60601", "75201"
- **Country**: "US", "United States", "Canada"
- **Partial matches**: "Main", "Ave", "St", "Pl"

**Examples:**
- "Search orders shipped to New York using your.email@example.com"
- "Find orders with address containing 'Main Street' for customer@company.com"
            """.strip()
        
        result = f"**🔍 Enhanced Address Search Results: {len(matching_orders)} orders with address containing '{address_query}'**\n"
        result += f"**Customer**: {account_name}\n"
        result += f"**Address Filter**: {address_query}\n\n"
        
        for i, order in enumerate(matching_orders, 1):
            order_id = order.get('id', 'N/A')
            order_date = order.get('createDate', 'N/A')
            one_line_address = order.get('oneLineAddress', 'N/A')
            shipment_status = order.get('shipmentStatus', 'N/A')
            total_amount = order.get('totalFormatted', 'N/A')
            shipping_date = order.get('shippingDate', 'N/A')
            tracking_number = order.get('trackingNumber', 'N/A')
            
            result += f"**{i}. Order #{order_id}**\n"
            result += f"   📅 **Date**: {order_date}\n"
            result += f"   📊 **Shipment Status**: {shipment_status.title()}\n"
            result += f"   💰 **Total**: {total_amount}\n"
            result += f"   📍 **Address**: {one_line_address}\n"
            if shipping_date != 'N/A':
                result += f"   🚚 **Shipped**: {shipping_date}\n"
            if tracking_number and tracking_number != 'N/A':
                result += f"   📦 **Tracking**: {tracking_number}\n"
            result += f"\n"
        
        result += f"**💡 For detailed shipping info, ask**: 'Get shipping info for order [ORDER_ID]'"
        
        return result.strip()
        
    except Exception as e:
        return f"Error retrieving orders: {str(e)}"

def search_orders_by_shipping_address(address_query: str, user_email: Optional[str] = None) -> str:
    """Enhanced search for orders by shipping address using shipments endpoint"""
    if not user_email:
        return """
**❌ User Identification Required**

To search for orders by shipping address, I need to know which customer you are.

**Please provide your email address:**
- "Search orders shipped to New York using your.email@example.com"
- "Find orders with address containing 'Main Street' for customer@company.com"
- "Show me orders shipped to California using myemail@domain.com"

**Available Address Search Options:**
- Street name or number
- City name
- State/Province
- Postal/ZIP code
- Country
- Partial address matches

**💡 Use the same email address you used when placing your orders.**
        """.strip()
    
    # Look up account information by email using Liferay API
    try:
        print(f"DEBUG: Looking up account for email: {user_email}")
        customer = liferay_client.get_customer_by_email(user_email)
        
        if customer:
            account_id = customer.id
            account_name = f"{customer.first_name} {customer.last_name}".strip() or customer.email
            print(f"DEBUG: Found customer - ID: {account_id}, Name: {account_name}")
        else:
            print("DEBUG: Customer not found, searching accounts by email...")
            channels = liferay_client.get_available_channels()
            if not channels:
                return "No channels available in the system."
            
            channel_id = channels[0]['id']
            accounts = liferay_client.get_available_accounts(channel_id)
            
            account_found = None
            for account in accounts:
                account_name_lower = account.get('name', '').lower()
                if user_email.lower() in account_name_lower or account_name_lower in user_email.lower():
                    account_found = account
                    break
            
            if not account_found:
                # Fallback to test email mapping
                test_email_mapping = {
                    "john.smith@betavehicle.com": {"id": "36650", "name": "Beta Vehicle Supply"},
                    "sarah.jones@betavehicle.com": {"id": "36651", "name": "Sarah Jones"},
                    "mike.wilson@betavehicle.com": {"id": "36652", "name": "Mike Wilson"},
                    "lisa.brown@betavehicle.com": {"id": "36653", "name": "Lisa Brown"},
                    "david.garcia@betavehicle.com": {"id": "36654", "name": "David Garcia"}
                }
                
                if user_email.lower() in test_email_mapping:
                    account_found = test_email_mapping[user_email.lower()]
                    print(f"DEBUG: Found test account mapping - ID: {account_found['id']}, Name: {account_found['name']}")
            
            if not account_found:
                return f"**❌ Account not found**\n\nThe email '{user_email}' was not found in our system."
            
            account_id = account_found['id']
            account_name = account_found['name']
            print(f"DEBUG: Found account - ID: {account_id}, Name: {account_name}")
        
        # Get all orders for the account
        channels = liferay_client.get_available_channels()
        if not channels:
            return "No channels available in the system."
        
        channel_id = channels[0]['id']
        all_orders = liferay_client.get_all_placed_orders_by_account(channel_id, account_id)
        print(f"DEBUG: Found {len(all_orders)} total orders for account {account_id}")
        
        if not all_orders:
            return f"**No orders found** for {user_email}"
        
        # Enhanced search using shipments endpoint
        matching_orders = []
        address_query_lower = address_query.lower().strip()
        
        print(f"DEBUG: Searching for address containing: '{address_query}'")
        
        for order in all_orders:
            order_id = order.get('id')
            if not order_id:
                continue
            
            # Get shipments for this order (contains complete address info)
            shipments = liferay_client.get_order_shipments(str(order_id))
            
            for shipment in shipments:
                one_line_address = shipment.get('oneLineAddress', '')
                if not one_line_address:
                    continue
                
                # Check if address contains the query
                if address_query_lower in one_line_address.lower():
                    # Get detailed order information for additional context
                    detailed_order = liferay_client.get_direct_order_details(str(order_id))
                    
                    matching_orders.append({
                        'id': order_id,
                        'createDate': order.get('createDate', 'N/A'),
                        'oneLineAddress': one_line_address,
                        'shipmentStatus': shipment.get('status', {}).get('label', 'N/A'),
                        'shippingDate': shipment.get('shippingDate', 'N/A'),
                        'expectedDate': shipment.get('expectedDate', 'N/A'),
                        'trackingNumber': shipment.get('trackingNumber', 'N/A'),
                        'carrier': shipment.get('carrier', 'N/A'),
                        'totalFormatted': detailed_order.get('totalFormatted', 'N/A') if detailed_order else order.get('summary', {}).get('totalFormatted', 'N/A')
                    })
                    print(f"DEBUG: Found matching order {order_id} - Address: {one_line_address}")
                    break  # Only add each order once, even if it has multiple matching shipments
        
        # Sort orders by date (newest first)
        matching_orders.sort(key=lambda x: x.get('createDate', ''), reverse=True)
        
        # Format results
        if not matching_orders:
            return f"""
**🔍 No Orders Found**

No orders found with shipping address containing: **"{address_query}"**

**Customer**: {account_name}
**Address Search**: {address_query}

**💡 Try These Search Terms:**
- **City**: "New York", "Los Angeles", "Chicago"
- **State**: "CA", "NY", "TX", "California", "New York"
- **Street**: "Main Street", "Oak Avenue", "123"
- **Postal Code**: "90210", "10001", "60601"
- **Country**: "US", "United States", "Canada"
- **Partial matches**: "Main", "Ave", "St"

**Examples:**
- "Search orders shipped to New York using your.email@example.com"
- "Find orders with address containing 'Main Street' for customer@company.com"
            """.strip()
        
        result = f"**🔍 Enhanced Address Search Results: {len(matching_orders)} orders with address containing '{address_query}'**\n"
        result += f"**Customer**: {account_name}\n"
        result += f"**Address Filter**: {address_query}\n\n"
        
        for i, order in enumerate(matching_orders, 1):
            order_id = order.get('id', 'N/A')
            order_date = order.get('createDate', 'N/A')
            one_line_address = order.get('oneLineAddress', 'N/A')
            shipment_status = order.get('shipmentStatus', 'N/A')
            total_amount = order.get('totalFormatted', 'N/A')
            shipping_date = order.get('shippingDate', 'N/A')
            tracking_number = order.get('trackingNumber', 'N/A')
            
            result += f"**{i}. Order #{order_id}**\n"
            result += f"   📅 **Date**: {order_date}\n"
            result += f"   📊 **Shipment Status**: {shipment_status.title()}\n"
            result += f"   💰 **Total**: {total_amount}\n"
            result += f"   📍 **Address**: {one_line_address}\n"
            if shipping_date != 'N/A':
                result += f"   🚚 **Shipped**: {shipping_date}\n"
            if tracking_number and tracking_number != 'N/A':
                result += f"   📦 **Tracking**: {tracking_number}\n"
            result += f"\n"
        
        result += f"**💡 For detailed shipping info, ask**: 'Get shipping info for order [ORDER_ID]'"
        
        return result.strip()
        
    except Exception as e:
        return f"Error retrieving orders: {str(e)}"

def search_orders_by_status_enhanced(order_status: str, user_email: Optional[str] = None) -> str:
    """Enhanced search for orders with a specific status using direct order details"""
    if not user_email:
        return """
**❌ User Identification Required**

To search for orders by status, I need to know which customer you are.

**Please provide your email address:**
- "Search my pending orders using your.email@example.com"
- "Find shipped orders for customer@company.com"
- "Show me canceled orders using myemail@domain.com"

**Available Status Options:**
- Canceled, Completed, On Hold, Partially Shipped, Pending, Processing, Shipped

**💡 Use the same email address you used when placing your orders.**
        """.strip()
    
    # Look up account information by email using Liferay API
    try:
        print(f"DEBUG: Looking up account for email: {user_email}")
        customer = liferay_client.get_customer_by_email(user_email)
        
        if customer:
            account_id = customer.id
            account_name = f"{customer.first_name} {customer.last_name}".strip() or customer.email
            print(f"DEBUG: Found customer - ID: {account_id}, Name: {account_name}")
        else:
            print("DEBUG: Customer not found, searching accounts by email...")
            channels = liferay_client.get_available_channels()
            if not channels:
                return "No channels available in the system."
            
            channel_id = channels[0]['id']
            accounts = liferay_client.get_available_accounts(channel_id)
            
            account_found = None
            for account in accounts:
                account_name_lower = account.get('name', '').lower()
                if user_email.lower() in account_name_lower or account_name_lower in user_email.lower():
                    account_found = account
                    break
            
            if not account_found:
                # Fallback to test email mapping
                test_email_mapping = {
                    "john.smith@betavehicle.com": {"id": "36650", "name": "Beta Vehicle Supply"},
                    "sarah.jones@betavehicle.com": {"id": "36651", "name": "Sarah Jones"},
                    "mike.wilson@betavehicle.com": {"id": "36652", "name": "Mike Wilson"},
                    "lisa.brown@betavehicle.com": {"id": "36653", "name": "Lisa Brown"},
                    "david.garcia@betavehicle.com": {"id": "36654", "name": "David Garcia"}
                }
                
                if user_email.lower() in test_email_mapping:
                    account_found = test_email_mapping[user_email.lower()]
                    print(f"DEBUG: Found test account mapping - ID: {account_found['id']}, Name: {account_found['name']}")
            
            if not account_found:
                return f"**❌ Account not found**\n\nThe email '{user_email}' was not found in our system."
            
            account_id = account_found['id']
            account_name = account_found['name']
            print(f"DEBUG: Found account - ID: {account_id}, Name: {account_name}")
        
        # Get all orders for this account
        try:
            channels = liferay_client.get_available_channels()
            if not channels:
                return "No channels available in the system."
            
            channel_id = channels[0]['id']
            all_orders = liferay_client.get_placed_orders_by_account_limited(channel_id, account_id, max_orders=100)
            print(f"DEBUG: Found {len(all_orders)} total orders for account {account_id}")
            
        except Exception as e:
            print(f"DEBUG: Exception getting orders: {e}")
            return f"Error retrieving orders: {str(e)}"
        
        if not all_orders:
            return f"**No orders found** for {user_email}"
        
        # Define status mappings for intelligent matching
        status_mappings = {
            'canceled': ['canceled', 'cancelled', 'cancel'],
            'completed': ['completed', 'complete', 'done'],
            'on hold': ['on hold', 'hold', 'on-hold', 'onhold'],
            'partially shipped': ['partially shipped', 'partial', 'partially', 'part shipped'],
            'pending': ['pending', 'pend', 'waiting'],
            'processing': ['processing', 'process', 'in process', 'in progress'],
            'shipped': ['shipped', 'shipping', 'delivered', 'out for delivery']
        }
        
        # Find the canonical status for the search term
        order_status_lower = order_status.lower().strip()
        canonical_status = None
        for canonical, variations in status_mappings.items():
            if order_status_lower in variations or any(variation in order_status_lower for variation in variations):
                canonical_status = canonical
                break
        
        if not canonical_status:
            canonical_status = order_status_lower
        
        print(f"DEBUG: Searching for status '{order_status}' -> canonical: '{canonical_status}'")
        
        # Enhanced search using direct order details
        matching_orders = []
        for order in all_orders:
            order_id = order.get('id')
            if not order_id:
                continue
                
            # Get detailed order information
            detailed_order = liferay_client.get_direct_order_details(str(order_id))
            if not detailed_order:
                print(f"DEBUG: Could not get detailed info for order {order_id}")
                continue
            
            # Check order status from detailed information
            order_status_info = detailed_order.get('orderStatusInfo', {})
            detailed_status = order_status_info.get('label', '').lower().strip()
            
            print(f"DEBUG: Order {order_id} detailed status: '{detailed_status}'")
            
            if canonical_status == detailed_status:
                matching_orders.append({
                    'id': order_id,
                    'createDate': order.get('createDate', 'N/A'),
                    'status': detailed_status,
                    'statusInfo': order_status_info,
                    'totalFormatted': detailed_order.get('totalFormatted', 'N/A'),
                    'externalReferenceCode': detailed_order.get('externalReferenceCode', 'N/A')
                })
                print(f"DEBUG: Found matching order {order_id} with status '{detailed_status}'")
        
        # Sort orders by date (newest first)
        matching_orders.sort(key=lambda x: x.get('createDate', ''), reverse=True)
        
        # Format results
        if not matching_orders:
            return f"""
**🔍 No Orders Found**

No orders found with status: **"{order_status}"**

**Customer**: {account_name}
**Search Status**: {order_status}

**💡 Available Status Options:**
- **Canceled** - Orders that have been cancelled
- **Completed** - Orders that are fully completed
- **On Hold** - Orders temporarily paused
- **Partially Shipped** - Orders with some items shipped
- **Pending** - Orders awaiting processing
- **Processing** - Orders currently being processed
- **Shipped** - Orders that have been shipped

**Try:**
- Use any of the status names above
- Partial names work too (e.g., "cancel" for "Canceled")
- Check available statuses with "Show me system status"
            """.strip()
        
        result = f"**🔍 Enhanced Status Search Results: {len(matching_orders)} orders with status '{order_status}'**\n"
        result += f"**Customer**: {account_name}\n"
        result += f"**Status Filter**: {order_status}\n\n"
        
        for i, order in enumerate(matching_orders, 1):
            order_id = order.get('id', 'N/A')
            order_date = order.get('createDate', 'N/A')
            order_status_value = order.get('status', 'N/A')
            total_amount = order.get('totalFormatted', 'N/A')
            status_info = order.get('statusInfo', {})
            
            result += f"**{i}. Order #{order_id}**\n"
            result += f"   📅 **Date**: {order_date}\n"
            result += f"   📊 **Status**: {order_status_value.title()}\n"
            if status_info.get('code') is not None:
                result += f"   🔢 **Status Code**: {status_info.get('code')}\n"
            result += f"   💰 **Total**: {total_amount}\n\n"
        
        result += f"**💡 For detailed order info, ask**: 'Find order [ORDER_ID]'"
        
        return result.strip()
        
    except Exception as e:
        return f"Error retrieving orders: {str(e)}"

def search_orders_by_status(order_status: str, user_email: Optional[str] = None) -> str:
    """Enhanced search for orders with a specific status using direct order details"""
    try:
        from datetime import datetime
        
        # Check if user email is provided
        if not user_email:
            return """
**❌ User Identification Required**

To search for orders by status, I need to know which customer you are.

**Please provide your email address:**
- "Search my pending orders using your.email@example.com"
- "Find shipped orders for customer@company.com"
- "Show me canceled orders using myemail@domain.com"

**Available Status Options:**
- Canceled, Completed, On Hold, Partially Shipped, Pending, Processing, Shipped

**💡 Use the same email address you used when placing your orders.**
            """.strip()
        
        # Look up account information by email using Liferay API
        try:
            print(f"DEBUG: Looking up account for email: {user_email}")
            
            # First try to find customer by email
            customer = liferay_client.get_customer_by_email(user_email)
            
            if customer:
                # If customer found, use their account ID
                account_id = customer.id
                account_name = f"{customer.first_name} {customer.last_name}".strip() or customer.email
                print(f"DEBUG: Found customer account - ID: {account_id}, Name: {account_name}")
            else:
                # If no customer found, try to find account by email in account list
                print(f"DEBUG: Customer not found, searching accounts by email...")
                
                # Get all accounts and search for one with matching email
                channels = liferay_client.get_available_channels()
                if not channels:
                    return "No channels available in the system."
                
                channel_id = channels[0]['id']
                accounts = liferay_client.get_available_accounts(channel_id)
                
                account_found = None
                for account in accounts:
                    # Check if account name or other fields contain the email
                    account_name = account.get('name', '').lower()
                    if user_email.lower() in account_name or account_name in user_email.lower():
                        account_found = account
                        break
                
                if not account_found:
                    # Try hardcoded mapping for test emails
                    test_email_mapping = {
                        "john.smith@betavehicle.com": {"id": "36650", "name": "Beta Vehicle Supply"},
                        "john.smith@nexusaccessories.com": {"id": "36558", "name": "Nexus Accessories"},
                        "john.smith@spiritsecurity.com": {"id": "36573", "name": "Spirit Security"},
                        "john.smith@dynamicfluids.com": {"id": "36584", "name": "Dynamic Fluids"},
                        "john.smith@sparkfabrication.com": {"id": "36595", "name": "Spark Fabrication"},
                        "john.smith@biogarage.com": {"id": "36606", "name": "Bio Garage"},
                        "john.smith@novalubricants.com": {"id": "36617", "name": "Nova Lubricants"},
                        "john.smith@crestviewinteriors.com": {"id": "36628", "name": "Crestview Interiors"},
                        "john.smith@velocityrecovery.com": {"id": "36639", "name": "Velocity Recovery"},
                        "john.smith@firstrestoration.com": {"id": "36661", "name": "First Restoration"},
                    }
                    
                    if user_email.lower() in test_email_mapping:
                        account_found = test_email_mapping[user_email.lower()]
                        print(f"DEBUG: Found test account mapping - ID: {account_found['id']}, Name: {account_found['name']}")
                
                if not account_found:
                    return f"""
**❌ Account not found**

The email '{user_email}' was not found in our system.

**Please check:**
- Email spelling and format
- If you have an account with us
- Contact customer support if you believe this is an error

**💡 Tip:** Make sure you're using the same email address you used when placing your orders.
                    """.strip()
                
                account_id = account_found['id']
                account_name = account_found['name']
                print(f"DEBUG: Found account - ID: {account_id}, Name: {account_name}")
            
        except Exception as e:
            print(f"DEBUG: Exception during account lookup: {e}")
            return f"""
**❌ Error looking up account**

There was an error looking up the email '{user_email}': {str(e)}

**Please try:**
- Check your email address spelling
- Contact customer support if the problem persists
            """.strip()
        
        # Get all orders for this account
        try:
            channels = liferay_client.get_available_channels()
            if not channels:
                return "No channels available in the system."
            
            channel_id = channels[0]['id']
            all_orders = liferay_client.get_placed_orders_by_account_limited(channel_id, account_id, max_orders=100)
            print(f"DEBUG: Found {len(all_orders)} total orders for account {account_id}")
            
        except Exception as e:
            print(f"DEBUG: Exception getting orders: {e}")
            return f"Error retrieving orders: {str(e)}"
        
        if not all_orders:
            return f"**No orders found** for {user_email}"
        
        # Filter orders by status with intelligent matching
        matching_orders = []
        order_status_lower = order_status.lower().strip()
        
        # Define status mappings for intelligent matching
        status_mappings = {
            'canceled': ['canceled', 'cancelled', 'cancel'],
            'completed': ['completed', 'complete', 'done'],
            'on hold': ['on hold', 'hold', 'on-hold', 'onhold'],
            'partially shipped': ['partially shipped', 'partial', 'partially', 'part shipped'],
            'pending': ['pending', 'pend', 'waiting'],
            'processing': ['processing', 'process', 'in process', 'in progress'],
            'shipped': ['shipped', 'shipping', 'delivered', 'out for delivery']
        }
        
        # Find the canonical status for the search term
        canonical_status = None
        for canonical, variations in status_mappings.items():
            if order_status_lower in variations or any(variation in order_status_lower for variation in variations):
                canonical_status = canonical
                break
        
        # If no mapping found, use the original search term
        if not canonical_status:
            canonical_status = order_status_lower
        
        print(f"DEBUG: Searching for status '{order_status}' -> canonical: '{canonical_status}'")
        
        # Enhanced search using direct order details
        for order in all_orders:
            order_id = order.get('id')
            if not order_id:
                continue
                
            # Get detailed order information
            detailed_order = liferay_client.get_direct_order_details(str(order_id))
            if not detailed_order:
                print(f"DEBUG: Could not get detailed info for order {order_id}")
                continue
            
            # Check order status from detailed information
            order_status_info = detailed_order.get('orderStatusInfo', {})
            detailed_status = order_status_info.get('label', '').lower().strip()
            
            print(f"DEBUG: Order {order_id} detailed status: '{detailed_status}'")
            
            if canonical_status == detailed_status:
                matching_orders.append({
                    'id': order_id,
                    'createDate': order.get('createDate', 'N/A'),
                    'status': detailed_status,
                    'statusInfo': order_status_info,
                    'totalFormatted': detailed_order.get('totalFormatted', 'N/A'),
                    'externalReferenceCode': detailed_order.get('externalReferenceCode', 'N/A')
                })
                print(f"DEBUG: Found matching order {order_id} with status '{detailed_status}'")
        
        # Sort orders by date (newest first)
        matching_orders.sort(key=lambda x: x.get('createDate', ''), reverse=True)
        
        # Format results
        if not matching_orders:
            return f"""
**🔍 No Orders Found**

No orders found with status: **"{order_status}"**

**Customer**: {account_name}
**Search Status**: {order_status}

**💡 Available Status Options:**
- **Canceled** - Orders that have been cancelled
- **Completed** - Orders that are fully completed
- **On Hold** - Orders temporarily paused
- **Partially Shipped** - Orders with some items shipped
- **Pending** - Orders awaiting processing
- **Processing** - Orders currently being processed
- **Shipped** - Orders that have been shipped

**Try:**
- Use any of the status names above
- Partial names work too (e.g., "cancel" for "Canceled")
- Check available statuses with "Show me system status"
            """.strip()
        
        result = f"**🔍 Enhanced Status Search Results: {len(matching_orders)} orders with status '{order_status}'**\n"
        result += f"**Customer**: {account_name}\n"
        result += f"**Status Filter**: {order_status}\n\n"
        
        for i, order in enumerate(matching_orders, 1):
            order_id = order.get('id', 'N/A')
            order_date = order.get('createDate', 'N/A')
            order_status_value = order.get('status', 'N/A')
            total_amount = order.get('totalFormatted', 'N/A')
            status_info = order.get('statusInfo', {})
            
            result += f"**{i}. Order #{order_id}**\n"
            result += f"   📅 **Date**: {order_date}\n"
            result += f"   📊 **Status**: {order_status_value.title()}\n"
            if status_info.get('code') is not None:
                result += f"   🔢 **Status Code**: {status_info.get('code')}\n"
            result += f"   💰 **Total**: {total_amount}\n\n"
        
        result += f"**💡 For detailed order info, ask**: 'Find order [ORDER_ID]'"
        
        return result
        
    except Exception as e:
        return f"Error searching orders by status: {str(e)}"

def get_available_test_emails() -> str:
    """Get information about customer email lookup"""
    return """
**Customer Email Lookup Information:**

**🔍 How Customer Lookup Works:**
- Enter any email address that exists in our system
- The system will automatically find the customer's account
- Orders will be retrieved for that specific customer only

**💡 How to Use:**
1. **Customer Order Lookup**: "Get my orders using your.email@example.com"
2. **Order Search**: "Search for orders customer@company.com"
3. **Date Range Search**: "Search orders from 2024-01-01 to 2024-01-31 using myemail@domain.com"

**🎯 What You Need:**
- A valid email address that exists in our customer database
- The same email address used when placing orders
- Proper email format (e.g., user@domain.com)

**❌ If Email Not Found:**
- Check spelling and format
- Verify the email exists in our system
- Contact customer support if needed
    """.strip()

def get_order_items(order_id: str) -> str:
    """Get detailed item information for a specific order"""
    try:
        # Get order items from Liferay API with better error handling
        try:
            items_response = liferay_client.get_placed_order_items(order_id)
        except Exception as api_error:
            return f"""
**🔍 Order Items Lookup Failed**

**Order ID**: {order_id}
**Error**: {str(api_error)}

**Possible Causes:**
1. The order may not have items in the system
2. The Liferay API endpoint may be unavailable
3. There may be an authentication issue

**💡 Try These Alternatives:**
- "Find order {order_id}" - Get complete order details
- "Search for orders" - Browse available orders
- Check if the order exists in the system
            """.strip()
        
        # Handle both list and dict responses
        if isinstance(items_response, list):
            order_items = items_response
        elif isinstance(items_response, dict) and 'items' in items_response:
            order_items = items_response['items']
        else:
            order_items = []
        
        if not order_items:
            return f"""
**📦 Order Items for Order {order_id}**

**Status**: Order exists but has no items
**Items Count**: 0

**💡 Try**: "Find order {order_id}" for complete order information
            """.strip()
        
        # Get order info directly using the delivery-order endpoint
        order_info = liferay_client.get_direct_order_details(order_id)
        
        # Build the response
        result = f"**📦 Order Items for Order {order_id}**\n\n"
        
        if order_info:
            external_ref = order_info.get('externalReferenceCode', 'N/A')
            create_date = order_info.get('createDate', 'N/A')
            total_amount = order_info.get('summary', {}).get('totalFormatted', 'N/A')
            result += f"**Order Details:** {external_ref} | Date: {create_date} | Total: {total_amount}\n\n"
            
            # Try to get shipping address from shipments API
            try:
                shipments = liferay_client.get_order_shipments(order_id)
                if shipments and len(shipments) > 0:
                    latest_shipment = shipments[0]
                    result += f"**📍 Shipping Address:**\n"
                    result += f"- **Address**: {latest_shipment.get('oneLineAddress', 'N/A')}\n"
                    result += f"- **Shipping Date**: {latest_shipment.get('shippingDate', 'N/A')}\n"
                    result += f"- **Tracking Number**: {latest_shipment.get('trackingNumber', 'N/A')}\n"
                    result += f"- **Carrier**: {latest_shipment.get('carrier', 'N/A')}\n"
                    status_info = latest_shipment.get('status', {})
                    if isinstance(status_info, dict):
                        result += f"- **Status**: {status_info.get('label', 'N/A')}\n\n"
                    else:
                        result += f"- **Status**: {status_info}\n\n"
                else:
                    result += f"**📍 Shipping Address**: Not available\n\n"
            except Exception as e:
                result += f"**📍 Shipping Address**: Not available\n\n"
        
        result += f"**Found {len(order_items)} items:**\n\n"
        
        for i, item in enumerate(order_items, 1):
            if not isinstance(item, dict):
                continue
            item_name = item.get('name', 'Unknown Product')
            item_sku = item.get('sku', 'N/A')
            item_quantity = item.get('quantity', 'N/A')
            item_price = item.get('finalPriceFormatted', 'N/A')
            
            result += f"{i}. **{item_name}**\n"
            result += f"   SKU: {item_sku} | Qty: {item_quantity} | Price: {item_price}\n"
            
            # Add more details if available
            if isinstance(item, dict) and item.get('options'):
                options = item.get('options', [])
                if isinstance(options, list):
                    option_names = []
                    for opt in options:
                        if isinstance(opt, dict):
                            option_names.append(opt.get('name', ''))
                    if option_names:
                        result += f"   Options: {', '.join(option_names)}\n"
            
            result += "\n"
        
        return result.strip()
        
    except Exception as e:
        return f"""
**❌ Unexpected Error**

**Order ID**: {order_id}
**Error**: {str(e)}
**Error Type**: {type(e).__name__}

**💡 Try These Alternatives:**
- "Find order {order_id}" - Get complete order details
- "Search for orders" - Browse available orders
- Check system status with "Show me system status"
        """.strip()

def get_order_shipping(order_id: str) -> str:
    """Get shipping information for a specific order with enhanced address details"""
    try:
        # Get order details directly using the delivery-order endpoint
        order_info = liferay_client.get_direct_order_details(order_id)
        if not order_info:
            return f"Order {order_id} not found or not accessible."
        
        # Extract shipping information
        summary = order_info.get('summary', {})
        shipping_value = summary.get('shippingValueFormatted', 'N/A')
        shipping_discount = summary.get('shippingDiscountValueFormatted', 'N/A')
        subtotal = summary.get('subtotalFormatted', 'N/A')
        tax_value = summary.get('taxValueFormatted', 'N/A')
        total = summary.get('totalFormatted', 'N/A')
        
        # Get enhanced shipping address from shipments API
        shipments = liferay_client.get_order_shipments(order_id)
        shipping_address_info = None
        if shipments:
            # Get the most recent shipment for address details
            latest_shipment = shipments[0]  # Shipments are typically sorted by date
            shipping_address_info = {
                'one_line_address': latest_shipment.get('oneLineAddress', 'N/A'),
                'shipping_date': latest_shipment.get('shippingDate', 'N/A'),
                'tracking_number': latest_shipment.get('trackingNumber', 'N/A'),
                'carrier': latest_shipment.get('carrier', 'N/A'),
                'status': latest_shipment.get('status', 'N/A')
            }
        
        # Get basic shipping address from order (fallback)
        shipping_address = order_info.get('shippingAddress', {})
        billing_address = order_info.get('billingAddress', {})
        
        # Build the response
        result = f"**🚚 Shipping Information for Order {order_id}**\n\n"
        
        # Order summary
        result += f"**Order Summary:**\n"
        result += f"- **Subtotal**: {subtotal}\n"
        result += f"- **Shipping Cost**: {shipping_value}\n"
        if shipping_discount != 'N/A' and shipping_discount != '$ 0.00':
            result += f"- **Shipping Discount**: {shipping_discount}\n"
        result += f"- **Tax**: {tax_value}\n"
        result += f"- **Total**: {total}\n\n"
        
        # Enhanced shipping address from shipments
        if shipping_address_info and shipping_address_info.get('one_line_address') != 'N/A':
            result += f"**📍 Shipping Address (from Shipments):**\n"
            result += f"- **Address**: {shipping_address_info['one_line_address']}\n"
            result += f"- **Shipping Date**: {shipping_address_info['shipping_date']}\n"
            result += f"- **Tracking Number**: {shipping_address_info['tracking_number']}\n"
            result += f"- **Carrier**: {shipping_address_info['carrier']}\n"
            result += f"- **Status**: {shipping_address_info['status']}\n\n"
        elif shipping_address:
            result += f"**📍 Shipping Address (from Order):**\n"
            result += f"- **Name**: {shipping_address.get('name', 'N/A')}\n"
            result += f"- **Street**: {shipping_address.get('street1', 'N/A')}\n"
            if shipping_address.get('street2'):
                result += f"- **Street 2**: {shipping_address.get('street2')}\n"
            result += f"- **City**: {shipping_address.get('city', 'N/A')}\n"
            result += f"- **State/Province**: {shipping_address.get('regionISOCode', 'N/A')}\n"
            result += f"- **Postal Code**: {shipping_address.get('zip', 'N/A')}\n"
            result += f"- **Country**: {shipping_address.get('countryISOCode', 'N/A')}\n"
            result += f"- **Phone**: {shipping_address.get('phoneNumber', 'N/A')}\n\n"
        else:
            result += f"**📍 Shipping Address**: Not available\n\n"
        
        # Billing address
        if billing_address:
            result += f"**💳 Billing Address:**\n"
            result += f"- **Name**: {billing_address.get('name', 'N/A')}\n"
            result += f"- **Street**: {billing_address.get('street1', 'N/A')}\n"
            if billing_address.get('street2'):
                result += f"- **Street 2**: {billing_address.get('street2')}\n"
            result += f"- **City**: {billing_address.get('city', 'N/A')}\n"
            result += f"- **State/Province**: {billing_address.get('regionISOCode', 'N/A')}\n"
            result += f"- **Postal Code**: {billing_address.get('zip', 'N/A')}\n"
            result += f"- **Country**: {billing_address.get('countryISOCode', 'N/A')}\n"
            result += f"- **Phone**: {billing_address.get('phoneNumber', 'N/A')}\n\n"
        else:
            result += f"**💳 Billing Address**: Not available\n\n"
        
        # Additional shipping info
        result += f"**📋 Additional Information:**\n"
        result += f"- **Order Date**: {order_info.get('createDate', 'N/A')}\n"
        result += f"- **Order Status**: {order_info.get('status', 'N/A')}\n"
        result += f"- **Order Reference**: {order_info.get('externalReferenceCode', 'N/A')}\n"
        
        return result.strip()
        
    except Exception as e:
        return f"""
**❌ Error Retrieving Shipping Information**

**Order ID**: {order_id}
**Error**: {str(e)}
**Error Type**: {type(e).__name__}

**💡 Try These Alternatives:**
- "Find order {order_id}" - Get complete order details
- "Get order items for order {order_id}" - View order items
- "Search for orders" - Browse available orders
        """.strip()

def debug_customer_and_orders(email: str) -> str:
    """Debug tool to check customer lookup and order retrieval for a specific email"""
    try:
        import os
        import requests
        
        # Get configuration
        base_url = os.getenv('LIFERAY_BASE_URL', 'http://localhost:8080')
        username = os.getenv('LIFERAY_USERNAME', 'test@liferay.com')
        password = os.getenv('LIFERAY_PASSWORD', 'test')
        ssl_verify = os.getenv('LIFERAY_SSL_VERIFY', 'True').lower() == 'true'
        
        debug_info = f"""
**Customer and Order Debug for: {email}**
- Base URL: {base_url}
- Username: {username}
- SSL Verify: {ssl_verify}

**Step 1: Customer Lookup**
        """.strip()
        
        # Test customer lookup
        try:
            session = requests.Session()
            session.auth = (username, password)
            session.verify = ssl_verify
            session.timeout = int(os.getenv('LIFERAY_TIMEOUT', '30'))
            
            # Test customer lookup
            customer_url = f"{base_url.rstrip('/')}/o/headless-admin-user/v1.0/user-accounts"
            customer_params = {"filter": f"emailAddress eq '{email}'"}
            
            debug_info += f"\n\n**Customer URL:** {customer_url}"
            debug_info += f"\n**Customer Params:** {customer_params}"
            
            customer_response = session.get(customer_url, params=customer_params)
            debug_info += f"\n**Customer Response Status:** {customer_response.status_code}"
            debug_info += f"\n**Customer Response:** {customer_response.text[:500]}..."
            
            if customer_response.status_code == 200:
                customer_data = customer_response.json()
                customers = customer_data.get('items', [])
                if customers:
                    customer = customers[0]
                    customer_id = customer.get('id')
                    debug_info += f"\n\n**✅ Customer Found:**"
                    debug_info += f"\n- ID: {customer_id}"
                    debug_info += f"\n- Name: {customer.get('givenName', '')} {customer.get('familyName', '')}"
                    debug_info += f"\n- Email: {customer.get('emailAddress', '')}"
                    
                    # Test order lookup
                    debug_info += f"\n\n**Step 2: Order Lookup**"
                    orders_url = f"{base_url.rstrip('/')}/o/headless-commerce-admin-order/v1.0/orders"
                    orders_params = {
                        "filter": f"accountId eq {customer_id}",
                        "pageSize": 100
                    }
                    
                    debug_info += f"\n**Orders URL:** {orders_url}"
                    debug_info += f"\n**Orders Params:** {orders_params}"
                    
                    orders_response = session.get(orders_url, params=orders_params)
                    debug_info += f"\n**Orders Response Status:** {orders_response.status_code}"
                    debug_info += f"\n**Orders Response:** {orders_response.text[:500]}..."
                    
                    if orders_response.status_code == 200:
                        orders_data = orders_response.json()
                        orders = orders_data.get('items', [])
                        debug_info += f"\n\n**✅ Orders Found:** {len(orders)} orders"
                        if orders:
                            for i, order in enumerate(orders[:3], 1):
                                debug_info += f"\n{i}. Order ID: {order.get('id')}, Status: {order.get('orderStatus')}"
                        else:
                            debug_info += f"\n\n**❌ No orders found for this customer**"
                    else:
                        debug_info += f"\n\n**❌ Order lookup failed with status {orders_response.status_code}**"
                else:
                    debug_info += f"\n\n**❌ No customer found with email {email}**"
            else:
                debug_info += f"\n\n**❌ Customer lookup failed with status {customer_response.status_code}**"
                
        except Exception as e:
            debug_info += f"\n\n**❌ Error:** {str(e)}"
            
        return debug_info
        
    except Exception as e:
        return f"❌ Debug tool error: {str(e)}"

def debug_liferay_connection() -> str:
    """Debug tool to check Liferay connection details and show exact error"""
    try:
        import os
        import requests
        
        # Get configuration
        base_url = os.getenv('LIFERAY_BASE_URL', 'http://localhost:8080')
        username = os.getenv('LIFERAY_USERNAME', 'test@liferay.com')
        password = os.getenv('LIFERAY_PASSWORD', 'test')
        ssl_verify = os.getenv('LIFERAY_SSL_VERIFY', 'True').lower() == 'true'
        
        debug_info = f"""
**Liferay Connection Debug Info:**
- Base URL: {base_url}
- Username: {username}
- Password: {'*' * len(password) if password else 'Not set'}
- SSL Verify: {ssl_verify}
- Timeout: {os.getenv('LIFERAY_TIMEOUT', '30')}

**Testing connection...**
        """.strip()
        
        # Test basic connectivity
        try:
            session = requests.Session()
            session.auth = (username, password)
            session.verify = ssl_verify
            session.timeout = int(os.getenv('LIFERAY_TIMEOUT', '30'))
            
            # Test the exact endpoint that's failing
            test_url = f"{base_url.rstrip('/')}/o/headless-admin-user/v1.0/my-user-account"
            debug_info += f"\n\n**Testing URL:** {test_url}"
            
            response = session.get(test_url)
            debug_info += f"\n**Response Status:** {response.status_code}"
            debug_info += f"\n**Response Headers:** {dict(response.headers)}"
            debug_info += f"\n**Response Text:** {response.text[:500]}..."
            
            if response.status_code == 200:
                debug_info += "\n\n✅ **Connection successful!**"
            else:
                debug_info += f"\n\n❌ **Connection failed with status {response.status_code}**"
                
        except requests.exceptions.ConnectionError as e:
            debug_info += f"\n\n❌ **Connection Error:** {str(e)}"
        except requests.exceptions.Timeout as e:
            debug_info += f"\n\n❌ **Timeout Error:** {str(e)}"
        except Exception as e:
            debug_info += f"\n\n❌ **Unexpected Error:** {str(e)}"
            
        return debug_info
        
    except Exception as e:
        return f"❌ Debug tool error: {str(e)}"

def test_external_api_access() -> str:
    """Test if the deployed agent can access external APIs"""
    try:
        import requests
        print("Testing external API access...")
        
        # Test with a simple public API
        response = requests.get('https://httpbin.org/ip', timeout=10)
        
        if response.status_code == 200:
            return f"✅ External API access is working! Response: {response.text}"
        else:
            return f"❌ External API access failed with status: {response.status_code}"
            
    except requests.exceptions.ConnectionError as e:
        return f"❌ Connection Error - No internet access: {str(e)}"
    except requests.exceptions.Timeout as e:
        return f"❌ Timeout Error - Network issues: {str(e)}"
    except Exception as e:
        return f"❌ Unexpected error: {str(e)}"

def get_faq_information(query: str = "") -> str:
    """Get frequently asked questions and answers from the official FAQ"""
    try:
        # Official FAQ content from https://webserver-lct66degrees-uat.lfr.cloud/web/minium-demo/faq
        faq_data = {
            "ordering": {
                "How do I place an order?": "To place an order, browse our selection by vehicle make/model, part category, or using our search bar. Add the desired items to your cart, then proceed to checkout. Follow the prompts to enter your shipping information and payment details to complete your purchase.",
                "Do I need an account to place an order?": "No, you can check out as a guest. However, creating an account allows you to track your order history, save multiple shipping addresses, and enjoy a faster checkout process on future purchases."
            },
            "payment": {
                "What payment methods do you accept?": "We accept all major credit cards (Visa, MasterCard, American Express, Discover), PayPal, and Google Pay. All transactions are securely processed.",
                "Is my payment information secure?": "Absolutely. We use industry-standard SSL encryption and PCI-compliant payment gateways to protect your personal and payment information. Your data is never stored on our servers."
            },
            "shipping": {
                "What are your shipping options and costs?": "We offer several shipping options, including Standard, Expedited, and Overnight delivery. Shipping costs are calculated at checkout based on your location, the weight/size of your order, and the chosen shipping speed.",
                "How long will it take for my order to arrive?": "Standard Shipping: Typically 3-7 business days. Expedited Shipping: Typically 2-3 business days. Overnight Shipping: 1 business day (orders must be placed by 2 PM PST for same-day dispatch). Please note that these are estimates and may vary based on product availability and carrier delays.",
                "Do you ship internationally?": "Yes, we ship to select international destinations. International shipping costs and delivery times vary significantly. Please enter your address at checkout to see available options and costs for your country. Customers are responsible for all customs duties, taxes, and fees.",
                "How can I track my order?": "Once your order ships, you will receive a shipping confirmation email with a tracking number. You can click on the link in the email or enter your tracking number on our 'Track Your Order' page."
            },
            "order_management": {
                "Can I change or cancel my order after it's placed?": "We process orders quickly to ensure fast delivery. If you need to change or cancel, please contact us immediately by phone or email. We will do our best to accommodate your request if the order has not yet been shipped.",
                "What if my package is lost or damaged?": "Please contact our customer support within 48 hours of the expected delivery date for lost packages, or immediately upon receipt for damaged items. We will initiate a claim with the carrier and arrange for a replacement or refund as quickly as possible."
            },
            "returns": {
                "What is your return policy?": "We offer a 30-day return policy for most unused parts in their original, unopened packaging. Some exceptions apply (e.g., electrical components, custom orders, final sale items). Please see our full Return Policy for complete details.",
                "How do I return a part?": "To initiate a return, please visit our Returns Portal or contact customer support to receive an RMA (Return Merchandise Authorization) number and detailed instructions. Do not send items back without an RMA.",
                "How long does it take to process a refund?": "Once we receive your returned item and inspect it, refunds are typically processed within 5-7 business days. The refund will be issued to your original payment method. Please note that it may take additional time for the refund to appear on your bank statement.",
                "Are there any non-returnable items?": "Yes, certain items are non-returnable for safety or hygiene reasons, or if they are custom-made or marked as 'final sale.' This often includes used parts, opened electrical components, or parts that have been installed. Please refer to our full Return Policy for the complete list."
            },
            "parts": {
                "How do I find the right part for my vehicle?": "You can use our 'Vehicle Selector' tool on the homepage by entering your Year, Make, and Model. Our search results will then filter for compatible parts. You can also search by VIN number, OEM part number, or part name.",
                "What if I can't find the part I need?": "If you're having trouble locating a specific part, please contact our parts specialists. Provide your vehicle's VIN and as much detail about the part as possible, and we'll do our best to help you find it or suggest alternatives.",
                "Are your parts new or used?": "Unless explicitly stated otherwise (e.g., in a 'Used Parts' or 'Salvage' section), all products sold on our website are brand new from the manufacturer.",
                "Do your parts come with a warranty?": "Many of our parts come with a manufacturer's warranty. Warranty terms vary by manufacturer and part. Please check the individual product page for specific warranty information. For warranty claims, please contact our support team.",
                "Do you offer technical support for installation?": "While we sell parts, we are not certified mechanics and cannot provide specific installation advice or instructions. We recommend consulting a qualified mechanic or referring to your vehicle's service manual for proper installation procedures.",
                "Do you provide installation instructions?": "Some manufacturers include basic installation guides with their parts. However, for detailed instructions, we strongly advise referring to your vehicle's factory service manual or seeking professional automotive assistance."
            },
            "account": {
                "How do I reset my password?": "Click on the 'Login' button at the top of the page, then click 'Forgot Password?'. Enter your registered email address, and we'll send you a link to reset your password.",
                "How do I update my account information?": "Log in to your account, and navigate to the 'My Account' or 'Account Settings' section. From there, you can update your personal details, shipping addresses, and payment methods."
            },
            "support": {
                "How can I contact customer service?": "You can reach us by: **Phone:** [Your Phone Number] (Mon-Fri, [Hours of Operation]) **Email:** [Your Support Email] (We aim to respond within 24 business hours) **Live Chat:** Available on our website during business hours.",
                "Do you offer a trade discount for mechanics/shops?": "Yes, we offer special pricing and programs for registered automotive businesses and mechanics. Please visit our 'Trade Program' page or contact our B2B sales team for more information."
            },
            "general": {
                "Can I pick up my order in person?": "No, currently all orders are processed and shipped from our distribution centers. We do not offer local pickup services."
            }
        }
        
        if not query:
            # Return all FAQ categories
            result = "**📚 Frequently Asked Questions**\n\n"
            result += "Here are the main categories of questions we can help with:\n\n"
            
            for category, questions in faq_data.items():
                category_name = category.replace('_', ' ').title()
                result += f"**{category_name}:**\n"
                for question in questions.keys():
                    result += f"- {question}\n"
                result += "\n"
            
            result += "**💡 How to use:** Ask me about any of these topics, and I'll provide the official answer!\n"
            result += "**Example:** 'What is your return policy?' or 'How do I track my order?'"
            
            return result
        
        # Search for relevant FAQ entries
        query_lower = query.lower()
        matching_answers = []
        
        for category, questions in faq_data.items():
            for question, answer in questions.items():
                # Check if query matches question or answer
                if (query_lower in question.lower() or 
                    any(word in answer.lower() for word in query_lower.split() if len(word) > 3)):
                    matching_answers.append({
                        'question': question,
                        'answer': answer,
                        'category': category.replace('_', ' ').title()
                    })
        
        if not matching_answers:
            return f"""
**❓ FAQ Search Results**

I couldn't find specific information about "{query}" in our FAQ database.

**💡 Try these alternatives:**
- Ask about specific topics like "return policy", "shipping", "payment", etc.
- Use "Get FAQ information" to see all available topics
- Contact customer support for specific questions

**Popular topics:**
- Ordering and checkout
- Shipping and delivery
- Returns and refunds
- Parts and compatibility
- Account management
            """.strip()
        
        # Format results
        result = f"**📚 FAQ Search Results for: '{query}'**\n\n"
        
        for i, match in enumerate(matching_answers[:5], 1):  # Limit to 5 results
            result += f"**{i}. {match['question']}**\n"
            result += f"*Category: {match['category']}*\n"
            result += f"{match['answer']}\n\n"
        
        if len(matching_answers) > 5:
            result += f"... and {len(matching_answers) - 5} more results.\n\n"
        
        result += "**💡 Source:** [Official FAQ](https://webserver-lct66degrees-uat.lfr.cloud/web/minium-demo/faq)"
        
        return result.strip()
        
    except Exception as e:
        return f"**❌ Error retrieving FAQ information**: {str(e)}"

def get_all_accounts_order_summary() -> str:
    """Get order summary for all available accounts"""
    try:
        # Auto-discover available channels and accounts
        channels = liferay_client.get_available_channels()
        if not channels:
            return "No channels available in the system."
        
        channel_id = channels[0]['id']
        accounts = liferay_client.get_available_accounts(channel_id)
        
        if not accounts:
            return f"No accounts available for channel {channel_id}."
        
        # Get order counts for all accounts
        account_summaries = []
        total_orders = 0
        
        for account in accounts:
            try:
                account_id = account['id']
                account_name = account.get('name', 'Unknown Account')
                account_type = account.get('type', 'N/A')
                
                # Get orders for this account
                orders = liferay_client.get_all_placed_orders_by_account(channel_id, account_id)
                order_count = len(orders) if orders else 0
                total_orders += order_count
                
                # Get sample order info if available
                sample_order = None
                if orders:
                    sample_order = orders[0]
                
                account_summaries.append({
                    'id': account_id,
                    'name': account_name,
                    'type': account_type,
                    'order_count': order_count,
                    'sample_order': sample_order
                })
                
            except Exception as e:
                # If we can't get orders for this account, continue
                account_summaries.append({
                    'id': account['id'],
                    'name': account.get('name', 'Unknown Account'),
                    'type': account.get('type', 'N/A'),
                    'order_count': 0,
                    'sample_order': None,
                    'error': str(e)
                })
        
        # Sort by order count (highest first)
        account_summaries.sort(key=lambda x: x['order_count'], reverse=True)
        
        # Build the response
        result = f"**📊 Order Summary for All Accounts**\n\n"
        result += f"**Channel**: {channels[0].get('name', 'N/A')} (ID: {channel_id})\n"
        result += f"**Total Accounts**: {len(accounts)}\n"
        result += f"**Total Orders**: {total_orders}\n\n"
        
        result += f"**🏢 Account Breakdown:**\n\n"
        
        for i, summary in enumerate(account_summaries, 1):
            result += f"{i}. **{summary['name']}** (ID: {summary['id']})\n"
            result += f"   **Type**: {summary['type']}\n"
            result += f"   **Orders**: {summary['order_count']}\n"
            
            # Add sample order info if available
            if summary['sample_order']:
                sample = summary['sample_order']
                sample_date = sample.get('createDate', 'N/A')
                sample_total = sample.get('summary', {}).get('totalFormatted', 'N/A')
                result += f"   **Sample Order**: {sample.get('id', 'N/A')} - {sample_date} - {sample_total}\n"
            
            # Add error info if there was an issue
            if 'error' in summary:
                result += f"   **⚠️ Error**: {summary['error']}\n"
            
            result += "\n"
        
        # Add summary statistics
        active_accounts = len([s for s in account_summaries if s['order_count'] > 0])
        inactive_accounts = len([s for s in account_summaries if s['order_count'] == 0])
        
        result += f"**📈 Summary Statistics:**\n"
        result += f"- **Active Accounts** (with orders): {active_accounts}\n"
        result += f"- **Inactive Accounts** (no orders): {inactive_accounts}\n"
        result += f"- **Average Orders per Account**: {total_orders / len(accounts) if accounts else 0:.1f}\n"
        
        return result.strip()
        
    except Exception as e:
        return f"""
**❌ Error Retrieving Account Summary**

**Error**: {str(e)}
**Error Type**: {type(e).__name__}

**💡 Try These Alternatives:**
- "Show me system status" - Basic system information
- "What channels and accounts are available?" - List available resources
- Check individual accounts one by one
        """.strip()

# Create FunctionTool instances for ADK using CompatibleFunctionTool for deployed environment compatibility
# Tools are ordered by performance in agent_tools.py for optimal routing
get_system_status_tool = CompatibleFunctionTool(get_system_status)
list_channels_accounts_tool = CompatibleFunctionTool(list_available_channels_and_accounts)
find_order_tool = CompatibleFunctionTool(find_order)
search_orders_tool = CompatibleFunctionTool(search_orders)
search_orders_by_date_range_tool = CompatibleFunctionTool(search_orders_by_date_range)
search_orders_by_product_tool = CompatibleFunctionTool(search_orders_by_product)
search_orders_by_status_tool = CompatibleFunctionTool(search_orders_by_status)
search_orders_by_shipping_address_tool = CompatibleFunctionTool(search_orders_by_shipping_address)
get_customer_orders_tool = CompatibleFunctionTool(get_customer_orders)
get_test_emails_tool = CompatibleFunctionTool(get_available_test_emails)
get_order_items_tool = CompatibleFunctionTool(get_order_items)
get_order_shipping_tool = CompatibleFunctionTool(get_order_shipping)
get_all_accounts_summary_tool = CompatibleFunctionTool(get_all_accounts_order_summary)
get_faq_information_tool = CompatibleFunctionTool(get_faq_information)
test_external_api_tool = CompatibleFunctionTool(test_external_api_access)
debug_liferay_connection_tool = CompatibleFunctionTool(debug_liferay_connection)
debug_customer_and_orders_tool = CompatibleFunctionTool(debug_customer_and_orders)
