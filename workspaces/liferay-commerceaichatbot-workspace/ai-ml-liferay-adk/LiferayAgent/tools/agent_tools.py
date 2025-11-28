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

"""Agent tools for the Liferay Agent."""

from .customer_service_tools import (
    get_system_status_tool,
    list_channels_accounts_tool,
    find_order_tool,
    search_orders_tool,
    search_orders_by_date_range_tool,
    search_orders_by_product_tool,
    search_orders_by_status_tool,
    search_orders_by_shipping_address_tool,
    get_customer_orders_tool,
    get_test_emails_tool,
    get_order_items_tool,
    get_order_shipping_tool,
    get_all_accounts_summary_tool,
    get_faq_information_tool,
    test_external_api_tool,
    debug_liferay_connection_tool,
    debug_customer_and_orders_tool
)

# Define the tools available to the agent
agent_tools = [
    get_system_status_tool,
    list_channels_accounts_tool,
    find_order_tool,
    search_orders_tool,
    search_orders_by_date_range_tool,
    search_orders_by_product_tool,
    search_orders_by_status_tool,
    search_orders_by_shipping_address_tool,
    get_customer_orders_tool,
    get_test_emails_tool,
    get_order_items_tool,
    get_order_shipping_tool,
    get_all_accounts_summary_tool,
    get_faq_information_tool,
    #test_external_api_tool,
    #debug_liferay_connection_tool,
    #debug_customer_and_orders_tool
]
