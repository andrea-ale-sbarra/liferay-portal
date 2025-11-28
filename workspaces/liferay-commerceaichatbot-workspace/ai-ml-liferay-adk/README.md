# Liferay Customer Service Agent

A comprehensive AI-powered customer service solution that integrates with Liferay Commerce to provide intelligent order management and customer support through multiple deployment channels.

## 🚀 Overview

This project provides a complete customer service agent solution that can be deployed both locally for development and testing, and to Google Cloud for production use. The agent integrates with Liferay Commerce APIs to provide real-time order information, customer support, and system status monitoring.

## 🏗️ Architecture

The solution consists of three main components that work together:

### 1. **Liferay Agent** (`LiferayAgent/`)
The core AI agent built with Google's Agent Development Kit (ADK) that handles customer inquiries and integrates with Liferay Commerce.

### 2. **Cloud Function** (`cloud_function/`)
A serverless webhook that acts as a bridge between Dialogflow CX and the deployed Agent Engine, maintaining conversational context.

### 3. **Deployment System** (`deployment/`)
Scripts and tools for deploying the agent to Google Cloud Vertex AI Agent Engine.

## 📁 Project Structure

```
ai-ml-liferay-adk/
├── LiferayAgent/                    # Core AI Agent
│   ├── agent.py                    # Main agent definition
│   ├── config.py                   # Configuration management
│   ├── liferay_client.py           # Liferay Commerce API client
│   ├── prompt.py                   # Agent instructions and prompts
│   ├── sub_agents/                 # Specialized sub-agents
│   │   └── customer_support/
│   └── tools/                      # Agent tools and utilities
│       ├── agent_tools.py          # Core agent tools
│       ├── callbacks.py            # Agent callbacks
│       └── customer_service_tools.py # Customer service tools
├── cloud_function/                 # Dialogflow Webhook
│   ├── main.py                    # Cloud Function entry point
│   ├── requirements.txt           # Function dependencies
│   └── SETUP.md                   # Cloud Function setup guide
├── deployment/                     # Deployment Scripts
│   ├── deploy.py                  # Agent deployment script
│   └── test_deploy.py             # Deployment testing
├── DEPLOYMENT.md                  # Comprehensive deployment guide
├── LOCAL_SETUP_GUIDE.md           # Local development setup guide
├── TOOL_TESTING_GUIDE.md          # Tool testing and validation guide
├── performance.md                 # Performance metrics and analysis
├── test_performance.py            # Performance testing script
├── test_conversation.py           # Conversation testing script
├── env_template                   # Environment variables template
└── pyproject.toml                 # Poetry workspace configuration
```

## 🛠️ Technology Stack

- **Google ADK**: Agent Development Kit for building intelligent agents
- **Python 3.10+**: Core development language
- **Poetry**: Unified dependency management and virtual environment
- **Google Cloud**: Vertex AI, Cloud Functions, Agent Engine
- **Liferay Commerce**: E-commerce platform integration
- **Dialogflow CX**: Conversational AI platform
- **Pydantic**: Data validation and settings management
- **Gemini 2.0 Flash**: Advanced AI model for natural language processing

## 🚦 Getting Started

### Prerequisites

1. **Python 3.10+** (updated requirement)
2. **Google Cloud Project** with Vertex AI API enabled
3. **Poetry** for dependency management
4. **Liferay Commerce instance** (for testing)

### Quick Setup

For detailed setup instructions, see `LOCAL_SETUP_GUIDE.md`.

1. **Clone the repository:**
   ```bash
   git clone git@github.com:66degrees/ai-ml-liferay-adk.git
   cd ai-ml-liferay-adk
   ```

2. **Install dependencies:**
   ```bash
   poetry install
   ```

3. **Configure environment:**
   Copy the environment template and configure:
   ```bash
   cp env_template .env
   # Edit .env with your credentials
   ```

4. **Google Cloud Setup:**
   ```bash
   gcloud auth application-default login
   gcloud config set project your-project-id
   ```

### Documentation

- **`LOCAL_SETUP_GUIDE.md`**: Comprehensive local development setup
- **`TOOL_TESTING_GUIDE.md`**: Tool testing and validation procedures
- **`performance.md`**: Performance metrics and analysis
- **`DEPLOYMENT.md`**: Production deployment guide

## 🎯 Usage

### Local Development

Run the agent locally for development and testing:

```bash
# Start the ADK web interface
poetry run adk web

# Or run the agent directly
poetry run python LiferayAgent/agent.py
```

### Production Deployment

#### 1. Deploy Agent to Vertex AI

```bash
# Deploy the agent to Agent Engine
poetry run python deployment/deploy.py --create
```

This will return a `REASONING_ENGINE_ID` that you'll need for the webhook.

#### 2. Deploy Cloud Function

```bash
# Deploy the Dialogflow webhook
gcloud functions deploy liferay-dialogflow-webhook \
  --region=us-central1 \
  --runtime=python311 \
  --source=./cloud_function \
  --entry-point=liferay_dialogflow_webhook \
  --trigger-http \
  --allow-unauthenticated \
  --memory=512MB \
  --set-env-vars \
  GOOGLE_CLOUD_PROJECT="ai-ml-team-sandbox",\
  GOOGLE_CLOUD_LOCATION="us-central1",\
  REASONING_ENGINE_ID="your-reasoning-engine-id"
```

