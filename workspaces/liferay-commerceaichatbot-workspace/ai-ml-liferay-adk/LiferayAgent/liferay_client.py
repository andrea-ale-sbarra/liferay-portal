import requests
import json
from typing import Dict, List, Optional, Any
from dataclasses import dataclass
from datetime import datetime
import os
from dotenv import load_dotenv

load_dotenv()

@dataclass
class OrderItem:
    """Represents an item within an order"""
    id: str
    name: str
    sku: str
    quantity: int
    unit_price: float
    total_price: float
    status: str

@dataclass
class Order:
    """Represents a customer order"""
    id: str
    order_number: str
    customer_id: str
    customer_name: str
    order_date: datetime
    status: str
    total_amount: float
    items: List[OrderItem]
    shipping_address: Dict[str, str]
    billing_address: Dict[str, str]

@dataclass
class Customer:
    """Represents customer information"""
    id: str
    email: str
    first_name: str
    last_name: str
    phone: str
    address: Dict[str, str]

class LiferayAPIClient:
    """Client for interacting with Liferay Commerce APIs"""
    
    def __init__(self, base_url: str = None, username: str = None, password: str = None):
        
        # self.base_url = base_url or os.getenv('LIFERAY_BASE_URL', 'http://localhost:8080')
        # self.username = username or os.getenv('LIFERAY_USERNAME', 'test@liferay.com')
        # self.password = password or os.getenv('LIFERAY_PASSWORD', 'test')

        self.base_url = base_url or os.getenv('LIFERAY_BASE_URL', 'https://webserver-lct66degrees-uat.lfr.cloud/')
        self.username = username or os.getenv('LIFERAY_USERNAME', 'test@liferay.com')
        self.password = password or os.getenv('LIFERAY_PASSWORD', '3tcEDmFlp84LgAUeuEZAy00ED0S5FT')
        
        print(f"🔧 LiferayAPIClient - Final values:")
        print(f"  base_url: {self.base_url}")
        print(f"  username: {self.username}")
        print(f"  password: {'*' * len(self.password) if self.password else 'NOT_SET'}")
        self.session = requests.Session()
        
        # Configure session for better reliability
        self.session.timeout = int(os.getenv('LIFERAY_TIMEOUT', '30'))
        
        # Handle SSL verification - disable for self-signed certificates
        # In production, you might want to enable this
        self.session.verify = os.getenv('LIFERAY_SSL_VERIFY', 'True').lower() == 'true'
        
        # Add headers for better compatibility
        self.session.headers.update({
            'User-Agent': 'Liferay-Customer-Service-Agent/1.0',
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        })
        
        # Don't authenticate during initialization - do it lazily when needed
        self._authenticated = False
    
    def _ensure_authenticated(self):
        """Ensure we're authenticated before making API calls"""
        if not self._authenticated:
            self._authenticate()
            self._authenticated = True
    
    def _authenticate(self):
        """Authenticate with Liferay using basic auth"""
        self.session.auth = (self.username, self.password)
        # Test authentication
        try:
            print(f"🔐 Testing authentication to: {self.base_url}")
            print(f"👤 Username: {self.username}")
            response = self.session.get(f"{self.base_url.rstrip('/')}/o/headless-admin-user/v1.0/my-user-account")
            print(f"📡 Response status: {response.status_code}")
            
            if response.status_code != 200:
                print(f"❌ Authentication failed: {response.status_code}")
                print(f"📄 Response text: {response.text[:200]}...")
                raise Exception(f"Authentication failed: {response.status_code} - {response.text[:100]}")
            else:
                print("✅ Authentication successful!")
                
        except requests.exceptions.SSLError as e:
            print(f"🔒 SSL Error: {e}")
            print("💡 Try setting LIFERAY_SSL_VERIFY=False in your environment")
            raise Exception(f"SSL Error: {e}")
        except requests.exceptions.ConnectionError as e:
            print(f"🌐 Connection Error: {e}")
            print(f"💡 Check if {self.base_url} is accessible from the deployed environment")
            raise Exception(f"Connection Error: {e}")
        except requests.exceptions.Timeout as e:
            print(f"⏰ Timeout Error: {e}")
            print("💡 Try increasing LIFERAY_TIMEOUT in your environment")
            raise Exception(f"Timeout Error: {e}")
        except Exception as e:
            print(f"❌ Authentication error: {e}")
            print(f"🔧 Debug info - URL: {self.base_url}, Username: {self.username}")
            raise Exception(f"Authentication failed: {e}")
    
    def get_orders_by_customer_email(self, email: str) -> List[Order]:
        """Retrieve orders for a customer by email address"""
        self._ensure_authenticated()
        try:
            # First find the customer by email
            customer = self.get_customer_by_email(email)
            if not customer:
                return []
            
            # Get orders for the customer
            response = self.session.get(
                f"{self.base_url.rstrip('/')}/o/headless-commerce-admin-order/v1.0/orders",
                params={
                    "filter": f"accountId eq {customer.id}",
                    "pageSize": 100
                }
            )
            
            if response.status_code != 200:
                return []
            
            orders_data = response.json().get('items', [])
            orders = []
            
            for order_data in orders_data:
                order = self._parse_order(order_data)
                if order:
                    orders.append(order)
            
            return orders
            
        except Exception as e:
            print(f"Error retrieving orders: {e}")
            return []
    
    def get_orders_by_order_number(self, order_number: str) -> Optional[Order]:
        """Retrieve a specific order by order number"""
        self._ensure_authenticated()
        try:
            response = self.session.get(
                f"{self.base_url.rstrip('/')}/o/headless-commerce-admin-order/v1.0/orders",
                params={"filter": f"orderNumber eq '{order_number}'"}
            )
            
            if response.status_code != 200:
                return None
            
            orders_data = response.json().get('items', [])
            if not orders_data:
                return None
            
            return self._parse_order(orders_data[0])
            
        except Exception as e:
            print(f"Error retrieving order: {e}")
            return None
    
    def get_customer_by_email(self, email: str) -> Optional[Customer]:
        """Retrieve customer information by email address"""
        self._ensure_authenticated()
        try:
            response = self.session.get(
                f"{self.base_url.rstrip('/')}/o/headless-admin-user/v1.0/user-accounts",
                params={"filter": f"emailAddress eq '{email}'"}
            )
            
            if response.status_code != 200:
                return None
            
            users_data = response.json().get('items', [])
            if not users_data:
                return None
            
            user_data = users_data[0]
            return Customer(
                id=user_data.get('id'),
                email=user_data.get('emailAddress'),
                first_name=user_data.get('givenName', ''),
                last_name=user_data.get('familyName', ''),
                phone=user_data.get('phoneNumbers', [{}])[0].get('phoneNumber', ''),
                address={}
            )
            
        except Exception as e:
            print(f"Error retrieving customer: {e}")
            return None
    
    def get_order_details(self, order_id: str) -> Optional[Order]:
        """Retrieve detailed order information including items using delivery order endpoint"""
        self._ensure_authenticated()
        try:
            # Get order details using delivery-order endpoint
            response = self.session.get(
                f"{self.base_url.rstrip('/')}/o/headless-commerce-delivery-order/v1.0/placed-orders/{order_id}"
            )
            
            if response.status_code != 200:
                return None
            
            order_data = response.json()
            return self._parse_order(order_data)
            
        except Exception as e:
            print(f"Error retrieving order details: {e}")
            return None
    
    def _parse_order(self, order_data: Dict[str, Any]) -> Optional[Order]:
        """Parse order data from API response"""
        try:
            # Get order items
            items = []
            if 'orderItems' in order_data:
                for item_data in order_data['orderItems']:
                    item = OrderItem(
                        id=item_data.get('id'),
                        name=item_data.get('name', ''),
                        sku=item_data.get('sku', ''),
                        quantity=item_data.get('quantity', 0),
                        unit_price=float(item_data.get('unitPrice', 0)),
                        total_price=float(item_data.get('totalPrice', 0)),
                        status=item_data.get('orderItemStatus', '')
                    )
                    items.append(item)
            
            # Parse dates
            order_date = datetime.now()
            if 'createDate' in order_data:
                try:
                    order_date = datetime.fromisoformat(order_data['createDate'].replace('Z', '+00:00'))
                except:
                    pass
            
            return Order(
                id=order_data.get('id'),
                order_number=order_data.get('orderNumber', ''),
                customer_id=order_data.get('accountId', ''),
                customer_name=order_data.get('accountName', ''),
                order_date=order_date,
                status=order_data.get('orderStatus', ''),
                total_amount=float(order_data.get('total', 0)),
                items=items,
                shipping_address={},
                billing_address={}
            )
            
        except Exception as e:
            print(f"Error parsing order data: {e}")
            return None
    
    def search_orders(self, query: str) -> List[Order]:
        """Search orders by various criteria (order number, customer name, etc.)"""
        self._ensure_authenticated()
        try:
            # Try to find by order number first
            if query.isdigit() or query.startswith('ORD'):
                order = self.get_orders_by_order_number(query)
                return [order] if order else []
            
            # Try to find by customer email
            if '@' in query:
                return self.get_orders_by_customer_email(query)
            
            # Try to find by customer name (partial match)
            response = self.session.get(
                f"{self.base_url.rstrip('/')}/o/headless-commerce-admin-order/v1.0/orders",
                params={
                    "filter": f"accountName contains '{query}'",
                    "pageSize": 50
                }
            )
            
            if response.status_code != 200:
                return []
            
            orders_data = response.json().get('items', [])
            orders = []
            
            for order_data in orders_data:
                order = self._parse_order(order_data)
                if order:
                    orders.append(order)
            
            return orders
            
        except Exception as e:
            print(f"Error searching orders: {e}")
            return []
    
    def get_order_status_summary(self, order_id: str) -> Dict[str, Any]:
        """Get a summary of order status and key information"""
        order = self.get_order_details(order_id)
        if not order:
            return {}
        
        return {
            "order_number": order.order_number,
            "status": order.status,
            "order_date": order.order_date.strftime("%Y-%m-%d %H:%M:%S"),
            "customer_name": order.customer_name,
            "total_amount": f"${order.total_amount:.2f}",
            "item_count": len(order.items),
            "items": [
                {
                    "name": item.name,
                    "quantity": item.quantity,
                    "price": f"${item.total_price:.2f}"
                }
                for item in order.items
            ]
        }
    
    def get_placed_orders_by_account(self, channel_id: str, account_id: str, page: int = 1, page_size: int = 20, 
                                   sort: str = None, filter_string: str = None) -> Dict[str, Any]:
        """Get placed orders for a specific account using the delivery order API with optional filtering and sorting"""
        self._ensure_authenticated()
        try:
            params = {
                "page": page,
                "pageSize": page_size
            }
            
            if sort:
                params["sort"] = sort
            if filter_string:
                params["filter"] = filter_string
            
            response = self.session.get(
                f"{self.base_url.rstrip('/')}/o/headless-commerce-delivery-order/v1.0/channels/{channel_id}/accounts/{account_id}/placed-orders",
                params=params
            )
            
            if response.status_code != 200:
                return {}
            
            return response.json()
            
        except Exception as e:
            print(f"Error retrieving placed orders: {e}")
            return {}
    
    def get_all_placed_orders_by_account(self, channel_id: str, account_id: str) -> List[Dict[str, Any]]:
        """Get all placed orders for an account across all pages"""
        try:
            all_orders = []
            page = 1
            page_size = 100  # Use larger page size for efficiency
            
            while True:
                response_data = self.get_placed_orders_by_account(channel_id, account_id, page, page_size)
                
                if not response_data or 'items' not in response_data:
                    break
                
                orders = response_data['items']
                if not orders:
                    break
                
                all_orders.extend(orders)
                
                # Check if we've reached the last page
                if response_data.get('lastPage', 1) <= page:
                    break
                
                page += 1
            
            return all_orders
            
        except Exception as e:
            print(f"Error retrieving all placed orders: {e}")
            return []
    
    def get_placed_orders_by_account_with_filters(self, channel_id: str, account_id: str, 
                                                start_date: str = None, end_date: str = None, 
                                                page: int = 1, page_size: int = 20, 
                                                max_pages: int = 5) -> List[Dict[str, Any]]:
        """Get placed orders for an account with server-side filtering and pagination limits"""
        try:
            all_orders = []
            current_page = page
            
            # Build filter string
            filters = []
            if start_date:
                filters.append(f"createDate ge {start_date}")
            if end_date:
                filters.append(f"createDate le {end_date}")
            
            filter_string = " and ".join(filters) if filters else None
            
            # Add pagination limits to prevent fetching too many orders
            while current_page <= max_pages:
                response_data = self.get_placed_orders_by_account(
                    channel_id, account_id, current_page, page_size, 
                    sort="createDate:desc", filter_string=filter_string
                )
                
                if not response_data or 'items' not in response_data:
                    break
                
                orders = response_data['items']
                if not orders:
                    break
                
                all_orders.extend(orders)
                
                # Check if we've reached the last page
                if response_data.get('lastPage', 1) <= current_page:
                    break
                
                current_page += 1
            
            return all_orders
            
        except Exception as e:
            print(f"Error retrieving filtered placed orders: {e}")
            return []
    
    def get_placed_orders_by_account_limited(self, channel_id: str, account_id: str, 
                                           max_orders: int = 100) -> List[Dict[str, Any]]:
        """Get placed orders for an account with a reasonable limit to prevent performance issues"""
        try:
            all_orders = []
            page = 1
            page_size = 50  # Reasonable page size
            
            while len(all_orders) < max_orders:
                response_data = self.get_placed_orders_by_account(
                    channel_id, account_id, page, page_size, 
                    sort="createDate:desc"
                )
                
                if not response_data or 'items' not in response_data:
                    break
                
                orders = response_data['items']
                if not orders:
                    break
                
                # Add orders up to the limit
                remaining_slots = max_orders - len(all_orders)
                orders_to_add = orders[:remaining_slots]
                all_orders.extend(orders_to_add)
                
                # If we've added all orders from this page, check if there are more pages
                if len(orders) < page_size or len(all_orders) >= max_orders:
                    break
                
                page += 1
            
            return all_orders
            
        except Exception as e:
            print(f"Error retrieving limited placed orders: {e}")
            return []
    
    def search_orders_by_external_reference(self, external_reference: str) -> Optional[Dict[str, Any]]:
        """Search for an order by its external reference code"""
        try:
            # This would need to be implemented based on available search endpoints
            # For now, we can search through placed orders if we have account/channel info
            print(f"Search by external reference not yet implemented: {external_reference}")
            return None
            
        except Exception as e:
            print(f"Error searching by external reference: {e}")
            return None
    
    def get_placed_order_items(self, order_id: str) -> List[Dict[str, Any]]:
        """Get detailed items for a specific placed order"""
        self._ensure_authenticated()
        try:
            response = self.session.get(
                f"{self.base_url.rstrip('/')}/o/headless-commerce-delivery-order/v1.0/placed-orders/{order_id}/placed-order-items"
            )
            
            if response.status_code != 200:
                return []
            
            data = response.json()
            return data.get('items', [])
            
        except Exception as e:
            print(f"Error retrieving order items: {e}")
            return []
    
    def get_order_shipments(self, order_id: str) -> List[Dict[str, Any]]:
        """Get shipment information for a specific order"""
        self._ensure_authenticated()
        try:
            response = self.session.get(
                f"{self.base_url.rstrip('/')}/o/headless-commerce-delivery-order/v1.0/placed-orders/{order_id}/shipments"
            )
            
            if response.status_code != 200:
                return []
            
            data = response.json()
            return data.get('items', [])
            
        except Exception as e:
            print(f"Error retrieving order shipments: {e}")
            return []
    
    def get_direct_order_details(self, order_id: str) -> Optional[Dict[str, Any]]:
        """Get detailed order information using the delivery order endpoint"""
        self._ensure_authenticated()
        try:
            response = self.session.get(
                f"{self.base_url.rstrip('/')}/o/headless-commerce-delivery-order/v1.0/placed-orders/{order_id}"
            )
            
            if response.status_code != 200:
                print(f"Error retrieving order details: {response.status_code} - {response.text[:200]}")
                return None
            
            return response.json()
            
        except Exception as e:
            print(f"Error retrieving direct order details: {e}")
            return None
    
    def get_products_by_channel(self, channel_id: str, account_id: str = None, page: int = 1, page_size: int = 20) -> Dict[str, Any]:
        """Get products from a specific channel's catalog"""
        self._ensure_authenticated()
        try:
            params = {
                "page": page,
                "pageSize": page_size
            }
            
            if account_id:
                params["accountId"] = account_id
            
            response = self.session.get(
                f"{self.base_url.rstrip('/')}/o/headless-commerce-delivery-catalog/v1.0/channels/{channel_id}/products",
                params=params
            )
            
            if response.status_code != 200:
                return {}
            
            return response.json()
            
        except Exception as e:
            print(f"Error retrieving products: {e}")
            return {}
    
    def get_all_products_by_channel(self, channel_id: str, account_id: str = None) -> List[Dict[str, Any]]:
        """Get all products from a channel's catalog across all pages"""
        try:
            all_products = []
            page = 1
            page_size = 100  # Use larger page size for efficiency
            
            while True:
                response_data = self.get_products_by_channel(channel_id, account_id, page, page_size)
                
                if not response_data or 'items' not in response_data:
                    break
                
                products = response_data['items']
                if not products:
                    break
                
                all_products.extend(products)
                
                # Check if we've reached the last page
                if response_data.get('lastPage', 1) <= page:
                    break
                
                page += 1
            
            return all_products
            
        except Exception as e:
            print(f"Error retrieving all products: {e}")
            return []
    
    def get_product_by_id(self, product_id: str) -> Optional[Dict[str, Any]]:
        """Get detailed information for a specific product"""
        self._ensure_authenticated()
        try:
            # Try the direct product endpoint first
            response = self.session.get(
                f"{self.base_url.rstrip('/')}/o/headless-commerce-delivery-catalog/v1.0/products/{product_id}"
            )
            
            if response.status_code == 200:
                return response.json()
            
            # If direct endpoint fails, try to find the product in the catalog
            # This is a fallback method for products that might not have direct access
            print(f"Direct product endpoint failed, trying catalog search...")
            
            # Search through all products to find the specific one
            # We'll use a default channel since we know the product exists
            channel_id = "33579"  # Default channel from our testing
            all_products = self.get_all_products_by_channel(channel_id)
            
            for product in all_products:
                if str(product.get('id')) == str(product_id):
                    return product
            
            return None
            
        except Exception as e:
            print(f"Error retrieving product: {e}")
            return None
    
    def search_products(self, channel_id: str, query: str, account_id: str = None) -> List[Dict[str, Any]]:
        """Search for products by name, description, or other criteria"""
        self._ensure_authenticated()
        try:
            # Get all products and filter by search criteria
            all_products = self.get_all_products_by_channel(channel_id, account_id)
            
            if not all_products:
                return []
            
            # Simple text search - can be enhanced with more sophisticated search logic
            query_lower = query.lower()
            matching_products = []
            
            for product in all_products:
                name = product.get('name', '').lower()
                description = product.get('description', '').lower()
                external_ref = product.get('externalReferenceCode', '').lower()
                
                if (query_lower in name or 
                    query_lower in description or 
                    query_lower in external_ref):
                    matching_products.append(product)
            
            return matching_products
            
        except Exception as e:
            print(f"Error searching products: {e}")
            return []
    
    def get_available_channels(self) -> List[Dict[str, Any]]:
        """Get list of available channels for the authenticated user"""
        self._ensure_authenticated()
        try:
            response = self.session.get(
                f"{self.base_url.rstrip('/')}/o/headless-commerce-delivery-catalog/v1.0/channels"
            )
            
            if response.status_code != 200:
                return []
            
            data = response.json()
            return data.get('items', [])
            
        except Exception as e:
            print(f"Error retrieving channels: {e}")
            return []
    
    def get_channel_by_name(self, channel_name: str) -> Optional[Dict[str, Any]]:
        """Find a channel by name (case-insensitive partial match)"""
        try:
            channels = self.get_available_channels()
            
            if not channels:
                return None
            
            channel_name_lower = channel_name.lower()
            
            for channel in channels:
                name = channel.get('name', '').lower()
                if channel_name_lower in name:
                    return channel
            
            return None
            
        except Exception as e:
            print(f"Error finding channel by name: {e}")
            return None
    
    def get_available_accounts(self, channel_id: str = None) -> List[Dict[str, Any]]:
        """Get list of available accounts (optionally filtered by channel)"""
        self._ensure_authenticated()
        try:
            if channel_id:
                # Try to get accounts for a specific channel
                response = self.session.get(
                    f"{self.base_url.rstrip('/')}/o/headless-commerce-delivery-catalog/v1.0/channels/{channel_id}/accounts"
                )
            else:
                # Try to get all accounts the user has access to
                response = self.session.get(
                    f"{self.base_url.rstrip('/')}/o/headless-commerce-delivery-catalog/v1.0/accounts"
                )
            
            if response.status_code != 200:
                return []
            
            data = response.json()
            return data.get('items', [])
            
        except Exception as e:
            print(f"Error retrieving accounts: {e}")
            return []
    
    def get_account_by_name(self, account_name: str, channel_id: str = None) -> Optional[Dict[str, Any]]:
        """Find an account by name (case-insensitive partial match)"""
        try:
            accounts = self.get_available_accounts(channel_id)
            
            if not accounts:
                return None
            
            account_name_lower = account_name.lower()
            
            for account in accounts:
                name = account.get('name', '').lower()
                if account_name_lower in name:
                    return account
            
            return None
            
        except Exception as e:
            print(f"Error finding account by name: {e}")
            return None
    
    def discover_channel_and_account(self, channel_name: str = None, account_name: str = None) -> Dict[str, Any]:
        """Discover channel and account IDs based on names or return available options"""
        try:
            result = {
                'channel_id': None,
                'account_id': None,
                'available_channels': [],
                'available_accounts': [],
                'suggestions': []
            }
            
            # Get available channels
            channels = self.get_available_channels()
            result['available_channels'] = channels
            
            if channels:
                print(f"✅ Found {len(channels)} available channels:")
                for channel in channels:
                    print(f"   - ID: {channel.get('id')}, Name: {channel.get('name')}")
                
                # If channel name provided, try to find it
                if channel_name:
                    found_channel = self.get_channel_by_name(channel_name)
                    if found_channel:
                        result['channel_id'] = found_channel['id']
                        print(f"✅ Found channel: {found_channel['name']} (ID: {found_channel['id']})")
                    else:
                        result['suggestions'].append(f"Channel '{channel_name}' not found. Available channels: {[c.get('name') for c in channels]}")
            else:
                result['suggestions'].append("No channels found. Check your permissions and API access.")
            
            # If we have a channel ID, try to get accounts
            if result['channel_id']:
                accounts = self.get_available_accounts(result['channel_id'])
                result['available_accounts'] = accounts
                
                if accounts:
                    print(f"\n✅ Found {len(accounts)} accounts for channel {result['channel_id']}:")
                    for account in accounts:
                        print(f"   - ID: {account.get('id')}, Name: {account.get('name')}")
                    
                    # If account name provided, try to find it
                    if account_name:
                        found_account = self.get_account_by_name(account_name, result['channel_id'])
                        if found_account:
                            result['account_id'] = found_account['id']
                            print(f"✅ Found account: {found_account['name']} (ID: {found_account['id']})")
                        else:
                            result['suggestions'].append(f"Account '{account_name}' not found in channel {result['channel_id']}. Available accounts: {[a.get('name') for a in accounts]}")
                else:
                    result['suggestions'].append(f"No accounts found for channel {result['channel_id']}")
            
            return result
            
        except Exception as e:
            print(f"Error discovering channel and account: {e}")
            return {
                'channel_id': None,
                'account_id': None,
                'available_channels': [],
                'available_accounts': [],
                'suggestions': [f"Error during discovery: {e}"]
            }
    
    def auto_discover_and_test(self) -> Dict[str, Any]:
        """Automatically discover available channels/accounts and test basic functionality"""
        self._ensure_authenticated()
        try:
            print("🔍 Auto-discovering available channels and accounts...")
            
            # Get available channels
            channels = self.get_available_channels()
            
            if not channels:
                return {
                    'success': False,
                    'message': 'No channels found. Check your permissions and API access.',
                    'channels': [],
                    'accounts': [],
                    'test_results': {}
                }
            
            print(f"✅ Found {len(channels)} channels:")
            for channel in channels:
                print(f"   - {channel.get('name')} (ID: {channel.get('id')})")
            
            # Test with first available channel
            test_channel = channels[0]
            channel_id = test_channel['id']
            print(f"\n🧪 Testing with channel: {test_channel['name']} (ID: {channel_id})")
            
            # Get accounts for this channel
            accounts = self.get_available_accounts(channel_id)
            
            if not accounts:
                return {
                    'success': False,
                    'message': f'No accounts found for channel {channel_id}',
                    'channels': channels,
                    'accounts': [],
                    'test_results': {}
                }
            
            print(f"✅ Found {len(accounts)} accounts:")
            for account in accounts:
                print(f"   - {account['name']} (ID: {account['id']})")
            
            # Test with first available account
            test_account = accounts[0]
            account_id = test_account['id']
            print(f"\n🧪 Testing with account: {test_account['name']} (ID: {account_id})")
            
            # Run basic tests
            test_results = {}
            
            # Test orders
            try:
                orders = self.get_placed_orders_by_account(channel_id, account_id, page=1, page_size=5)
                if orders and 'items' in orders:
                    test_results['orders'] = {
                        'success': True,
                        'count': len(orders['items']),
                        'total_available': orders.get('totalCount', 0)
                    }
                    print(f"✅ Orders test: {len(orders['items'])} orders retrieved")
                else:
                    test_results['orders'] = {'success': False, 'message': 'No orders found'}
                    print("⚠️  Orders test: No orders found")
            except Exception as e:
                test_results['orders'] = {'success': False, 'message': str(e)}
                print(f"❌ Orders test failed: {e}")
            
            # Test products
            try:
                products = self.get_products_by_channel(channel_id, account_id, page=1, page_size=5)
                if products and 'items' in products:
                    test_results['products'] = {
                        'success': True,
                        'count': len(products['items']),
                        'total_available': products.get('totalCount', 0)
                    }
                    print(f"✅ Products test: {len(products['items'])} products retrieved")
                else:
                    test_results['products'] = {'success': False, 'message': 'No products found'}
                    print("⚠️  Products test: No products found")
            except Exception as e:
                test_results['products'] = {'success': False, 'message': str(e)}
                print(f"❌ Products test failed: {e}")
            
            return {
                'success': True,
                'message': 'Auto-discovery and testing completed successfully',
                'channels': channels,
                'accounts': accounts,
                'test_channel': test_channel,
                'test_account': test_account,
                'test_results': test_results
            }
            
        except Exception as e:
            return {
                'success': False,
                'message': f'Auto-discovery failed: {e}',
                'channels': [],
                'accounts': [],
                'test_results': {}
            }

