# Liferay Customer Service Agent - Local Setup Guide

This guide provides step-by-step instructions for setting up and running the Liferay Customer Service Agent locally using Poetry.

## 📋 Prerequisites

- **Python 3.10+** installed on your system
- **Poetry** package manager installed
- **Git** (optional, for version control)
- Access to **Liferay UAT environment** (webserver-lct66degrees-uat.lfr.cloud)

## 🚀 Quick Setup (5 minutes)

### 1. Extract the Project
```bash
# Extract the zipped project to your desired location
unzip liferay-agent-project.zip
cd ai-ml-liferay-adk
```

### 2. Install Dependencies with Poetry
```bash
# Install all dependencies including development tools
poetry install
```

### 3. Set Up Environment Variables
```bash
# Copy the environment template
cp .env.example .env

# Edit the .env file with your credentials
nano .env
```

**Required Environment Variables:**
```env
# Liferay API Configuration
LIFERAY_BASE_URL=https://webserver-lct66degrees-uat.lfr.cloud/
LIFERAY_USERNAME=test@liferay.com
LIFERAY_PASSWORD=your_password_here
LIFERAY_API_VERSION=v1.0
LIFERAY_TIMEOUT=30
LIFERAY_SSL_VERIFY=False

# Google Cloud Configuration (for deployment)
GOOGLE_CLOUD_PROJECT=your-project-id
GOOGLE_CLOUD_LOCATION=us-central1
REASONING_ENGINE_ID=your-reasoning-engine-id

# Model Configuration
GEN_FAST_MODEL=gemini-2.0-flash
GEN_ADVANCED_MODEL=gemini-2.0-flash
GOOGLE_GENAI_USE_VERTEXAI=True
```

### 4. Test the Installation
```bash
# Test that all dependencies are installed correctly
poetry run python -c "import google.adk; print('✅ ADK installed successfully')"

# Test Liferay API connectivity
poetry run python -c "from LiferayAgent.liferay_client import LiferayAPIClient; client = LiferayAPIClient(); print('✅ Liferay API client initialized')"
```

---

## 🎯 Running the Agent Locally

### Option 1: ADK Web Interface (Recommended)

```bash
# Start the ADK web interface
poetry run adk web

# Open your browser to: http://localhost:8080
```

**What you'll see:**
- Interactive web interface
- Chat interface to test the agent
- Real-time tool execution
- Detailed logs and responses

---

## 🧪 Testing the Agent

### Quick Test (2 minutes)
Use these sample questions in the ADK web interface:

1. **FAQ Tool**: "What is your return policy?"
2. **Find Order**: "Find order 80654"
3. **Customer Orders**: "Get orders for john.smith@betavehicle.com"
4. **System Status**: "Check system status"

### Comprehensive Test (10 minutes)
Follow the detailed testing guide in `TOOL_TESTING_GUIDE.md` for complete tool testing.

---

## 📊 Available Tools

The agent includes **13 tools** organized by performance:

### ⚡ Fast Tools (< 5 seconds)
- **FAQ Information**: Instant answers to common questions
- **Test Emails**: List available test email addresses
- **Find Order**: Direct order lookup by ID
- **Order Shipping**: Shipping information for specific orders
- **List Channels/Accounts**: System information
- **Order Items**: Detailed items for specific orders

### ⚡ Medium Tools (5-30 seconds)
- **Customer Orders**: All orders for a customer
- **Date Range Search**: Orders within date range
- **System Status**: Connectivity and health check
- **All Accounts Summary**: Comprehensive account information

### ⚡ Slow Tools (30+ seconds)
- **Search Orders**: General search with intelligent routing
- **Product Search**: Orders containing specific products
- **Status Search**: Orders with specific status
- **Address Search**: Orders shipped to specific locations

---

## 🔧 Troubleshooting

### Common Issues

#### 1. Poetry Installation Issues
```bash
# If Poetry is not installed
curl -sSL https://install.python-poetry.org | python3 -

# Add Poetry to PATH (add to ~/.bashrc or ~/.zshrc)
export PATH="$HOME/.local/bin:$PATH"

# Restart terminal and try again
poetry install
```

#### 2. Python Version Issues
```bash
# Check Python version
python3 --version

# If Python 3.10+ is not available, install it
# On macOS with Homebrew:
brew install python@3.11

# On Ubuntu/Debian:
sudo apt update
sudo apt install python3.11 python3.11-venv
```

#### 3. Liferay API Connection Issues
```bash
# Test API connectivity
poetry run python -c "
from LiferayAgent.liferay_client import LiferayAPIClient
client = LiferayAPIClient()
print('Testing connection...')
result = client.get_system_status()
print('Connection successful!' if result else 'Connection failed!')
"
```

#### 4. Missing Dependencies
```bash
# Reinstall all dependencies
poetry install --sync

# If specific packages are missing
poetry add google-adk google-cloud-aiplatform requests python-dotenv
```

#### 5. Environment Variable Issues
```bash
# Check if .env file exists and has correct values
cat .env

# Verify environment variables are loaded
poetry run python -c "
import os
from dotenv import load_dotenv
load_dotenv()
print('LIFERAY_BASE_URL:', os.getenv('LIFERAY_BASE_URL'))
print('LIFERAY_USERNAME:', os.getenv('LIFERAY_USERNAME'))
"
```

---

## 🚀 Next Steps

### 1. Test All Tools
- Follow the `TOOL_TESTING_GUIDE.md` for comprehensive testing
- Verify all 13 tools work correctly
- Test performance expectations

### Common Commands Reference
```bash
# Install dependencies
poetry install

# Activate virtual environment
poetry shell

# Run ADK web interface
poetry run adk web

# Test specific tool
poetry run python -c "from LiferayAgent.tools.customer_service_tools import *; print(find_order('80654'))"

# Check Poetry environment
poetry env info

# Update dependencies
poetry update
```

---

## ✅ Verification Checklist

- [ ] Python 3.10+ installed
- [ ] Poetry installed and working
- [ ] Project extracted successfully
- [ ] Dependencies installed (`poetry install`)
- [ ] Environment variables configured (`.env` file)
- [ ] Liferay API connectivity working
- [ ] ADK web interface starts (`poetry run adk web`)
- [ ] Basic tools tested (FAQ, Find Order, Customer Orders)
- [ ] All 13 tools functioning correctly

---