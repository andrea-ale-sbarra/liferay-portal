import os
from dotenv import load_dotenv
from typing import Dict, Any

load_dotenv()

class LiferayConfig:
    """Configuration class for Liferay API settings"""
    
    # Liferay API Configuration
    LIFERAY_BASE_URL = os.getenv('LIFERAY_BASE_URL', 'http://localhost:8080')
    LIFERAY_USERNAME = os.getenv('LIFERAY_USERNAME', 'test@liferay.com')
    LIFERAY_PASSWORD = os.getenv('LIFERAY_PASSWORD', 'test')
    
    # API Endpoints - Prefer delivery-order endpoints for customer-facing operations
    API_ENDPOINTS = {
        'user_accounts': '/o/headless-admin-user/v1.0/user-accounts',
        'my_user_account': '/o/headless-admin-user/v1.0/my-user-account',
        'orders': '/o/headless-commerce-delivery-order/v1.0/placed-orders',  # Prefer delivery-order
        'order_items': '/o/headless-commerce-delivery-order/v1.0/placed-orders',  # Via placed-orders/{id}/placed-order-items
        'accounts': '/o/headless-admin-user/v1.0/accounts',
        'commerce_accounts': '/o/headless-commerce-admin-account/v1.0/accounts',
        'admin_orders': '/o/headless-commerce-admin-order/v1.0/orders',  # Fallback for complex filtering
        'admin_order_items': '/o/headless-commerce-admin-order/v1.0/order-items'  # Fallback for order items
    }
    
    # API Headers
    DEFAULT_HEADERS = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
    
    # Search Configuration
    SEARCH_CONFIG = {
        'max_results': 100,
        'default_page_size': 20,
        'timeout_seconds': 30
    }
    
    # Order Status Mapping
    ORDER_STATUS_MAPPING = {
        'pending': 'Pending',
        'processing': 'Processing',
        'shipped': 'Shipped',
        'delivered': 'Delivered',
        'cancelled': 'Cancelled',
        'refunded': 'Refunded'
    }
    
    @classmethod
    def get_api_url(cls, endpoint: str) -> str:
        """Get full API URL for a given endpoint"""
        return f"{cls.LIFERAY_BASE_URL}{cls.API_ENDPOINTS.get(endpoint, endpoint)}"
    
    @classmethod
    def get_auth_credentials(cls) -> Dict[str, str]:
        """Get authentication credentials"""
        return {
            'username': cls.LIFERAY_USERNAME,
            'password': cls.LIFERAY_PASSWORD
        }
    
    @classmethod
    def validate_config(cls) -> bool:
        """Validate that required configuration is present"""
        required_vars = ['LIFERAY_BASE_URL', 'LIFERAY_USERNAME', 'LIFERAY_PASSWORD']
        
        for var in required_vars:
            if not getattr(cls, var):
                print(f"Warning: {var} is not set")
                return False
        
        return True
    
    @classmethod
    def print_config(cls):
        """Print current configuration (without sensitive data)"""
        print("Liferay API Configuration:")
        print(f"  Base URL: {cls.LIFERAY_BASE_URL}")
        print(f"  Username: {cls.LIFERAY_USERNAME}")
        print(f"  API Endpoints: {len(cls.API_ENDPOINTS)} configured")
        print(f"  Search Config: Max results: {cls.SEARCH_CONFIG['max_results']}")
        print(f"  Order Statuses: {len(cls.ORDER_STATUS_MAPPING)} supported")

# Environment variable template
ENV_TEMPLATE = """
# Liferay API Configuration
LIFERAY_BASE_URL=http://localhost:8080
LIFERAY_USERNAME=test@liferay.com
LIFERAY_PASSWORD=test

# Optional: Custom API endpoints (if different from defaults)
# LIFERAY_API_VERSION=v1.0
# LIFERAY_TIMEOUT=30
"""

def create_env_file():
    """Create a .env file with template configuration"""
    env_file = '.env'
    if not os.path.exists(env_file):
        with open(env_file, 'w') as f:
            f.write(ENV_TEMPLATE.strip())
        print(f"Created {env_file} with template configuration")
        print("Please update the values with your actual Liferay instance details")
    else:
        print(f"{env_file} already exists")

if __name__ == "__main__":
    # Print current configuration
    LiferayConfig.print_config()
    
    # Validate configuration
    if LiferayConfig.validate_config():
        print("\n✅ Configuration is valid")
    else:
        print("\n⚠️  Configuration has issues")
        print("Consider creating a .env file with proper values")
        create_env_file()
