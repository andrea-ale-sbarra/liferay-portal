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

"""Liferay Agent - Main Agent Definition."""

import os
from google.adk.agents import LlmAgent
from google.adk.tools import get_user_choice

# Import agent components from their new, organized locations
from .tools.callbacks import before_agent_run, after_tool_run
from .tools.agent_tools import agent_tools
from .prompt import ROOT_AGENT_INSTRUCTION

# Load environment variables
try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass

root_agent = LlmAgent(
    name="InteractiveLiferayCustomerService",
    model=os.getenv("GEN_ADVANCED_MODEL", "gemini-2.0-flash"),
    instruction=ROOT_AGENT_INSTRUCTION,
    tools=[
        get_user_choice,
        *agent_tools,
    ],
    before_agent_callback=[before_agent_run],
    after_tool_callback=[after_tool_run],
)

def main():
    """Main function to run the Liferay Customer Service Agent"""
    print("🚀 Starting Liferay Customer Service Agent...")
    print("=" * 50)
    
    try:
        # Test the agent configuration
        print(f"✅ Agent Name: {root_agent.name}")
        print(f"✅ Model: {root_agent.model}")
        print(f"✅ Tools Available: {len(root_agent.tools)}")
        print(f"✅ Callbacks: {len(root_agent.before_agent_callback)} before, {len(root_agent.after_tool_callback)} after")
        
        print("\n🎉 Agent is ready!")
        print("\nTo use this agent:")
        print("1. Run: poetry run adk web agents --port 8000")
        print("2. Open http://localhost:8000 in your browser")
        print("3. Start chatting with your customer service agent!")
        
    except Exception as e:
        print(f"❌ Error initializing agent: {e}")
        print("\nTroubleshooting:")
        print("1. Make sure all dependencies are installed: poetry install")
        print("2. Check your credentials: poetry run setup-credentials")
        print("3. Verify your .env file has the correct API keys")

if __name__ == "__main__":
    main()
