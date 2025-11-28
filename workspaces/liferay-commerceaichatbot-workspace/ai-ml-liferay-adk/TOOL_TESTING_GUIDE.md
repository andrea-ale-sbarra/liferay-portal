# Liferay Customer Service Agent - Tool Testing Guide

This guide provides sample questions to test each of the 13 tools available in the Liferay Customer Service Agent when running ADK web locally.

## 🚀 Quick Start

### Quick Test (5 minutes)
Run these questions to verify basic functionality:

1. "What test emails are available?"
2. "Find order 80654"
3. "Get orders for john.smith@betavehicle.com"
4. "What is your return policy?"

---

## 📊 Tool Categories by Performance

### ⚡ Tier 1: Instant/Fast Tools (< 5 seconds)

#### 1. FAQ Information Tool
**Purpose**: Provides instant answers to frequently asked questions

**Sample Questions**:
```
What is your return policy?
How do I track my order?
What are your shipping options?
How do I contact customer service?
What are your business hours?
How do I cancel an order?
What payment methods do you accept?
How do I update my account information?
```

**Expected Response**: Structured FAQ responses with relevant information

---

#### 2. Test Emails Tool
**Purpose**: Lists available test email addresses for testing

**Sample Questions**:
```
What test emails are available?
Show me available test email addresses
List test customer emails
What email addresses can I use for testing?
```

**Expected Response**: List of test email mappings with account information

---

#### 3. Find Order Tool
**Purpose**: Direct order lookup by order ID

**Sample Questions**:
```
Find order 80654
Look up order 74522
Get details for order 73817
Show me order 75230
Find order 78435
```

**Expected Response**: Detailed order information including status, date, and customer details

---

#### 4. Order Shipping Tool
**Purpose**: Retrieves shipping information for a specific order

**Sample Questions**:
```
Get shipping information for order 80654
Show me shipping details for order 74522
What's the shipping status for order 73817?
Get delivery information for order 75230
Show me shipping address for order 78435
```

**Expected Response**: Shipping address, cost, and delivery status information

---

#### 5. List Channels and Accounts Tool
**Purpose**: Shows available channels and customer accounts

**Sample Questions**:
```
List available channels and accounts
Show me system information
What channels and accounts are available?
Get channel and account details
Show me available channels
```

**Expected Response**: List of channels and accounts with IDs and names

---

#### 6. Order Items Tool
**Purpose**: Shows detailed items for a specific order

**Sample Questions**:
```
Show me items for order 80654
What products are in order 74522?
Get order items for order 73817
List products for order 75230
Show me order contents for order 78435
```

**Expected Response**: Detailed list of order items with quantities, prices, and descriptions

---

### ⚡ Tier 2: Medium Speed Tools (5-30 seconds)

#### 7. Customer Orders Tool
**Purpose**: Retrieves all orders for a specific customer

**Sample Questions**:
```
Get orders for john.smith@betavehicle.com
Show me orders for john.smith@betavehicle.com
List all orders for john.smith@betavehicle.com
Find orders for john.smith@betavehicle.com
Get customer orders for john.smith@betavehicle.com
```

**Expected Response**: List of all orders for the customer, sorted by date (newest first)

---

#### 8. Date Range Search Tool
**Purpose**: Finds orders within a specific date range

**Sample Questions**:
```
Find orders from 2024-01-01 to 2024-12-31 for john.smith@betavehicle.com
Show me orders since 2024-06-01 for john.smith@betavehicle.com
Get orders between 2024-03-01 and 2024-03-31 for john.smith@betavehicle.com
Find orders from 2024-07-01 to 2024-07-31 for john.smith@betavehicle.com
Show me orders from last month for john.smith@betavehicle.com
```

**Expected Response**: Orders within the specified date range, sorted by date

---

#### 9. System Status Tool
**Purpose**: Checks system connectivity and health

**Sample Questions**:
```
Check system status
Is the system working?
Show me system health
Get system information
Check connectivity
```

**Expected Response**: System status including channel and account information

---

#### 10. All Accounts Summary Tool
**Purpose**: Provides comprehensive account information

**Sample Questions**:
```
Show me all accounts summary
List all available accounts
Get comprehensive account information
Show me account details
List customer accounts
```

**Expected Response**: Detailed summary of all available accounts

---

### ⚡ Tier 3: Slow Tools (> 30 seconds) - Use as Last Resort

#### 11. Search Orders Tool (General)
**Purpose**: General order search with intelligent routing

**Sample Questions**:
```
Search for orders
Find my orders
Help me find orders
I need to search for orders
Show me order search options
```

**Expected Response**: Intelligent routing to appropriate specific tools with performance guidance

---

#### 12. Product Search Tool
**Purpose**: Finds orders containing specific products