#### 3. Configure Dialogflow CX

1. Create a Dialogflow CX agent
2. Set up a webhook fulfillment pointing to your Cloud Function URL
3. Configure intents to route to the webhook

## 🔧 Key Features

### Liferay Agent Capabilities

- **Advanced Order Management**: Find orders by ID, email, customer name, date range, product, status, or shipping address
- **Comprehensive Order Details**: Retrieve detailed order information including items, pricing, status, and shipping
- **Intelligent Customer Support**: Handle customer inquiries with FAQ integration and context-aware responses
- **System Monitoring**: Real-time Liferay Commerce system health monitoring and debugging
- **Enhanced Search**: Multiple search capabilities with both standard and enhanced (faster) versions
- **Multi-tool Integration**: Seamlessly combines 19 specialized tools for complex customer service tasks
- **Performance Optimized**: Tools tested and optimized for production use with comprehensive performance metrics

### Cloud Function Features

- **Session Management**: Maintains conversational context across webhook calls
- **Dialogflow Integration**: Properly formatted responses for Dialogflow CX
- **Error Handling**: Robust error handling and logging
- **Scalability**: Serverless architecture that scales automatically

### Deployment Features

- **One-command Deployment**: Simple deployment scripts
- **Environment Management**: Proper configuration and environment variable handling
- **Testing Tools**: Built-in testing and validation
- **Monitoring**: Comprehensive logging and debugging

## 🛠️ Available Tools

The agent includes a comprehensive set of **19 tools** for customer service:

### Core Order Management
- **`find_order`**: Find specific orders by ID
- **`get_customer_orders`**: Get all orders for a specific customer
- **`get_order_items`**: Retrieve detailed item information for orders
- **`get_order_shipping`**: Get shipping information for orders
- **`get_all_accounts_order_summary`**: Get summary of all accounts

### Advanced Search Capabilities
- **`search_orders`**: General order search with flexible criteria
- **`search_orders_by_date_range`**: Search orders within specific date ranges
- **`search_orders_by_product`**: Find orders containing specific products
- **`search_orders_by_status`**: Search orders by status (completed, pending, etc.)
- **`search_orders_by_status_enhanced`**: Enhanced status search with better performance
- **`search_orders_by_shipping_address`**: Search orders by shipping address
- **`search_orders_by_shipping_address_enhanced`**: Enhanced address search (3x faster)

### System & Debugging Tools
- **`get_system_status`**: Monitor Liferay Commerce system health
- **`list_available_channels_and_accounts`**: List available channels and accounts
- **`debug_liferay_connection`**: Debug Liferay API connectivity
- **`debug_customer_and_orders`**: Debug customer and order data
- **`test_external_api_access`**: Test external API access

### Utility & Support Tools
- **`get_available_test_emails`**: Get available test email addresses
- **`get_faq_information`**: Retrieve FAQ information for customer queries

## 🔄 How It All Works Together

### 1. **User Interaction Flow**
```
User → Dialogflow CX → Cloud Function → Agent Engine → Liferay Commerce
     ←                ←               ←              ←
```

### 2. **Session Management**
- Dialogflow maintains conversation sessions
- Cloud Function extracts session ID and passes it to Agent Engine
- Agent Engine maintains context using the session ID
- Responses flow back through the same path

### 3. **Data Flow**
1. User asks a question in Dialogflow
2. Dialogflow routes to Cloud Function webhook
3. Cloud Function calls Agent Engine with session context
4. Agent Engine processes the query using available tools
5. Tools interact with Liferay Commerce APIs
6. Response flows back through the chain to the user

## 📊 Performance & Monitoring

### Performance Metrics
The agent has been extensively tested with comprehensive performance metrics:

- **Total Tools**: 19 customer service tools
- **Success Rate**: 100% (19/19 tests passed)
- **Fastest Tools**: FAQ Information & Search Orders (0.1ms)
- **Average Response Time**: 45.91s
- **Enhanced Tools**: 3x faster shipping address search

See `performance.md` for detailed performance analysis and `test_performance.py` for running performance tests.

### Performance Testing
```bash
# Run comprehensive performance tests
poetry run python test_performance.py

# Run conversation tests
poetry run python test_conversation.py
```

### Monitoring and Debugging

#### Agent Engine Logs (Direct Access)

**View Agent Engine Logs in Real-Time:**
```bash
# Stream logs from the deployed Agent Engine
gcloud logging tail "resource.type=aiplatform.googleapis.com/ReasoningEngine" \
  --project=your-project-id \
  --format="value(timestamp,severity,textPayload)"

# Filter for specific log levels
gcloud logging tail "resource.type=aiplatform.googleapis.com/ReasoningEngine AND severity>=ERROR" \
  --project=your-project-id

# View logs for a specific time range
gcloud logging read "resource.type=aiplatform.googleapis.com/ReasoningEngine" \
  --project=your-project-id \
  --limit=100 \
  --format="table(timestamp,severity,textPayload)"
```

