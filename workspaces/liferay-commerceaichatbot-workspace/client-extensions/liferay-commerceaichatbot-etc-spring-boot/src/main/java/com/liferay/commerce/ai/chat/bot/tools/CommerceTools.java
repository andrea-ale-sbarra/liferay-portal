package com.liferay.commerce.ai.chat.bot.tools;

import com.google.adk.tools.Annotations;
import com.liferay.commerce.ai.chat.bot.service.CommerceService;
import com.liferay.commerce.ai.chat.bot.model.*;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CommerceTools {

    public CommerceTools(CommerceService commerceService) {
        _commerceService = commerceService;
    }

    @Annotations.Schema(
        description = "List all available channels and accounts in Liferay Commerce.",
        name = "listAvailableChannelsAndAccountsTool")
    public Map<String, ?> listAvailableChannelsAndAccountsTool() {
        try {
            List<Channel> channels = _commerceService.getChannels();
            StringBuilder sb = new StringBuilder(
                "**Available Channels and Accounts**\n\n");

            if (channels != null && !channels.isEmpty()) {
                sb.append("**Channels:**\n");

                for (int i = 0; i < channels.size(); i++) {
                    Channel channel = channels.get(i);

                    sb.append(i + 1);
                    sb.append(". **");
                    sb.append(nullToNA(channel.getName()));
                    sb.append("** (ID: ");
                    sb.append(nullToNA(channel.getId()));
                    sb.append(")\n");
                    sb.append("   - Type: ");
                    sb.append(nullToNA(channel.getType()));
                    sb.append("\n");
                    sb.append("   - Active: ");
                    sb.append(channel.getActive());
                    sb.append("\n\n");
                }

                for (Channel channel : channels) {
                    String channelId = channel.getId();

                    List<Account> accounts = _commerceService
                        .getAccounts(channelId);

                    if (accounts != null && !accounts.isEmpty()) {
                        sb.append("**Accounts for Channel ");
                        sb.append(channelId).append(":**\n");

                        for (int i = 0; i < accounts.size(); i++) {
                            Account account = accounts.get(i);

                            sb.append(i + 1);
                            sb.append(". **");
                            sb.append(nullToNA(account.getName()));
                            sb.append("** (ID: ");
                            sb.append(nullToNA(account.getId()));
                            sb.append(")\n");
                            sb.append("   - Type: ");
                            sb.append(nullToNA(account.getType()));
                            sb.append("\n");
                            sb.append("   - Status: ");
                            sb.append(nullToNA(account.getStatus()));
                            sb.append("\n\n");
                        }
                    } else {
                        sb.append("**No accounts found for this channel**\n\n");
                    }
                }
            } else {
                sb.append("**No channels available**\n\n");
            }

            sb.append("**How to use:**\n");
            sb.append("- Use these IDs in your API calls\n");
            sb.append("- Ask me to 'Find orders for account [ID]'\n");
            sb.append("- Ask me to 'Show products for channel [ID]'\n");

            return Map.of("response", sb.toString());
        } catch (Exception e) {
            return Map.of("error", "**Error listing resources**: " + e.getMessage());
        }
    }

    @Annotations.Schema(
        description = "Find order details by order ID.", name = "findOrderTool")
    public Map<String, ?> findOrderTool(
            @Annotations.Schema(description = "Order ID", name = "orderId") String orderId) {

        try {
            Order order = _commerceService.getOrder(orderId);

            if (order != null) {
                return Map.of("order", formatOrderSummary(order));
            }

            return Map.of("error", "I couldn't find an order with ID '" + orderId +
                "'. Please check the order ID and try again.");
        } catch (Exception e) {
            return Map.of("error", "Error finding order: " + e.getMessage());
        }
    }

    @Annotations.Schema(
        description = "Search for orders using various criteria with intelligent routing to fastest tools",
        name = "searchOrdersTool")
    public Map<String, ?> searchOrdersTool(
            @Annotations.Schema(description = "Query", name = "query") String query) {
        try {
            String queryLower = query == null ? "" : query.toLowerCase(Locale.ROOT);

            // 1) Direct order ID lookup (4-6 digits)
            Pattern orderIdPattern = Pattern.compile("\\b\\d{4,6}\\b");
            Matcher matcher = orderIdPattern.matcher(query == null ? "" : query);
            if (matcher.find()) {
                String orderId = matcher.group();
                return Map.of(
                    "order",
                    "**Fast Route: Direct Order Lookup**\n\n" +
                        "I found what looks like an order ID (" + orderId + ") in your query.\n" +
                        "Let me get that order directly for you - this will be much faster than searching.\n\n" +
                        findOrderTool(orderId)
                );
            }

            // 2) Date-related search routing
            if (queryLower.contains("from") || queryLower.contains("between") || queryLower.contains("since") ||
                queryLower.contains("after") || queryLower.contains("before") || queryLower.contains("date range") ||
                queryLower.contains("last ")) {
                String doc = "Search for orders within a specific date range for a specific user";
                return Map.of(
                    "search",
                    "**Fast Route: Date Range Search**\n\n" +
                        "I detected a date-related search. Let me route this to the optimized date range search tool for better performance.\n\n" +
                        doc + "\n\n" +
                        "**Please provide your email address to continue:**\n" +
                        "- \"Search my orders from 2024-01-01 to 2024-01-31 using your.email@example.com\"\n" +
                        "- \"Find orders since 2024-01-01 for customer@company.com\""
                );
            }

            // 3) Product-related search routing
            if (queryLower.contains("product") || queryLower.contains("item") || queryLower.contains("contains") ||
                queryLower.contains("battery") || queryLower.contains("tire") || queryLower.contains("brake") ||
                queryLower.contains("oil")) {
                return Map.of(
                    "search",
                    "**Slow Route: Product Search**\n\n" +
                        "I detected a product-related search. This will take about 1-2 minutes to complete.\n\n" +
                        "**Available options:**\n" +
                        "1. **Fast alternative**: If you know the order ID, use \"Find order [ID]\" (1 second)\n" +
                        "2. **Continue with product search**: Provide your email address\n\n" +
                        "**Please provide your email address:**\n" +
                        "- \"Search my orders for brake pads using your.email@example.com\""
                );
            }

            // 4) Status-related search routing
            if (queryLower.contains("pending") || queryLower.contains("shipped") || queryLower.contains("completed") ||
                queryLower.contains("canceled") || queryLower.contains("processing") || queryLower.contains("hold") ||
                queryLower.contains("status")) {
                return Map.of(
                    "search",
                    "**Slow Route: Status Search**\n\n" +
                        "I detected a status-related search. This will take about 1-2 minutes to complete.\n\n" +
                        "**Available options:**\n" +
                        "1. **Fast alternative**: If you know the order ID, use \"Find order [ID]\" (1 second)\n" +
                        "2. **Continue with status search**: Provide your email address\n\n" +
                        "**Please provide your email address:**\n" +
                        "- \"Search my pending orders using your.email@example.com\""
                );
            }

            // 5) Address-related search routing
            if (queryLower.contains("address") || queryLower.contains("shipped to") || queryLower.contains("delivery") ||
                queryLower.contains("city") || queryLower.contains("state") || queryLower.contains("zip") ||
                queryLower.contains("postal")) {
                return Map.of(
                    "search",
                    "**Extremely Slow Route: Address Search**\n\n" +
                        "I detected an address-related search. This will take 5+ minutes to complete.\n\n" +
                        "**Available options:**\n" +
                        "1. **Fast alternative**: If you know the order ID, use \"Find order [ID]\" (1 second)\n" +
                        "2. **Medium speed**: Try date range search if you know the timeframe (6 seconds)\n" +
                        "3. **Continue with address search**: Provide your email address (5+ minutes)\n\n" +
                        "**Please provide your email address:**\n" +
                        "- \"Search orders shipped to New York using your.email@example.com\""
                );
            }

            // 6) Default fallback
            return Map.of(
                "search",
                "**🔍 General Order Search**\n" +
                    "I can help you search for orders, but I need more specific information to route you to the fastest tool.\n\n" +
                    "**Fastest Options (1-6 seconds):**\n" +
                    "- **Direct order lookup**: \"Find order 12345\" (1 second)\n" +
                    "- **Date range search**: \"Orders from 2024-01-01 to 2024-01-31\" (6 seconds)\n" +
                    "- **Customer orders**: \"Get orders for customer@email.com\" (6 seconds)\n\n" +
                    "**Slower Options (1-5 minutes):**\n" +
                    "- **Product search**: \"Orders containing 'battery'\" (1m 47s)\n" +
                    "- **Status search**: \"Pending orders\" (1m 45s)\n" +
                    "- **Address search**: \"Orders shipped to Dallas\" (5m+)\n\n" +
                    "**Please specify:**\n" +
                    "1. What type of search you need\n" +
                    "2. Your email address (if searching your orders)\n" +
                    "3. Any specific criteria (dates, products, status, etc.)"
            );
        } catch (Exception e) {
            return Map.of("error", "Error searching orders: " + e.getMessage());
        }
    }
    
    @Annotations.Schema(
        description = "Search for orders within a specific date range for a specific user",
        name = "searchOrdersByDateRangeTool")
    public Map<String, ?> searchOrdersByDateRangeTool(
            @Annotations.Schema(description = "Start date", name = "startDate") String startDate,
            @Annotations.Schema(description = "End date", name = "endDate") String endDate,
            @Annotations.Schema(description = "User email", name = "userEmail") String userEmail) {
        try {
            // 1) Require user email
            if (userEmail == null || userEmail.trim().isEmpty()) {
                return Map.of(
                    "response",
                    """
                    **User Identification Required**
                    
                    To search for orders by date range, I need to know which customer you are.
                    
                    **Please provide your email address:**
                    - Search my orders from 2024-01-01 to 2024-01-31 using your.email@example.com
                    - Find orders since 2024-01-01 for customer@company.com
                    - Show orders from last 7 days using myemail@domain.com
                    
                    **Use the same email address you used when placing your orders.**
                    """
                );
            }

            // 2) Look up account by email
            String accountId = null;
            String accountName = null;

            try {
                UserAccount userAccount = _commerceService.getUserAccountByEmail(userEmail);
                if (userAccount != null) {
                    accountId = userAccount.getId();
                    String fullName = ((userAccount.getFirstName() == null ? "" : userAccount.getFirstName()) + " " +
                        (userAccount.getLastName() == null ? "" : userAccount.getLastName())).trim();
                    accountName = fullName.isEmpty() ? userAccount.getEmail() : fullName;
                } else {
                    // Try to find account by email in account list (first channel)
                    List<Channel> channels = _commerceService.getChannels();
                    if (channels == null || channels.isEmpty()) {
                        return Map.of("error", "No channels available in the system.");
                    }
                    String channelId = channels.get(0).getId();
                    List<Account> accounts = _commerceService.getAccounts(channelId);

                    Account accountFound = null;
                    if (accounts != null) {
                        String emailLower = userEmail.toLowerCase(java.util.Locale.ROOT);
                        for (Account acc : accounts) {
                            String name = acc.getName() == null ? "" : acc.getName();
                            String nameLower = name.toLowerCase(java.util.Locale.ROOT);
                            if (emailLower.contains(nameLower) || nameLower.contains(emailLower)) {
                                accountFound = acc;
                                break;
                            }
                        }
                    }

                        if (accountFound != null) {
                            accountId = accountFound.getId();
                            accountName = accountFound.getName();
                        } else {
                            return Map.of(
                                "error",
                                """
**Account not found**

The email '" + userEmail + "' was not found in our system.

**Please check:**
- Email spelling and format
- If you have an account with us
- Contact customer support if you believe this is an error

**💡 Tip:** Make sure you're using the same email address you used when placing your orders.
"""
                            );
                        }

                }
            } catch (Exception ex) {
                return Map.of(
                    "error",
                    """
**Error looking up account**

There was an error looking up the email '" + userEmail + "': " + ex.getMessage() + "

**Please try:**
- Check your email address spelling
- Contact customer support if the problem persists
"""
                );
            }

            // Determine channel (first available)
            List<Channel> channels = _commerceService.getChannels();
            if (channels == null || channels.isEmpty()) {
                return Map.of("error", "No channels available in the system.");
            }
            String channelId = channels.get(0).getId();

            // 3) Parse dates (support multiple formats and relative expressions)
            java.time.OffsetDateTime startDt = null;
            java.time.OffsetDateTime endDt = null;

            java.util.List<java.time.format.DateTimeFormatter> fmts = new java.util.ArrayList<>();
            fmts.add(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            fmts.add(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            fmts.add(java.time.format.DateTimeFormatter.ofPattern("MM-dd-yyyy"));
            fmts.add(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            fmts.add(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"));

            java.util.function.Function<String, java.time.OffsetDateTime> parseFlexible = (s) -> {
                String t = s == null ? "" : s.trim();
                String tl = t.toLowerCase(java.util.Locale.ROOT);
                // Relative
                if (tl.equals("today") || tl.equals("now")) {
                    java.time.LocalDate ld = java.time.LocalDate.now();
                    return ld.atStartOfDay(java.time.ZoneId.systemDefault()).toOffsetDateTime();
                }
                if (tl.equals("yesterday")) {
                    java.time.LocalDate ld = java.time.LocalDate.now().minusDays(1);
                    return ld.atStartOfDay(java.time.ZoneId.systemDefault()).toOffsetDateTime();
                }
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("last (\\d+) days?").matcher(tl);
                if (m.find()) {
                    int days = Integer.parseInt(m.group(1));
                    java.time.LocalDate ld = java.time.LocalDate.now().minusDays(days);
                    return ld.atStartOfDay(java.time.ZoneId.systemDefault()).toOffsetDateTime();
                }
                // Try explicit formats
                for (java.time.format.DateTimeFormatter f : fmts) {
                    try {
                        if (f.toString().contains("H")) {
                            java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(t, f);
                            return ldt.atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();
                        } else {
                            java.time.LocalDate ld = java.time.LocalDate.parse(t, f);
                            return ld.atStartOfDay(java.time.ZoneId.systemDefault()).toOffsetDateTime();
                        }
                    } catch (Exception ignore) {}
                }
                throw new RuntimeException("Unable to parse date: " + s);
            };

            try {
                startDt = parseFlexible.apply(startDate);
                endDt = (endDate != null && !endDate.trim().isEmpty()) ? parseFlexible.apply(endDate) : java.time.OffsetDateTime.now();
                if (endDt.getHour() == 0 && endDt.getMinute() == 0) {
                    endDt = endDt.withHour(23).withMinute(59).withSecond(59);
                }
            } catch (RuntimeException ex) {
                return Map.of(
                    "error",
                    "**Date Parsing Error**: " + ex.getMessage() + "\n\n**Supported formats:**\n- YYYY-MM-DD (2024-01-15)\n- MM/DD/YYYY (01/15/2024)\n- MM-DD-YYYY (01-15-2024)\n- today, yesterday\n- last X days (last 7 days)\n\n**Example:** 'Search orders from 2024-01-01 to 2024-01-31'");
            }

            // Build API filter dates in ISO Z
            java.time.format.DateTimeFormatter isoZ = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(java.time.ZoneOffset.UTC);
            String startIso = isoZ.format(startDt);
            String endIso = isoZ.format(endDt);
            String filter = "createDate ge " + startIso + " and createDate le " + endIso;

            // 4) Retrieve orders with pagination limits
            java.util.List<Order> all = new java.util.ArrayList<>();
            int currentPage = 1;
            int maxPages = 3;
            int pageSize = 50;
            while (currentPage <= maxPages) {
                com.liferay.commerce.ai.chat.bot.model.PageResult<Order> pr = _commerceService.getPlacedOrdersByAccount(
                        channelId, accountId, currentPage, pageSize, "createDate:desc", filter);
                if (pr == null || pr.getItems() == null || pr.getItems().isEmpty()) {
                    break;
                }
                all.addAll(pr.getItems());
                if (currentPage >= pr.getLastPage()) {
                    break;
                }
                currentPage++;
            }

            if (all.isEmpty()) {
                java.time.format.DateTimeFormatter d = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return Map.of("error", "**No orders found** for " + userEmail + " between " + d.format(startDt) + " and " + d.format(endDt));
            }

            // Sort newest first (by orderDate)
            all.sort((a, b) -> {
                java.time.OffsetDateTime da = a.getOrderDate();
                java.time.OffsetDateTime db = b.getOrderDate();
                if (da == null && db == null) return 0;
                if (da == null) return 1;
                if (db == null) return -1;
                return db.compareTo(da);
            });

            StringBuilder result = new StringBuilder();
            result.append("**Orders Found: ").append(all.size()).append(" orders for ").append(userEmail).append("**\n");
            result.append("**Customer**: ").append(accountName == null ? "N/A" : accountName).append("\n");
            result.append("**Date Range**: ")
                  .append(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd").format(startDt))
                  .append(" to ")
                  .append(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd").format(endDt))
                  .append("\n\n");

            java.time.format.DateTimeFormatter dispFmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            int limit = Math.min(20, all.size());
            for (int i = 0; i < limit; i++) {
                Order o = all.get(i);
                String orderIdVal = o.getId() == null ? "N/A" : o.getId();
                String externalRef = "N/A"; // not exposed in DTO
                String dateStr;
                try {
                    dateStr = (o.getOrderDate() != null) ? dispFmt.format(o.getOrderDate()) : "N/A";
                } catch (Exception ignore) {
                    dateStr = "N/A";
                }
                String status = o.getStatus() == null ? "N/A" : o.getStatus();
                String total = "N/A"; // summary.totalFormatted not exposed here

                result.append(i + 1).append(". **Order ").append(orderIdVal).append("** - ").append(externalRef).append("\n");
                result.append("   Date: ").append(dateStr).append(", Status: ").append(status).append(", Total: ").append(total).append("\n\n");
            }
            if (all.size() > 20) {
                result.append("... and ").append(all.size() - 20).append(" more orders.\n");
            }
            result.append("\n**💡 Tips:**\n");
            result.append("- Use 'Find order [ID]' for detailed order information\n");
            result.append("- Use 'Get order items for order [ID]' for item details\n");
            result.append("- Use 'Get shipping info for order [ID]' for shipping details\n");

            return Map.of("response", result.toString());
        } catch (Exception e) {
            return Map.of("error", "**Error searching orders by date**: " + e.getMessage());
        }
    }
    
    @Annotations.Schema(
        description = "Search for orders containing specific products or product descriptions",
        name = "searchOrdersByProductTool")
    public Map<String, ?> searchOrdersByProductTool(
            @Annotations.Schema(description = "Product description", name = "productDescription") String productDescription,
            @Annotations.Schema(description = "User email", name = "userEmail") String userEmail) {
        try {
            // 1) Require user email
            if (userEmail == null || userEmail.trim().isEmpty()) {
                return Map.of(
                    "error",
                    """
**User Identification Required**

To search for orders by product, I need to know which customer you are.

**Please provide your email address:**
- "Search my orders for brake pads using your.email@example.com"
- "Find orders containing 'tires' for customer@company.com"
- "Show me orders with 'oil' using myemail@domain.com"

**💡 Use the same email address you used when placing your orders.**
"""
                );
            }

            // 2) Look up account by email
            String accountId = null;
            String accountName = null;
            try {
                UserAccount userAccount = _commerceService.getUserAccountByEmail(userEmail);
                if (userAccount != null) {
                    accountId = userAccount.getId();
                    String fullName = ((userAccount.getFirstName() == null ? "" : userAccount.getFirstName()) + " " +
                        (userAccount.getLastName() == null ? "" : userAccount.getLastName())).trim();
                    accountName = fullName.isEmpty() ? userAccount.getEmail() : fullName;
                } else {
                    // Try to find account by email in account list (first channel)
                    List<Channel> channels = _commerceService.getChannels();
                    if (channels == null || channels.isEmpty()) {
                        return Map.of("error", "No channels available in the system.");
                    }
                    String channelIdForAccounts = channels.get(0).getId();
                    List<Account> accounts = _commerceService.getAccounts(channelIdForAccounts);

                    Account accountFound = null;
                    if (accounts != null) {
                        String emailLower = userEmail.toLowerCase(java.util.Locale.ROOT);
                        for (Account acc : accounts) {
                            String name = acc.getName() == null ? "" : acc.getName();
                            String nameLower = name.toLowerCase(java.util.Locale.ROOT);
                            if (emailLower.contains(nameLower) || nameLower.contains(emailLower)) {
                                accountFound = acc;
                                break;
                            }
                        }
                    }

                        if (accountFound != null) {
                            accountId = accountFound.getId();
                            accountName = accountFound.getName();
                        } else {
                            return Map.of(
                                "error",
                                """
**Account not found**

The email '" + userEmail + "' was not found in our system.

**Please check:**
- Email spelling and format
- If you have an account with us
- Contact customer support if you believe this is an error

**💡 Tip:** Make sure you're using the same email address you used when placing your orders.
"""
                            );
                        }

                }
            } catch (Exception ex) {
                return Map.of(
                    "error",
                    """
**Error looking up account**

There was an error looking up the email '" + userEmail + "': " + ex.getMessage() + "

**Please try:**
- Check your email address spelling
- Contact customer support if the problem persists
"""
                );
            }

            // 3) Determine channel (first available) and fetch up to 100 orders
            List<Channel> channels = _commerceService.getChannels();
            if (channels == null || channels.isEmpty()) {
                return Map.of("error", "No channels available in the system.");
            }
            String channelId = channels.get(0).getId();

            List<Order> allOrders = new ArrayList<>();
            int page = 1;
            int pageSize = 50;
            while (allOrders.size() < 100) {
                PageResult<Order> pr = _commerceService.getPlacedOrdersByAccount(
                        channelId, accountId, page, pageSize, "createDate:desc", null);
                if (pr == null || pr.getItems() == null || pr.getItems().isEmpty()) {
                    break;
                }
                int remaining = 100 - allOrders.size();
                List<Order> items = pr.getItems();
                if (items.size() > remaining) {
                    allOrders.addAll(items.subList(0, remaining));
                } else {
                    allOrders.addAll(items);
                }
                if (page >= pr.getLastPage()) {
                    break;
                }
                page++;
            }

            if (allOrders.isEmpty()) {
                return Map.of("error", "**No orders found** for " + userEmail);
            }

            // 4) Search through orders for matching products
            String term = productDescription == null ? "" : productDescription.toLowerCase(Locale.ROOT);
            List<java.util.AbstractMap.SimpleEntry<Order, List<OrderItem>>> matches = new ArrayList<>();

            for (Order o : allOrders) {
                try {
                    String orderIdVal = o.getId() == null ? "N/A" : o.getId();
                    List<OrderItem> items = _commerceService.getPlacedOrderItems(orderIdVal);
                    if (items == null) items = new ArrayList<>();
                    List<OrderItem> matchingItems = new ArrayList<>();
                    for (OrderItem it : items) {
                        String name = it.getName() == null ? "" : it.getName().toLowerCase(Locale.ROOT);
                        String sku = it.getSku() == null ? "" : it.getSku().toLowerCase(Locale.ROOT);
                        if (name.contains(term) || sku.contains(term) || term.contains(name)) {
                            matchingItems.add(it);
                        }
                    }
                    if (!matchingItems.isEmpty()) {
                        matches.add(new java.util.AbstractMap.SimpleEntry<>(o, matchingItems));
                    }
                } catch (Exception ignore) {
                }
            }

            // Sort by order date desc
            matches.sort((a, b) -> {
                java.time.OffsetDateTime da = a.getKey().getOrderDate();
                java.time.OffsetDateTime db = b.getKey().getOrderDate();
                if (da == null && db == null) return 0;
                if (da == null) return 1;
                if (db == null) return -1;
                return db.compareTo(da);
            });

            if (matches.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("**🔍 No Orders Found**\n\n");
                sb.append("No orders found containing products matching: **\"")
                  .append(productDescription)
                  .append("\"**\n\n");
                sb.append("**Customer**: ")
                  .append(accountName == null ? "N/A" : accountName)
                  .append("\n");
                sb.append("**Search Term**: ")
                  .append(productDescription)
                  .append("\n\n");
                sb.append("**💡 Try:**\n");
                sb.append("- Different product names or keywords\n");
                sb.append("- Partial product names (e.g., \"brake\" instead of \"brake pads\")\n");
                sb.append("- SKU codes if you know them\n");
                sb.append("- More general terms (e.g., \"parts\" instead of specific part names)\n");
                return Map.of("response", sb.toString());
            }

            StringBuilder result = new StringBuilder();
            result.append("**🔍 Product Search Results: ")
                  .append(matches.size())
                  .append(" orders containing '")
                  .append(productDescription)
                  .append("'**\n");
            result.append("**Customer**: ")
                  .append(accountName == null ? "N/A" : accountName)
                  .append("\n");
            result.append("**Search Term**: ")
                  .append(productDescription)
                  .append("\n\n");

            DateTimeFormatter dispFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            int toShow = Math.min(10, matches.size());
            for (int i = 0; i < toShow; i++) {
                Order o = matches.get(i).getKey();
                List<OrderItem> matchingItems = matches.get(i).getValue();
                String orderIdVal = o.getId() == null ? "N/A" : o.getId();
                String externalRef = "N/A"; // not available in DTO
                String status = o.getStatus() == null ? "N/A" : o.getStatus();
                String total = "N/A"; // summary.totalFormatted not available
                String dateStr;
                try {
                    dateStr = (o.getOrderDate() != null) ? dispFmt.format(o.getOrderDate()) : "N/A";
                } catch (Exception ignore) {
                    dateStr = "N/A";
                }

                result.append(i + 1).append(". **Order ").append(orderIdVal).append("** - ").append(externalRef).append("\n");
                result.append("   Date: ").append(dateStr).append(", Status: ").append(status).append(", Total: ").append(total).append("\n");

                if (matchingItems != null && !matchingItems.isEmpty()) {
                    result.append("   **Matching Products:**\n");
                    int itemShow = Math.min(3, matchingItems.size());
                    for (int j = 0; j < itemShow; j++) {
                        OrderItem it = matchingItems.get(j);
                        String iname = it.getName() == null ? "Unknown Product" : it.getName();
                        String isku = it.getSku() == null ? "N/A" : it.getSku();
                        int qty = it.getQuantity();
                        result.append("   - ").append(iname).append(" (SKU: ").append(isku).append(") x").append(qty).append("\n");
                    }
                    if (matchingItems.size() > 3) {
                        result.append("   - ... and ").append(matchingItems.size() - 3).append(" more items\n");
                    }
                }

                result.append("\n");
            }

            if (matches.size() > 10) {
                result.append("... and ").append(matches.size() - 10).append(" more orders.\n\n");
            }

            result.append("**Total Orders Found**: ").append(matches.size()).append("\n");
            result.append("**Customer**: ").append(accountName == null ? "N/A" : accountName).append("\n\n");
            result.append("**💡 Tips:**\n");
            result.append("- Use 'Find order [ID]' for detailed order information\n");
            result.append("- Use 'Get order items for order [ID]' for complete item details\n");
            result.append("- Try different search terms for better results\n");

            return Map.of("response", result.toString());
        } catch (Exception e) {
            return Map.of("error", "Error searching orders by product: " + e.getMessage());
        }
    }

    @Annotations.Schema(
        description = "Enhanced search for orders with a specific status using direct order details",
        name = "searchOrdersByStatusTool")
    public Map<String, ?> searchOrdersByStatusTool(
            @Annotations.Schema(description = "Order status", name = "orderStatus") String orderStatus,
            @Annotations.Schema(description = "User email", name = "userEmail") String userEmail) {
        try {
            // Check if user email is provided
            if (userEmail == null || userEmail.trim().isEmpty()) {
                return Map.of(
                    "error",
                    """
**User Identification Required**

To search for orders by status, I need to know which customer you are.

**Please provide your email address:**
- "Search my pending orders using your.email@example.com"
- "Find shipped orders for customer@company.com"
- "Show me canceled orders using myemail@domain.com"

**Available Status Options:**
- Canceled, Completed, On Hold, Partially Shipped, Pending, Processing, Shipped

**💡 Use the same email address you used when placing your orders.**
"""
                );
            }

            // Look up account information by email using Liferay API
            String accountId;
            String accountName;
            try {
                UserAccount userAccount = _commerceService.getUserAccountByEmail(userEmail);
                if (userAccount != null) {
                    accountId = userAccount.getId();
                    String fullName = ((userAccount.getFirstName() == null ? "" : userAccount.getFirstName()) + " " +
                            (userAccount.getLastName() == null ? "" : userAccount.getLastName())).trim();
                    accountName = fullName.isEmpty() ? userAccount.getEmail() : fullName;
                } else {
                    List<Channel> channels = _commerceService.getChannels();
                    if (channels == null || channels.isEmpty()) {
                        return Map.of("error", "No channels available in the system.");
                    }
                    String channelId = channels.get(0).getId();
                    List<Account> accounts = _commerceService.getAccounts(channelId);

                    Account accountFound = null;
                    if (accounts != null) {
                        String emailLower = userEmail.toLowerCase(java.util.Locale.ROOT);
                        for (Account acc : accounts) {
                            String name = acc.getName() == null ? "" : acc.getName();
                            String nameLower = name.toLowerCase(java.util.Locale.ROOT);
                            if (emailLower.contains(nameLower) || nameLower.contains(emailLower)) {
                                accountFound = acc;
                                break;
                            }
                        }
                    }

                    if (accountFound == null) {
                        return Map.of(
                            "error",
                            """
                            **Account not found**

                            The email '" + userEmail + "' was not found in our system.

                            **Please check:**
                            - Email spelling and format
                            - If you have an account with us
                            - Contact customer support if you believe this is an error

                            **💡 Tip:** Make sure you're using the same email address you used when placing your orders.
                            """
                        );
                    } else {
                        accountId = accountFound.getId();
                        accountName = accountFound.getName();
                    }
                }
            } catch (Exception e) {
                return Map.of(
                    "error",
                    """
**Error looking up account**

There was an error looking up the email '" + userEmail + "': " + e.getMessage() + "

**Please try:**
- Check your email address spelling
- Contact customer support if the problem persists
"""
                );
            }

            // Determine channel (first available) and fetch up to 100 orders
            List<Channel> channels = _commerceService.getChannels();
            if (channels == null || channels.isEmpty()) {
                return Map.of("error", "No channels available in the system.");
            }
            String channelId = channels.get(0).getId();

            List<Order> allOrders = new ArrayList<>();
            int page = 1;
            int pageSize = 50;
            while (allOrders.size() < 100) {
                PageResult<Order> pr = _commerceService.getPlacedOrdersByAccount(
                        channelId, accountId, page, pageSize, "createDate:desc", null);
                if (pr == null || pr.getItems() == null || pr.getItems().isEmpty()) {
                    break;
                }
                int remaining = 100 - allOrders.size();
                List<Order> items = pr.getItems();
                if (items.size() > remaining) {
                    allOrders.addAll(items.subList(0, remaining));
                } else {
                    allOrders.addAll(items);
                }
                if (page >= pr.getLastPage()) {
                    break;
                }
                page++;
            }

            if (allOrders.isEmpty()) {
                return Map.of("error", "**No orders found** for " + userEmail);
            }

            // Define status mappings for intelligent matching
            java.util.Map<String, java.util.List<String>> statusMappings = new java.util.HashMap<>();
            statusMappings.put("canceled", java.util.Arrays.asList("canceled", "cancelled", "cancel"));
            statusMappings.put("completed", java.util.Arrays.asList("completed", "complete", "done"));
            statusMappings.put("on hold", java.util.Arrays.asList("on hold", "hold", "on-hold", "onhold"));
            statusMappings.put("partially shipped", java.util.Arrays.asList("partially shipped", "partial", "partially", "part shipped"));
            statusMappings.put("pending", java.util.Arrays.asList("pending", "pend", "waiting"));
            statusMappings.put("processing", java.util.Arrays.asList("processing", "process", "in process", "in progress"));
            statusMappings.put("shipped", java.util.Arrays.asList("shipped", "shipping", "delivered", "out for delivery"));

            String orderStatusLower = orderStatus == null ? "" : orderStatus.toLowerCase(java.util.Locale.ROOT).trim();
            String canonicalStatus = null;
            for (java.util.Map.Entry<String, java.util.List<String>> e : statusMappings.entrySet()) {
                for (String v : e.getValue()) {
                    if (orderStatusLower.equals(v) || orderStatusLower.contains(v)) {
                        canonicalStatus = e.getKey();
                        break;
                    }
                }
                if (canonicalStatus != null) break;
            }
            if (canonicalStatus == null) {
                canonicalStatus = orderStatusLower;
            }

            // Enhanced search using direct order details now merged into Order
            java.util.List<Order> matches = new java.util.ArrayList<>();
            for (Order o : allOrders) {
                String oid = o.getId();
                if (oid == null || oid.isEmpty()) continue;
                Order detailed = _commerceService.getOrder(oid);
                if (detailed == null) continue;
                String detailedStatus = detailed.getStatusLabel() == null ? "" : detailed.getStatusLabel().toLowerCase(java.util.Locale.ROOT).trim();
                if (canonicalStatus.equals(detailedStatus)) {
                    // Prefer createDate from Order DTO if available to mirror Python behavior
                    if (o.getOrderDate() != null) {
                        detailed.setCreateDate(o.getOrderDate().toString());
                    }
                    matches.add(detailed);
                }
            }

            // Sort by date desc
            matches.sort((a, b) -> {
                String da = a.getCreateDate() == null ? "" : a.getCreateDate();
                String db = b.getCreateDate() == null ? "" : b.getCreateDate();
                return db.compareTo(da);
            });

            // Format results
            if (matches.isEmpty()) {
                StringBuilder noRes = new StringBuilder();
                noRes.append("**No Orders Found**\n\n");
                noRes.append("No orders found with status: **\"").append(orderStatus).append("\"**\n\n");
                noRes.append("**Customer**: ").append(accountName).append("\n");
                noRes.append("**Search Status**: ").append(orderStatus).append("\n\n");
                noRes.append("**💡 Available Status Options:**\n");
                noRes.append("- **Canceled** - Orders that have been cancelled\n");
                noRes.append("- **Completed** - Orders that are fully completed\n");
                noRes.append("- **On Hold** - Orders temporarily paused\n");
                noRes.append("- **Partially Shipped** - Orders with some items shipped\n");
                noRes.append("- **Pending** - Orders awaiting processing\n");
                noRes.append("- **Processing** - Orders currently being processed\n");
                noRes.append("- **Shipped** - Orders that have been shipped\n\n");
                noRes.append("**Try:**\n");
                noRes.append("- Use any of the status names above\n");
                noRes.append("- Partial names work too (e.g., \"cancel\" for \"Canceled\")\n");
                noRes.append("- Check available statuses with \"Show me system status\"\n");
                return Map.of("response", noRes.toString());
            }

            StringBuilder result = new StringBuilder();
            result.append("**🔍 Enhanced Status Search Results: ").append(matches.size()).append(" orders with status '").append(orderStatus).append("'**\n");
            result.append("**Customer**: ").append(accountName).append("\n");
            result.append("**Status Filter**: ").append(orderStatus).append("\n\n");

            for (int i = 0; i < matches.size(); i++) {
                Order od = matches.get(i);
                String orderIdVal = od.getId() == null ? "N/A" : od.getId();
                String orderDate = (od.getCreateDate() == null || od.getCreateDate().isEmpty()) ? "N/A" : od.getCreateDate();
                String orderStatusValue = od.getStatusLabel() == null ? "N/A" : od.getStatusLabel();
                String totalAmount = od.getTotalFormatted() == null ? "N/A" : od.getTotalFormatted();
                String statusCode = od.getStatusCode();

                result.append("**").append(i + 1).append(". Order #").append(orderIdVal).append("**\n");
                result.append("   📅 **Date**: ").append(orderDate).append("\n");
                result.append("   📊 **Status**: ").append(titleCase(orderStatusValue)).append("\n");
                if (statusCode != null && !statusCode.isEmpty()) {
                    result.append("   🔢 **Status Code**: ").append(statusCode).append("\n");
                }
                result.append("   💰 **Total**: ").append(totalAmount).append("\n\n");
            }

            result.append("**💡 For detailed order info, ask**: 'Find order [ORDER_ID]'");

            return Map.of("response", result.toString());
        } catch (Exception e) {
            return Map.of("error", "Error searching orders by status: " + e.getMessage());
        }
    }

    @Annotations.Schema(
        description = "Enhanced search for orders by shipping address using shipments endpoint",
        name = "searchOrdersByShippingAddressTool")
    public Map<String, ?> searchOrdersByShippingAddressTool(
            @Annotations.Schema(description = "Address query", name = "addressQuery") String addressQuery,
            @Annotations.Schema(description = "User email", name = "userEmail") String userEmail) {
        try {
            if (userEmail == null || userEmail.trim().isEmpty()) {
                return Map.of(
                    "error",
                    """
**User Identification Required**

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
"""
                );
            }

            String accountId;
            String accountName;
            try {
                UserAccount userAccount = _commerceService.getUserAccountByEmail(userEmail);
                if (userAccount != null) {
                    accountId = userAccount.getId();
                    String fullName = ((userAccount.getFirstName() == null ? "" : userAccount.getFirstName()) + " " +
                        (userAccount.getLastName() == null ? "" : userAccount.getLastName())).trim();
                    accountName = fullName.isEmpty() ? userAccount.getEmail() : fullName;
                } else {
                    List<Channel> channels = _commerceService.getChannels();
                    if (channels == null || channels.isEmpty()) {
                        return Map.of("error", "No channels available in the system.");
                    }
                    String channelId = channels.get(0).getId();
                    List<Account> accounts = _commerceService.getAccounts(channelId);

                    Account accountFound = null;
                    if (accounts != null) {
                        String emailLower = userEmail.toLowerCase(java.util.Locale.ROOT);

                        for (Account account : accounts) {
                            String name = account.getName() == null ? "" : account.getName();
                            String nameLower = name.toLowerCase(java.util.Locale.ROOT);
                            if (emailLower.contains(nameLower) || nameLower.contains(emailLower)) {
                                accountFound = account;
                                break;
                            }
                        }
                    }

                    if (accountFound == null) {
                        return Map.of("error", "**Account not found**\n\nThe email '" + userEmail + "' was not found in our system.");
                    }

                    accountId = accountFound.getId();
                    accountName = accountFound.getName();
                }
            } catch (Exception ex) {
                return Map.of("error", "**Error looking up account**\n\nThere was an error looking up the email '" + userEmail + "': " + ex.getMessage());
            }

            List<Channel> channels = _commerceService.getChannels();
            if (channels == null || channels.isEmpty()) {
                return Map.of("error", "No channels available in the system.");
            }
            String channelId = channels.get(0).getId();

            List<Order> allOrders = new ArrayList<>();
            int page = 1;
            int pageSize = 50;
            while (true) {
                PageResult<Order> pr = _commerceService.getPlacedOrdersByAccount(
                        channelId, accountId, page, pageSize, "createDate:desc", null);
                if (pr == null || pr.getItems() == null || pr.getItems().isEmpty()) {
                    break;
                }
                allOrders.addAll(pr.getItems());
                if (page >= pr.getLastPage()) {
                    break;
                }
                page++;
            }

            if (allOrders.isEmpty()) {
                return Map.of("error", "**No orders found** for " + userEmail);
            }

            List<OrderShipmentDetails> matchingOrders = new ArrayList<>();
            String addressQueryLower = addressQuery == null ? "" : addressQuery.toLowerCase(Locale.ROOT).trim();

            for (Order order : allOrders) {
                String orderId = order.getId();
                if (orderId == null || orderId.isEmpty()) {
                    continue;
                }

                List<Shipment> shipments = _commerceService.getOrderShipmentsDto(orderId);

                for (Shipment shipment : shipments) {
                    String oneLineAddress = shipment.getOneLineAddress();
                    if (oneLineAddress == null || oneLineAddress.isEmpty()) {
                        continue;
                    }

                    if (oneLineAddress.toLowerCase(Locale.ROOT).contains(addressQueryLower)) {
                        Order detailedOrder = _commerceService.getOrder(orderId);

                        OrderShipmentDetails orderShipmentDetails = new OrderShipmentDetails();

                        orderShipmentDetails.setId(orderId);
                        orderShipmentDetails.setCreateDate(order.getCreateDate() != null ? order.getCreateDate() : "N/A");
                        orderShipmentDetails.setOneLineAddress(oneLineAddress);
                        orderShipmentDetails.setShipmentStatus(shipment.getStatus() != null && shipment.getStatus().getLabel() != null ? shipment.getStatus().getLabel() : "N/A");
                        orderShipmentDetails.setShippingDate(shipment.getShippingDate() != null ? shipment.getShippingDate() : "N/A");
                        orderShipmentDetails.setExpectedDate(shipment.getExpectedDate() != null ? shipment.getExpectedDate() : "N/A");
                        orderShipmentDetails.setTrackingNumber(shipment.getTrackingNumber() != null ? shipment.getTrackingNumber() : "N/A");
                        orderShipmentDetails.setCarrier(shipment.getCarrier() != null ? shipment.getCarrier() : "N/A");
                        orderShipmentDetails.setTotalFormatted(detailedOrder != null && detailedOrder.getTotalFormatted() != null ? detailedOrder.getTotalFormatted() : "N/A");

                        matchingOrders.add(orderShipmentDetails);
                        break;
                    }
                }
            }

            matchingOrders.sort((a, b) -> {
                String da = a.getCreateDate() == null ? "" : a.getCreateDate();
                String db = b.getCreateDate() == null ? "" : b.getCreateDate();
                return db.compareTo(da);
            });

            if (matchingOrders.isEmpty()) {
                return Map.of(
                    "error",
                    """
**No Orders Found**

No orders found with shipping address containing: **\"""" + addressQuery + """
"**

**Customer**: """ + accountName + """

**Address Search**: """ + addressQuery + """


**Try These Search Terms:**
- **City**: "New York", "Los Angeles", "Chicago"
- **State**: "CA", "NY", "TX", "California", "New York"
- **Street**: "Main Street", "Oak Avenue", "123"
- **Postal Code**: "90210", "10001", "60601"
- **Country**: "US", "United States", "Canada"
- **Partial matches**: "Main", "Ave", "St"

**Examples:**
- "Search orders shipped to New York using your.email@example.com"
- "Find orders with address containing 'Main Street' for customer@company.com"
"""
                );
            }

            StringBuilder result = new StringBuilder();
            result.append("**🔍 Enhanced Address Search Results: ").append(matchingOrders.size()).append(" orders with address containing '").append(addressQuery).append("'**\n");
            result.append("**Customer**: ").append(accountName).append("\n");
            result.append("**Address Filter**: ").append(addressQuery).append("\n\n");

            for (int i = 0; i < matchingOrders.size(); i++) {
                OrderShipmentDetails order = matchingOrders.get(i);
                String orderIdVal = order.getId() != null ? order.getId() : "N/A";
                String orderDate = order.getCreateDate();
                String oneLineAddress = order.getOneLineAddress() != null ? order.getOneLineAddress() : "N/A";
                String shipmentStatus = order.getShipmentStatus();
                String totalAmount = order.getTotalFormatted();
                String shippingDate = order.getShippingDate();
                String trackingNumber = order.getTrackingNumber();

                result.append("**").append(i + 1).append(". Order #").append(orderIdVal).append("**\n");
                result.append("**Date**: ").append(orderDate).append("\n");
                result.append("**Shipment Status**: ").append(titleCase(shipmentStatus)).append("\n");
                result.append("**Total**: ").append(totalAmount).append("\n");
                result.append("**Address**: ").append(oneLineAddress).append("\n");
                if (!"N/A".equals(shippingDate)) {
                    result.append("   🚚 **Shipped**: ").append(shippingDate).append("\n");
                }
                if (trackingNumber != null && !"N/A".equals(trackingNumber)) {
                    result.append("   📦 **Tracking**: ").append(trackingNumber).append("\n");
                }
                result.append("\n");
            }

            result.append("**For detailed shipping info, ask**: 'Get shipping info for order [ORDER_ID]'");

            return Map.of("response", result.toString());
        } catch (Exception e) {
            return Map.of("error", "Error retrieving orders: " + e.getMessage());
        }
    }

    @Annotations.Schema(
        description = "Get frequently asked questions and answers from the official FAQ",
        name = "getFaqInformationTool")
    public Map<String, ?> getFaqInformationTool(
            @Annotations.Schema(description = "FAQ query (leave empty to list categories)", name = "query") String query) {
        try {
            // Official FAQ content from https://webserver-lct66degrees-uat.lfr.cloud/web/minium-demo/faq
            Map<String, Map<String, String>> faqData = new LinkedHashMap<>();

            // ordering
            Map<String, String> ordering = new LinkedHashMap<>();
            ordering.put("How do I place an order?", "To place an order, browse our selection by vehicle make/model, part category, or using our search bar. Add the desired items to your cart, then proceed to checkout. Follow the prompts to enter your shipping information and payment details to complete your purchase.");
            ordering.put("Do I need an account to place an order?", "No, you can check out as a guest. However, creating an account allows you to track your order history, save multiple shipping addresses, and enjoy a faster checkout process on future purchases.");
            faqData.put("ordering", ordering);

            // payment
            Map<String, String> payment = new LinkedHashMap<>();
            payment.put("What payment methods do you accept?", "We accept all major credit cards (Visa, MasterCard, American Express, Discover), PayPal, and Google Pay. All transactions are securely processed.");
            payment.put("Is my payment information secure?", "Absolutely. We use industry-standard SSL encryption and PCI-compliant payment gateways to protect your personal and payment information. Your data is never stored on our servers.");
            faqData.put("payment", payment);

            // shipping
            Map<String, String> shipping = new LinkedHashMap<>();
            shipping.put("What are your shipping options and costs?", "We offer several shipping options, including Standard, Expedited, and Overnight delivery. Shipping costs are calculated at checkout based on your location, the weight/size of your order, and the chosen shipping speed.");
            shipping.put("How long will it take for my order to arrive?", "Standard Shipping: Typically 3-7 business days. Expedited Shipping: Typically 2-3 business days. Overnight Shipping: 1 business day (orders must be placed by 2 PM PST for same-day dispatch). Please note that these are estimates and may vary based on product availability and carrier delays.");
            shipping.put("Do you ship internationally?", "Yes, we ship to select international destinations. International shipping costs and delivery times vary significantly. Please enter your address at checkout to see available options and costs for your country. Customers are responsible for all customs duties, taxes, and fees.");
            shipping.put("How can I track my order?", "Once your order ships, you will receive a shipping confirmation email with a tracking number. You can click on the link in the email or enter your tracking number on our 'Track Your Order' page.");
            faqData.put("shipping", shipping);

            // order_management
            Map<String, String> orderManagement = new LinkedHashMap<>();
            orderManagement.put("Can I change or cancel my order after it's placed?", "We process orders quickly to ensure fast delivery. If you need to change or cancel, please contact us immediately by phone or email. We will do our best to accommodate your request if the order has not yet been shipped.");
            orderManagement.put("What if my package is lost or damaged?", "Please contact our customer support within 48 hours of the expected delivery date for lost packages, or immediately upon receipt for damaged items. We will initiate a claim with the carrier and arrange for a replacement or refund as quickly as possible.");
            faqData.put("order_management", orderManagement);

            // returns
            Map<String, String> returns = new LinkedHashMap<>();
            returns.put("What is your return policy?", "We offer a 30-day return policy for most unused parts in their original, unopened packaging. Some exceptions apply (e.g., electrical components, custom orders, final sale items). Please see our full Return Policy for complete details.");
            returns.put("How do I return a part?", "To initiate a return, please visit our Returns Portal or contact customer support to receive an RMA (Return Merchandise Authorization) number and detailed instructions. Do not send items back without an RMA.");
            returns.put("How long does it take to process a refund?", "Once we receive your returned item and inspect it, refunds are typically processed within 5-7 business days. The refund will be issued to your original payment method. Please note that it may take additional time for the refund to appear on your bank statement.");
            returns.put("Are there any non-returnable items?", "Yes, certain items are non-returnable for safety or hygiene reasons, or if they are custom-made or marked as 'final sale.' This often includes used parts, opened electrical components, or parts that have been installed. Please refer to our full Return Policy for the complete list.");
            faqData.put("returns", returns);

            // parts
            Map<String, String> parts = new LinkedHashMap<>();
            parts.put("How do I find the right part for my vehicle?", "You can use our 'Vehicle Selector' tool on the homepage by entering your Year, Make, and Model. Our search results will then filter for compatible parts. You can also search by VIN number, OEM part number, or part name.");
            parts.put("What if I can't find the part I need?", "If you're having trouble locating a specific part, please contact our parts specialists. Provide your vehicle's VIN and as much detail about the part as possible, and we'll do our best to help you find it or suggest alternatives.");
            parts.put("Are your parts new or used?", "Unless explicitly stated otherwise (e.g., in a 'Used Parts' or 'Salvage' section), all products sold on our website are brand new from the manufacturer.");
            parts.put("Do your parts come with a warranty?", "Many of our parts come with a manufacturer's warranty. Warranty terms vary by manufacturer and part. Please check the individual product page for specific warranty information. For warranty claims, please contact our support team.");
            parts.put("Do you offer technical support for installation?", "While we sell parts, we are not certified mechanics and cannot provide specific installation advice or instructions. We recommend consulting a qualified mechanic or referring to your vehicle's service manual for proper installation procedures.");
            parts.put("Do you provide installation instructions?", "Some manufacturers include basic installation guides with their parts. However, for detailed instructions, we strongly advise referring to your vehicle's factory service manual or seeking professional automotive assistance.");
            faqData.put("parts", parts);

            // account
            Map<String, String> account = new LinkedHashMap<>();
            account.put("How do I reset my password?", "Click on the 'Login' button at the top of the page, then click 'Forgot Password?'. Enter your registered email address, and we'll send you a link to reset your password.");
            account.put("How do I update my account information?", "Log in to your account, and navigate to the 'My Account' or 'Account Settings' section. From there, you can update your personal details, shipping addresses, and payment methods.");
            faqData.put("account", account);

            // support
            Map<String, String> support = new LinkedHashMap<>();
            support.put("How can I contact customer service?", "You can reach us by: **Phone:** [Your Phone Number] (Mon-Fri, [Hours of Operation]) **Email:** [Your Support Email] (We aim to respond within 24 business hours) **Live Chat:** Available on our website during business hours.");
            support.put("Do you offer a trade discount for mechanics/shops?", "Yes, we offer special pricing and programs for registered automotive businesses and mechanics. Please visit our 'Trade Program' page or contact our B2B sales team for more information.");
            faqData.put("support", support);

            // general
            Map<String, String> general = new LinkedHashMap<>();
            general.put("Can I pick up my order in person?", "No, currently all orders are processed and shipped from our distribution centers. We do not offer local pickup services.");
            faqData.put("general", general);

            String q = (query == null) ? "" : query;
            if (q.isEmpty()) {
                StringBuilder result = new StringBuilder();
                result.append("**📚 Frequently Asked Questions**\n\n");
                result.append("Here are the main categories of questions we can help with:\n\n");

                for (Map.Entry<String, Map<String, String>> entry : faqData.entrySet()) {
                    String categoryName = titleCase(entry.getKey().replace('_', ' '));
                    result.append("**").append(categoryName).append(":**\n");
                    for (String questionText : entry.getValue().keySet()) {
                        result.append("- ").append(questionText).append("\n");
                    }
                    result.append("\n");
                }

                result.append("**💡 How to use:** Ask me about any of these topics, and I'll provide the official answer!\n");
                result.append("**Example:** 'What is your return policy?' or 'How do I track my order?'");

                return Map.of("response", result.toString());
            }

            String queryLower = q.toLowerCase(java.util.Locale.ROOT);
            java.util.List<Map<String, String>> matchingAnswers = new java.util.ArrayList<>();

            for (Map.Entry<String, Map<String, String>> entry : faqData.entrySet()) {
                String category = titleCase(entry.getKey().replace('_', ' '));
                for (Map.Entry<String, String> qa : entry.getValue().entrySet()) {
                    String questionText = qa.getKey();
                    String answerText = qa.getValue();
                    boolean questionMatches = questionText.toLowerCase(java.util.Locale.ROOT).contains(queryLower);

                    boolean answerMatches = false;
                    String[] words = queryLower.split("\\s+");
                    for (String w : words) {
                        if (w.length() > 3 && answerText.toLowerCase(java.util.Locale.ROOT).contains(w)) {
                            answerMatches = true;
                            break;
                        }
                    }

                    if (questionMatches || answerMatches) {
                        Map<String, String> match = new LinkedHashMap<>();
                        match.put("question", questionText);
                        match.put("answer", answerText);
                        match.put("category", category);
                        matchingAnswers.add(match);
                    }
                }
            }

            if (matchingAnswers.isEmpty()) {
                return Map.of(
                    "error",
                    "**FAQ Search Results**\n\n" +
                        "I couldn't find specific information about \"" + q + "\" in our FAQ database.\n\n" +
                        "**Try these alternatives:**\n" +
                        "- Ask about specific topics like \"return policy\", \"shipping\", \"payment\", etc.\n" +
                        "- Use \"Get FAQ information\" to see all available topics\n" +
                        "- Contact customer support for specific questions\n\n" +
                        "**Popular topics:**\n" +
                        "- Ordering and checkout\n" +
                        "- Shipping and delivery\n" +
                        "- Returns and refunds\n" +
                        "- Parts and compatibility\n" +
                        "- Account management"
                );
            }

            StringBuilder result = new StringBuilder();
            result.append("**FAQ Search Results for: '").append(q).append("'**\n\n");

            int limit = Math.min(5, matchingAnswers.size());
            for (int i = 0; i < limit; i++) {
                Map<String, String> match = matchingAnswers.get(i);
                result.append("**").append(i + 1).append(". ").append(match.get("question")).append("**\n");
                result.append("*Category: ").append(match.get("category")).append("*\n");
                result.append(match.get("answer")).append("\n\n");
            }

            if (matchingAnswers.size() > 5) {
                result.append("... and ").append(matchingAnswers.size() - 5).append(" more results.\n\n");
            }

            result.append("**Source:** [Official FAQ](https://webserver-lct66degrees-uat.lfr.cloud/web/minium-demo/faq)");

            return Map.of("response", result.toString());
        } catch (Exception e) {
            return Map.of("error", "**Error retrieving FAQ information**: " + e.getMessage());
        }
    }
    
    @Annotations.Schema(
        description = "Get orders for a specific customer email",
        name = "getCustomerOrdersTool")
    public Map<String, ?> getCustomerOrdersTool(
            @Annotations.Schema(description = "Customer email", name = "email") String email) {
        try {
            String accountId;
            String accountName;

            // Lookup account information by email using Liferay API
            try {
                // First, try to find userAccount by email
                UserAccount userAccount = _commerceService.getUserAccountByEmail(email);
                if (userAccount != null) {
                    accountId = userAccount.getId();
                    String fullName = ((userAccount.getFirstName() == null ? "" : userAccount.getFirstName()) + " " +
                            (userAccount.getLastName() == null ? "" : userAccount.getLastName())).trim();
                    accountName = fullName.isEmpty() ? userAccount.getEmail() : fullName;
                } else {
                    // If no customer found, try to find account by email in account list
                    List<Channel> channels = _commerceService.getChannels();
                    if (channels == null || channels.isEmpty()) {
                        return Map.of("error", "No channels found");
                    }
                    String channelId = channels.get(0).getId();
                    List<Account> accounts = _commerceService.getAccounts(channelId);

                    Account accountFound = null;
                    if (accounts != null) {
                        String emailLower = email == null ? "" : email.toLowerCase(java.util.Locale.ROOT);
                        for (Account acc : accounts) {
                            String name = acc.getName() == null ? "" : acc.getName();
                            String nameLower = name.toLowerCase(java.util.Locale.ROOT);
                            if (emailLower.contains(nameLower) || nameLower.contains(emailLower)) {
                                accountFound = acc;
                                break;
                            }
                        }
                    }

                    if (accountFound == null) {
                            return Map.of(
                                "error",
                                "**Account not found**\n\n" +
                                    "The email '" + email + "' was not found in our system.\n\n" +
                                    "**Please check:**\n" +
                                    "- Email spelling and format\n" +
                                    "- If you have an account with us\n" +
                                    "- Contact customer support if you believe this is an error\n\n" +
                                    "**💡 Tip:** Make sure you're using the same email address you used when placing your orders."
                            );
                    } else {
                        accountId = accountFound.getId();
                        accountName = accountFound.getName();
                    }
                }
            } catch (Exception e) {
                return Map.of(
                    "error",
                    "**Error looking up account**\n\n" +
                        "There was an error looking up the email '" + email + "': " + e.getMessage() + "\n\n" +
                        "**Please try:**\n" +
                        "- Check your email address spelling\n" +
                        "- Contact customer support if the problem persists"
                );
            }

            // Get orders for this account
            List<Channel> channels = _commerceService.getChannels();
            if (channels == null || channels.isEmpty()) {
                return Map.of("error", "No channels available in the system.");
            }
            String channelId = channels.get(0).getId();
            List<Order> orders;
            try {
                orders = _commerceService.getAllPlacedOrdersByAccountDto(channelId, accountId);
            } catch (Exception ex) {
                return Map.of("error", "**Error retrieving orders for " + email + "**: " + ex.getMessage());
            }
            if (orders == null || orders.isEmpty()) {
                return Map.of("error", "I found the account for '" + email + "' but no orders are available.");
            }

            // Sort orders by date (newest first) using createDate string
            orders.sort((a, b) -> {
                String da = a.getCreateDate() == null ? "" : a.getCreateDate();
                String db = b.getCreateDate() == null ? "" : b.getCreateDate();
                return db.compareTo(da);
            });

            // Format the response
            if (orders.size() == 1) {
                Order order = orders.get(0);
                String id = order.getId() == null ? "N/A" : order.getId();
                String ref = order.getExternalReferenceCode() == null ? "N/A" : order.getExternalReferenceCode();
                String date = order.getCreateDate() == null ? "N/A" : order.getCreateDate();
                String status = order.getStatus() == null ? "N/A" : order.getStatus();
                String total = order.getTotalFormatted() == null ? "N/A" : order.getTotalFormatted();
                return Map.of(
                    "orders",
                    "**Customer Orders for " + email + "**\n" +
                        "**Account**: " + accountName + " (ID: " + accountId + ")\n\n" +
                        "**Found 1 order:**\n" +
                        "- **Order ID**: " + id + "\n" +
                        "- **Reference**: " + ref + "\n" +
                        "- **Date**: " + date + "\n" +
                        "- **Status**: " + status + "\n" +
                        "- **Total**: " + total
                );
            } else {
                StringBuilder result = new StringBuilder();
                result.append("**Customer Orders for ").append(email).append("**\n");
                result.append("**Account**: ").append(accountName).append(" (ID: ").append(accountId).append(")\n\n");
                result.append("**Found ").append(orders.size()).append(" orders:**");

                int toShow = Math.min(10, orders.size());
                for (int i = 0; i < toShow; i++) {
                    Order o = orders.get(i);
                    String orderIdVal = o.getId() == null ? "N/A" : o.getId();
                    String createDate = o.getCreateDate() == null ? "N/A" : o.getCreateDate();
                    String status = o.getStatus() == null ? "N/A" : o.getStatus();
                    String total = o.getTotalFormatted() == null ? "N/A" : o.getTotalFormatted();

                    result.append("\n").append(i + 1).append(". **Order ").append(orderIdVal).append("** - ").append(orderIdVal);
                    result.append("\n   Date: ").append(createDate).append(", Status: ").append(status).append(", Total: ").append(total);
                }

                if (orders.size() > 10) {
                    result.append("\n\n... and ").append(orders.size() - 10).append(" more orders.");
                }

                result.append("\n\n**Total Orders**: ").append(orders.size());
                result.append("\n**Account**: ").append(accountName);

                return Map.of("orders", result.toString());
            }
        } catch (Exception e) {
            return Map.of("error", "Error getting customer orders: " + e.getMessage());
        }
    }

    @Annotations.Schema(
        description = "Get information about customer email lookup",
        name = "getTestEmailsTool")
    public Map<String, ?> getTestEmailsTool() {
        return Map.of(
            "result",
            """
**Customer Email Lookup Information:**

**How Customer Lookup Works:**
- Enter any email address that exists in our system
- The system will automatically find the customer's account
- Orders will be retrieved for that specific customer only

**How to Use:**
1. **Customer Order Lookup**: "Get my orders using your.email@example.com"
2. **Order Search**: "Search for orders customer@company.com"
3. **Date Range Search**: "Search orders from 2024-01-01 to 2024-01-31 using myemail@domain.com"

**What You Need:**
- A valid email address that exists in our customer database
- The same email address used when placing orders
- Proper email format (e.g., user@domain.com)

**If Email Not Found:**
- Check spelling and format
- Verify the email exists in our system
- Contact customer support if needed
"""
        );
    }

    @Annotations.Schema(
        description = "Get detailed item information for a specific order",
        name = "getOrderItemsTool")
    public Map<String, ?> getOrderItemsTool(
        @Annotations.Schema(description = "Order ID", name = "orderId") String orderId) {
        try {
            // Get order items using DTOs
            java.util.List<OrderItem> orderItems;
            try {
                java.util.List<OrderItem> itemsResponse = _commerceService.getPlacedOrderItems(orderId);
                if (itemsResponse != null) {
                    orderItems = itemsResponse;
                } else {
                    orderItems = new java.util.ArrayList<>();
                }
            } catch (Exception apiError) {
                return Map.of(
                    "error",
                    """
**🔍 Order Items Lookup Failed**

**Order ID**: """ + orderId + "\n" +
                        "**Error**: " + apiError.getMessage() + "\n\n" +
                        "**Possible Causes:**\n" +
                        "1. The order may not have items in the system\n" +
                        "2. The Liferay API endpoint may be unavailable\n" +
                        "3. There may be an authentication issue\n\n" +
                        "**💡 Try These Alternatives:**\n" +
                        "- \"Find order " + orderId + "\" - Get complete order details\n" +
                        "- \"Search for orders\" - Browse available orders\n" +
                        "- Check if the order exists in the system"
                );
            }

            if (orderItems.isEmpty()) {
                return Map.of(
                    "error",
                    """
**📦 Order Items for Order """ + orderId + "**\n\n" +
                        "**Status**: Order exists but has no items\n" +
                        "**Items Count**: 0\n\n" +
                        "**💡 Try**: \"Find order " + orderId + "\" for complete order information"
                );
            }

            // Get order info via DTO
            Order order = null;
            try { order = _commerceService.getOrder(orderId); } catch (Exception ignore) {}

            // Build the response
            StringBuilder result = new StringBuilder();
            result.append("**📦 Order Items for Order ").append(orderId).append("**\n\n");

            if (order != null) {
                String externalRef = order.getExternalReferenceCode() == null ? "N/A" : order.getExternalReferenceCode();
                String createDate = order.getCreateDate() == null ? "N/A" : order.getCreateDate();
                String totalAmount = order.getTotalFormatted() == null ? "N/A" : order.getTotalFormatted();
                result.append("**Order Details:** ").append(externalRef)
                      .append(" | Date: ").append(createDate)
                      .append(" | Total: ").append(totalAmount)
                      .append("\n\n");

                // Try to get shipping address from shipments API (DTOs)
                try {
                    java.util.List<Shipment> shipments = _commerceService.getOrderShipmentsDto(orderId);
                    if (shipments != null && !shipments.isEmpty()) {
                        Shipment latestShipment = shipments.get(0);
                        result.append("**Shipping Address:**\n");
                        result.append("- **Address**: ")
                              .append(latestShipment.getOneLineAddress() == null ? "N/A" : latestShipment.getOneLineAddress())
                              .append("\n");
                        result.append("- **Shipping Date**: ")
                              .append(latestShipment.getShippingDate() == null ? "N/A" : latestShipment.getShippingDate())
                              .append("\n");
                        result.append("- **Tracking Number**: ")
                              .append(latestShipment.getTrackingNumber() == null ? "N/A" : latestShipment.getTrackingNumber())
                              .append("\n");
                        result.append("- **Carrier**: ")
                              .append(latestShipment.getCarrier() == null ? "N/A" : latestShipment.getCarrier())
                              .append("\n");
                        Shipment.Status statusInfo = latestShipment.getStatus();
                        if (statusInfo != null) {
                            result.append("- **Status**: ")
                                  .append(statusInfo.getLabel() == null ? "N/A" : statusInfo.getLabel())
                                  .append("\n\n");
                        } else {
                            String st = latestShipment.getShipmentStatus();
                            result.append("- **Status**: ")
                                  .append(st == null ? "N/A" : st)
                                  .append("\n\n");
                        }
                    } else {
                        result.append("**Shipping Address**: Not available\n\n");
                    }
                } catch (Exception e) {
                    result.append("**Shipping Address**: Not available\n\n");
                }
            }

            result.append("**Found ").append(orderItems.size()).append(" items:**\n\n");

            for (int i = 0; i < orderItems.size(); i++) {
                OrderItem item = orderItems.get(i);
                String itemName = (item.getName() == null || item.getName().isEmpty()) ? "Unknown Product" : item.getName();
                String itemSku = (item.getSku() == null || item.getSku().isEmpty()) ? "N/A" : item.getSku();
                String itemQuantity = String.valueOf(item.getQuantity());
                String itemPrice = (item.getTotalPrice() > 0)
                        ? String.format(java.util.Locale.ROOT, "$ %.2f", item.getTotalPrice())
                        : "N/A";

                result.append(i + 1).append(". **").append(itemName).append("**\n");
                result.append("   SKU: ").append(itemSku)
                      .append(" | Qty: ").append(itemQuantity)
                      .append(" | Price: ").append(itemPrice)
                      .append("\n\n");
            }

            return Map.of("response", result.toString());
        } catch (Exception e) {
            return Map.of(
                "error",
                """
**Unexpected Error**

**Order ID**: """ + orderId + "\n" +
                    "**Error**: " + e.getMessage() + "\n" +
                    "**Error Type**: " + e.getClass().getSimpleName() + "\n\n" +
                    "**💡 Try These Alternatives:**\n" +
                    "- \"Find order " + orderId + "\" - Get complete order details\n" +
                    "- \"Search for orders\" - Browse available orders\n" +
                    "- Check system status with \"Show me system status\""
            );
        }
    }

    @Annotations.Schema(
        description = "Get shipping information for a specific order with enhanced address details",
        name = "getOrderShippingTool")
    public Map<String, ?> getOrderShippingTool(
        @Annotations.Schema(description = "Order ID", name = "orderId") String orderId) {
        try {
            // Use DTOs instead of raw JSON
            Order order = _commerceService.getOrder(orderId);
            if (order == null) {
                return Map.of("error", "Order " + orderId + " not found or not accessible.");
            }

            // Extract shipping information from DTO
            String shippingValue = order.getShippingValueFormatted() == null ? "N/A" : order.getShippingValueFormatted();
            String shippingDiscount = order.getShippingDiscountValueFormatted() == null ? "N/A" : order.getShippingDiscountValueFormatted();
            String subtotal = order.getSubtotalFormatted() == null ? "N/A" : order.getSubtotalFormatted();
            String taxValue = order.getTaxValueFormatted() == null ? "N/A" : order.getTaxValueFormatted();
            String total = order.getTotalFormatted() == null ? "N/A" : order.getTotalFormatted();

            // Get enhanced shipping address from shipments API (DTOs)
            List<Shipment> shipments = null;
            try {
                shipments = _commerceService.getOrderShipmentsDto(orderId);
            } catch (Exception ignore) {
                shipments = new ArrayList<>();
            }

            Shipment latestShipment = null;
            if (shipments != null && !shipments.isEmpty()) {
                latestShipment = shipments.get(0); // Shipments are typically sorted by date
            }

            // Get basic addresses from order (fallback) via DTO maps
            java.util.Map<String, String> shippingAddress = order.getShippingAddress();
            java.util.Map<String, String> billingAddress = order.getBillingAddress();

            // Build the response
            StringBuilder result = new StringBuilder();
            result.append("**Shipping Information for Order ").append(orderId).append("**\n\n");

            // Order summary
            result.append("**Order Summary:**\n");
            result.append("- **Subtotal**: ").append(subtotal).append("\n");
            result.append("- **Shipping Cost**: ").append(shippingValue).append("\n");
            if (!"N/A".equals(shippingDiscount) && !"$ 0.00".equals(shippingDiscount)) {
                result.append("- **Shipping Discount**: ").append(shippingDiscount).append("\n");
            }
            result.append("- **Tax**: ").append(taxValue).append("\n");
            result.append("- **Total**: ").append(total).append("\n\n");

            // Enhanced shipping address from shipments
            if (latestShipment != null && latestShipment.getOneLineAddress() != null && !latestShipment.getOneLineAddress().equals("N/A")) {
                result.append("**Shipping Address (from Shipments):**\n");
                result.append("- **Address**: ")
                      .append(latestShipment.getOneLineAddress() == null ? "N/A" : latestShipment.getOneLineAddress())
                      .append("\n");
                result.append("- **Shipping Date**: ")
                      .append(latestShipment.getShippingDate() == null ? "N/A" : latestShipment.getShippingDate())
                      .append("\n");
                result.append("- **Tracking Number**: ")
                      .append(latestShipment.getTrackingNumber() == null ? "N/A" : latestShipment.getTrackingNumber())
                      .append("\n");
                result.append("- **Carrier**: ")
                      .append(latestShipment.getCarrier() == null ? "N/A" : latestShipment.getCarrier())
                      .append("\n");
                String statusStr = null;
                if (latestShipment.getStatus() != null && latestShipment.getStatus().getLabel() != null) {
                    statusStr = latestShipment.getStatus().getLabel();
                } else {
                    statusStr = latestShipment.getShipmentStatus();
                }
                result.append("- **Status**: ")
                      .append(statusStr == null ? "N/A" : statusStr)
                      .append("\n\n");
            } else if (shippingAddress != null && !shippingAddress.isEmpty()) {
                result.append("**Shipping Address (from Order):**\n");
                result.append("- **Name**: ").append(shippingAddress.getOrDefault("name", "N/A")).append("\n");
                result.append("- **Street**: ").append(shippingAddress.getOrDefault("street1", "N/A")).append("\n");
                String street2 = shippingAddress.get("street2");
                if (street2 != null && !street2.isEmpty()) {
                    result.append("- **Street 2**: ").append(street2).append("\n");
                }
                result.append("- **City**: ").append(shippingAddress.getOrDefault("city", "N/A")).append("\n");
                result.append("- **State/Province**: ").append(shippingAddress.getOrDefault("regionISOCode", "N/A")).append("\n");
                result.append("- **Postal Code**: ").append(shippingAddress.getOrDefault("zip", "N/A")).append("\n");
                result.append("- **Country**: ").append(shippingAddress.getOrDefault("countryISOCode", "N/A")).append("\n");
                result.append("- **Phone**: ").append(shippingAddress.getOrDefault("phoneNumber", "N/A")).append("\n\n");
            } else {
                result.append("**Shipping Address**: Not available\n\n");
            }

            // Billing address
            if (billingAddress != null && !billingAddress.isEmpty()) {
                result.append("**Billing Address:**\n");
                result.append("- **Name**: ").append(billingAddress.getOrDefault("name", "N/A")).append("\n");
                result.append("- **Street**: ").append(billingAddress.getOrDefault("street1", "N/A")).append("\n");
                String billStreet2 = billingAddress.get("street2");
                if (billStreet2 != null && !billStreet2.isEmpty()) {
                    result.append("- **Street 2**: ").append(billStreet2).append("\n");
                }
                result.append("- **City**: ").append(billingAddress.getOrDefault("city", "N/A")).append("\n");
                result.append("- **State/Province**: ").append(billingAddress.getOrDefault("regionISOCode", "N/A")).append("\n");
                result.append("- **Postal Code**: ").append(billingAddress.getOrDefault("zip", "N/A")).append("\n");
                result.append("- **Country**: ").append(billingAddress.getOrDefault("countryISOCode", "N/A")).append("\n");
                result.append("- **Phone**: ").append(billingAddress.getOrDefault("phoneNumber", "N/A")).append("\n\n");
            } else {
                result.append("**💳 Billing Address**: Not available\n\n");
            }

            // Additional shipping info
            result.append("**Additional Information:**\n");
            result.append("- **Order Date**: ").append(order.getCreateDate() == null ? "N/A" : order.getCreateDate()).append("\n");
            result.append("- **Order Status**: ").append(order.getStatus() == null ? "N/A" : order.getStatus()).append("\n");
            result.append("- **Order Reference**: ").append(order.getExternalReferenceCode() == null ? "N/A" : order.getExternalReferenceCode());

            return Map.of("response", result.toString());
        } catch (Exception e) {
            return Map.of(
                "error",
                """
**Error Retrieving Shipping Information**

**Order ID**: """ + orderId + "\n" +
                    "**Error**: " + e.getMessage() + "\n" +
                    "**Error Type**: " + e.getClass().getSimpleName() + "\n\n" +
                    "**Try These Alternatives:**\n" +
                    "- \"Find order " + orderId + "\" - Get complete order details\n" +
                    "- \"Get order items for order " + orderId + "\" - View order items\n" +
                    "- \"Search for orders\" - Browse available orders"
            );
        }
    }

    @Annotations.Schema(
        description = "Get order summary for all available accounts",
        name = "getAllAccountsOrderSummaryTool")
    public Map<String, ?> getAllAccountsOrderSummaryTool() {
        try {
            List<Channel> channels = _commerceService.getChannels();

            if (channels == null || channels.isEmpty()) {
                return Map.of("error", "No channels available in the system.");
            }

            String channelId = channels.get(0).getId();
            List<Account> accounts = _commerceService.getAccounts(channelId);

            if (accounts == null || accounts.isEmpty()) {
                return Map.of("error", "No accounts available for channel " + channelId + ".");
            }

            // Get order counts for all accounts
            class Summary {
                String id;
                String name;
                String type;
                int orderCount;
                Order sampleOrder;
                String error; // optional
            }

            List<Summary> accountSummaries = new ArrayList<>();
            int totalOrders = 0;

            for (Account account : accounts) {
                try {
                    String accountId = account.getId();
                    String accountName = account.getName() == null ? "Unknown Account" : account.getName();
                    String accountType = account.getType() == null ? "N/A" : account.getType();

                    // Get orders for this account
                    List<Order> orders = _commerceService.getAllPlacedOrdersByAccountDto(channelId, accountId);
                    int orderCount = (orders != null) ? orders.size() : 0;
                    totalOrders += orderCount;

                    // Get sample order info if available
                    Order sampleOrder = null;
                    if (orders != null && !orders.isEmpty()) {
                        sampleOrder = orders.get(0);
                    }

                    Summary s = new Summary();
                    s.id = accountId;
                    s.name = accountName;
                    s.type = accountType;
                    s.orderCount = orderCount;
                    s.sampleOrder = sampleOrder;
                    accountSummaries.add(s);
                } catch (Exception e) {
                    // If we can't get orders for this account, continue
                    Summary s = new Summary();
                    s.id = account.getId();
                    s.name = account.getName() == null ? "Unknown Account" : account.getName();
                    s.type = account.getType() == null ? "N/A" : account.getType();
                    s.orderCount = 0;
                    s.sampleOrder = null;
                    s.error = e.getMessage();
                    accountSummaries.add(s);
                }
            }

            // Sort by order count (highest first)
            accountSummaries.sort((a, b) -> Integer.compare(b.orderCount, a.orderCount));

            // Build the response
            StringBuilder result = new StringBuilder();
            result.append("**Order Summary for All Accounts**\n\n");
            String channelName = channels.get(0).getName() == null ? "N/A" : channels.get(0).getName();
            result.append("**Channel**: ").append(channelName).append(" (ID: ").append(channelId).append(")\n");
            result.append("**Total Accounts**: ").append(accounts.size()).append("\n");
            result.append("**Total Orders**: ").append(totalOrders).append("\n\n");

            result.append("**Account Breakdown:**\n\n");

            for (int i = 0; i < accountSummaries.size(); i++) {
                Summary summary = accountSummaries.get(i);
                result.append(i + 1).append(". **").append(summary.name).append("** (ID: ")
                      .append(summary.id).append(")\n");
                result.append("   **Type**: ").append(summary.type).append("\n");
                result.append("   **Orders**: ").append(summary.orderCount).append("\n");

                // Add sample order info if available
                if (summary.sampleOrder != null) {
                    Order sample = summary.sampleOrder;
                    String sampleDate = sample.getCreateDate() == null ? "N/A" : sample.getCreateDate();
                    String sampleTotal = sample.getTotalFormatted() == null ? "N/A" : sample.getTotalFormatted();
                    result.append("   **Sample Order**: ")
                          .append(sample.getId() == null ? "N/A" : sample.getId())
                          .append(" - ")
                          .append(sampleDate)
                          .append(" - ")
                          .append(sampleTotal)
                          .append("\n");
                }

                // Add error info if there was an issue
                if (summary.error != null && !summary.error.isEmpty()) {
                    result.append("   **⚠Error**: ").append(summary.error).append("\n");
                }

                result.append("\n");
            }

            // Add summary statistics
            int activeAccounts = 0;
            int inactiveAccounts = 0;
            for (Summary s : accountSummaries) {
                if (s.orderCount > 0) activeAccounts++; else inactiveAccounts++;
            }

            result.append("**📈 Summary Statistics:**\n");
            result.append("- **Active Accounts** (with orders): ").append(activeAccounts).append("\n");
            result.append("- **Inactive Accounts** (no orders): ").append(inactiveAccounts).append("\n");
            double avg = accounts.isEmpty() ? 0.0 : ((double) totalOrders) / ((double) accounts.size());
            result.append("- **Average Orders per Account**: ")
                  .append(String.format(java.util.Locale.ROOT, "%.1f", avg))
                  .append("\n");

            return Map.of("response", result.toString());
        } catch (Exception e) {
            return Map.of(
                "error",
                """
**Error Retrieving Account Summary**

**Error**: """ + e.getMessage() + "\n" +
                    "**Error Type**: " + e.getClass().getSimpleName() + "\n\n" +
                    "**Try These Alternatives:**\n" +
                    "- \"Show me system status\" - Basic system information\n" +
                    "- \"What channels and accounts are available?\" - List available resources\n" +
                    "- Check individual accounts one by one"
            );
        }
    }

    private static String titleCase(String s) {
        if (s == null || s.isEmpty()) return "";
        String[] parts = s.toLowerCase(java.util.Locale.ROOT).split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0)));
                if (p.length() > 1) sb.append(p.substring(1));
            }
            if (i < parts.length - 1) sb.append(' ');
        }
        return sb.toString();
    }

    private static String nullToNA(String s) {
        return (s == null || s.isEmpty()) ? "N/A" : s;
    }

    private String formatOrdersList(List<Order> orders) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        int i = 1;
        for (Order o : orders) {
            String dateStr = o.getOrderDate() != null ? o.getOrderDate().format(fmt) : "N/A";
            sb.append(i++)
                .append(") Order ")
                .append(nullToNA(o.getOrderNumber()))
                .append(" — Status: ")
                .append(nullToNA(o.getStatus()))
                .append(" — Date: ")
                .append(dateStr)
                .append(" — Total: ")
                .append(o.getTotal())
                .append("\n");
        }
        return sb.toString();
    }

    private String formatOrderSummary(Order order) {
        String orderIdVal = nullToNA(order.getId());
        String status = nullToNA(order.getStatus());
        String orderDate = order.getOrderDate() != null ? order.getOrderDate().toString() : "N/A";

        StringBuilder result = new StringBuilder();
        result.append("**Order Details for Order ").append(orderIdVal).append("**\n")
            .append("- **Reference**: ").append(order.getExternalReferenceCode()).append("\n")
            .append("- **Status**: ").append(status).append("\n")
            .append("- **Order Date**: ").append(orderDate).append("\n")
//                .append("- **Customer**: ").append(order.getAccountUserName()).append("\n")
            .append("- **Account**: ").append(order.getAccountName()).append("\n")
            .append("- **Total Amount**: ").append(order.getTotalFormatted()).append("\n")
            .append("- **Items**: ").append(order.getItemsQuantity()).append(" items\n");

        if (order.getShippingAddress() != null && !order.getShippingAddress().isEmpty()) {
            String name = nullToNA(order.getShippingAddress().get("name"));
            String city = nullToNA(order.getShippingAddress().get("city"));
            String region = nullToNA(order.getShippingAddress().get("regionISOCode"));
            String country = nullToNA(order.getShippingAddress().get("countryISOCode"));
            result.append("\n**Shipping Address:**\n")
                .append("- **Name**: ").append(name).append("\n")
                .append("- **City**: ").append(city).append(", ").append(region).append("\n")
                .append("- **Country**: ").append(country).append("\n")
                .append("\n💡 **For complete shipping details, ask**: 'Get shipping info for order ").append(orderIdVal).append("'\n");
        }

        return result.toString();
    }

    private final CommerceService _commerceService;
}