def main():
    """Main function to test the Liferay API Client"""
    print("🚀 Testing Liferay API Client...")
    print("=" * 50)
    
    try:
        # Initialize client
        client = LiferayAPIClient()
        print(f"✅ Client initialized with base URL: {client.base_url}")
        
        # Test connection
        print("\n🔍 Testing API connection...")
        result = client.auto_discover_and_test()
        
        if result['success']:
            print("✅ API connection successful!")
            print(f"📊 Found {len(result['channels'])} channels and {len(result['accounts'])} accounts")
            
            if result['test_channel']:
                print(f"🎯 Test channel: {result['test_channel']['name']} (ID: {result['test_channel']['id']})")
            if result['test_account']:
                print(f"👤 Test account: {result['test_account']['name']} (ID: {result['test_account']['id']})")
            
            # Show test results
            if result['test_results']:
                print("\n📋 Test Results:")
                for test_name, test_result in result['test_results'].items():
                    if test_result['success']:
                        print(f"  ✅ {test_name}: {test_result.get('count', 'N/A')} items")
                    else:
                        print(f"  ❌ {test_name}: {test_result.get('message', 'Failed')}")
        else:
            print(f"❌ API connection failed: {result['message']}")
            print("\nTroubleshooting:")
            print("1. Check your LIFERAY_BASE_URL in .env file")
            print("2. Verify your LIFERAY_USERNAME and LIFERAY_PASSWORD")
            print("3. Make sure your Liferay instance is running")
            print("4. Check if the API endpoints are accessible")
        
    except Exception as e:
        print(f"❌ Error testing client: {e}")
        print("\nTroubleshooting:")
        print("1. Make sure all dependencies are installed: poetry install")
        print("2. Check your .env file configuration")
        print("3. Verify your Liferay instance is running and accessible")

if __name__ == "__main__":
    main()