**Advanced Agent Engine Log Filtering:**
```bash
# Filter by specific error types
gcloud logging read "resource.type=aiplatform.googleapis.com/ReasoningEngine AND textPayload:\"ERROR\"" \
  --project=your-project-id \
  --limit=50

# Filter by tool execution logs
gcloud logging read "resource.type=aiplatform.googleapis.com/ReasoningEngine AND textPayload:\"tool\"" \
  --project=your-project-id \
  --limit=50

# Export logs to file for analysis
gcloud logging read "resource.type=aiplatform.googleapis.com/ReasoningEngine" \
  --project=your-project-id \
  --limit=1000 \
  --format=json > agent_engine_logs.json
```

**Monitor Agent Engine Performance:**
```bash
# View reasoning engine metrics
gcloud ai reasoning-engines describe your-reasoning-engine-id \
  --region=us-central1 \
  --project=your-project-id

# List all reasoning engines
gcloud ai reasoning-engines list \
  --region=us-central1 \
  --project=your-project-id
```

#### Cloud Function Logs
```bash
# View Cloud Function logs
gcloud functions logs read liferay-dialogflow-webhook --region=us-central1

# Stream Cloud Function logs in real-time
gcloud functions logs tail liferay-dialogflow-webhook --region=us-central1

# Filter Cloud Function logs by severity
gcloud functions logs read liferay-dialogflow-webhook --region=us-central1 \
  --filter="severity>=ERROR"
```

#### Comprehensive Log Analysis
```bash
# View all logs across the entire system
gcloud logging read "resource.type=aiplatform.googleapis.com/ReasoningEngine OR resource.type=cloud_function" \
  --project=your-project-id \
  --limit=200 \
  --format="table(timestamp,resource.type,severity,textPayload)"

# Search for specific patterns across all logs
gcloud logging read "textPayload:\"liferay\" OR textPayload:\"order\"" \
  --project=your-project-id \
  --limit=100
```

#### Testing and Validation

**Agent Engine Testing:**
```bash
poetry run python deployment/test_deploy.py
```

**Local Testing:**
```bash
poetry run python LiferayAgent/agent.py
```

**Tool Testing:**
```bash
# Follow the comprehensive tool testing guide
# See TOOL_TESTING_GUIDE.md for detailed instructions
```

**Performance Testing:**
```bash
# Run comprehensive performance tests
poetry run python test_performance.py

# Run conversation tests
poetry run python test_conversation.py
```

#### Debugging Tools

**Enable Debug Logging:**
```bash
# Set environment variable for verbose logging
export GOOGLE_CLOUD_LOG_LEVEL=DEBUG

# Or add to your .env file
echo "GOOGLE_CLOUD_LOG_LEVEL=DEBUG" >> .env
```

**Common Debugging Commands:**
```bash
# Check Agent Engine status
gcloud ai reasoning-engines describe your-reasoning-engine-id \
  --region=us-central1 \
  --project=your-project-id

# Verify Cloud Function is running
gcloud functions describe liferay-dialogflow-webhook \
  --region=us-central1

# Check recent deployments
gcloud functions list --filter="name:liferay-dialogflow-webhook"
```

## 🚀 Deployment Checklist

- [ ] Google Cloud project configured
- [ ] Vertex AI API enabled
- [ ] Liferay Commerce credentials configured
- [ ] Agent deployed to Agent Engine
- [ ] Cloud Function deployed
- [ ] Dialogflow CX configured
- [ ] Webhook URL set in Dialogflow
- [ ] Environment variables configured
- [ ] Testing completed

## 🆕 Recent Updates

### Latest Improvements (v2.0)

- **Expanded Tool Suite**: Added 6 new tools including enhanced search capabilities and FAQ integration
- **Performance Optimization**: Implemented enhanced versions of search tools with 3x performance improvements
- **Comprehensive Testing**: Added performance testing suite with 19 tools and 100% success rate
- **Enhanced Documentation**: Added detailed setup guides, tool testing procedures, and performance analysis
- **Debug Tools**: Added comprehensive debugging tools for system monitoring and troubleshooting
- **FAQ Integration**: Added intelligent FAQ system for instant customer support responses

### Performance Highlights

- **19 Customer Service Tools** with comprehensive coverage
- **100% Test Success Rate** across all tools
- **Sub-millisecond Response** for FAQ and general search
- **Enhanced Search Performance** with 3x speed improvements
- **Comprehensive Monitoring** with detailed performance metrics

## 🤝 Contributing

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Make your changes** with clear commit messages
4. **Test thoroughly** using the provided testing tools
5. **Run performance tests** to ensure no regressions
6. **Push to your branch**: `git push origin feature/amazing-feature`
7. **Create a Pull Request** with detailed description

## 📄 License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## 🙏 Acknowledgments

- Google ADK team for the development framework
- Google Cloud for the infrastructure and AI services
- Liferay for the commerce platform integration
- Open source community for various dependencies and tools

---

**Happy Customer Service Automation! 🎉**

For questions or support, please refer to the project-specific documentation or create an issue in the repository.