**Sample Questions**:
```
Find orders containing brake for john.smith@betavehicle.com
Search for orders with tire products for john.smith@betavehicle.com
Show me orders with oil products for john.smith@betavehicle.com
Find orders with battery for john.smith@betavehicle.com
Search for orders containing filter for john.smith@betavehicle.com
```

**Expected Response**: Orders containing the specified product, with detailed item information

---

#### 13. Status Search Tool
**Purpose**: Finds orders with specific status

**Sample Questions**:
```
Find completed orders for john.smith@betavehicle.com
Show me pending orders for john.smith@betavehicle.com
Get shipped orders for john.smith@betavehicle.com
Find processing orders for john.smith@betavehicle.com
Show me canceled orders for john.smith@betavehicle.com
```

**Expected Response**: Orders with the specified status, with detailed status information

---

#### 14. Shipping Address Search Tool
**Purpose**: Finds orders shipped to specific locations

**Sample Questions**:
```
Find orders shipped to New York for john.smith@betavehicle.com
Search for orders delivered to California for john.smith@betavehicle.com
Show me orders shipped to Texas for john.smith@betavehicle.com
Find orders delivered to Florida for john.smith@betavehicle.com
Search for orders shipped to Illinois for john.smith@betavehicle.com
```

**Expected Response**: Orders shipped to the specified location, with shipping address details

---

## 🎯 Testing Scenarios

### Scenario 1: Quick Functionality Test (5 minutes)
Test basic functionality with these questions:

1. "What test emails are available?"
2. "Find order 80654"
3. "Get orders for john.smith@betavehicle.com"
4. "What is your return policy?"

### Scenario 2: Customer Service Representative (10 minutes)
Simulate a customer service interaction:

1. "Check system status"
2. "Find order 80654"
3. "Show me items for order 80654"
4. "Get shipping information for order 80654"
5. "Find orders from 2024-01-01 to 2024-12-31 for john.smith@betavehicle.com"

### Scenario 3: Comprehensive Testing (15 minutes)
Test all tool categories:

1. **FAQ**: "What are your shipping options?"
2. **System Status**: "Check system status"
3. **Find Order**: "Find order 80654"
4. **Order Items**: "Show me items for order 80654"
5. **Order Shipping**: "Get shipping information for order 80654"
6. **Customer Orders**: "Get orders for john.smith@betavehicle.com"
7. **Date Range**: "Find orders from 2024-01-01 to 2024-12-31 for john.smith@betavehicle.com"
8. **Product Search**: "Find orders containing brake for john.smith@betavehicle.com"
9. **Status Search**: "Find completed orders for john.smith@betavehicle.com"
10. **Address Search**: "Find orders shipped to New York for john.smith@betavehicle.com"

### Scenario 4: Performance Testing (20 minutes)
Test performance across different tool categories:

**Fast Tools (1-5 seconds)**:
- "What test emails are available?"
- "Find order 80654"
- "Show me items for order 80654"

**Medium Tools (5-30 seconds)**:
- "Get orders for john.smith@betavehicle.com"
- "Find orders from 2024-01-01 to 2024-12-31 for john.smith@betavehicle.com"
- "Check system status"

**Slow Tools (30+ seconds)**:
- "Find orders containing brake for john.smith@betavehicle.com"
- "Find completed orders for john.smith@betavehicle.com"
- "Find orders shipped to New York for john.smith@betavehicle.com"

---

## 📋 Expected Results

### Response Time Expectations
- **Fast Tools**: 1-5 seconds
- **Medium Tools**: 5-30 seconds
- **Slow Tools**: 30 seconds to 5+ minutes

### Data Quality Expectations
- Orders should be properly formatted and sorted by date (newest first)
- Error handling should be graceful for invalid inputs
- Responses should be clear and helpful
- All tools should return structured data

### Error Handling Expectations
- Invalid order IDs should return appropriate error messages
- Invalid email addresses should be handled gracefully
- Network errors should be reported clearly
- Missing data should be indicated appropriately

---

## 🔧 Troubleshooting

### Common Issues
1. **Slow Responses**: Some tools are inherently slow due to API limitations
2. **No Results**: Check if the test data contains the requested information
3. **Authentication Errors**: Verify Liferay credentials are correct
4. **Network Issues**: Check connectivity to Liferay UAT environment

### Debug Tips
1. Start with fast tools to verify basic connectivity
2. Use known order IDs (80654, 74522, 73817) for testing
3. Use test email addresses (john.smith@betavehicle.com) for customer queries
4. Check system status if other tools fail

---

## 📝 Notes

- All tools have been tested locally and work correctly
- Performance times are based on local testing with Liferay UAT environment
- Some tools may take longer in production due to network latency
- The Agent Engine platform has known issues with tool execution in deployed environments

---

*Last Updated: January 2025*
*Version: 1.0*
