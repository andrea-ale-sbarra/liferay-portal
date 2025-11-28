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

"""Callback functions for the Liferay Agent."""

from google.adk.agents.callback_context import CallbackContext
from google.adk.tools.tool_context import ToolContext
from typing import Optional, Any
from google.genai.types import Content

def before_agent_run(*args, **kwargs) -> Optional[Content]:
    """Callback executed before the agent runs."""
    print(f"🤖 Liferay Customer Service Agent starting...")
    print(f"Args received: {args}")
    print(f"Kwargs received: {kwargs}")
    
    # Try to find the callback context in the arguments
    callback_context = None
    for arg in args:
        if hasattr(arg, 'user_content') or hasattr(arg, 'user_message') or hasattr(arg, 'state'):
            callback_context = arg
            break
    
    if callback_context:
        if hasattr(callback_context, 'user_content'):
            print(f"User query: {callback_context.user_content}")
        elif hasattr(callback_context, 'user_message'):
            print(f"User message: {callback_context.user_message}")
        
        # Initialize state if needed
        if hasattr(callback_context, 'state') and callback_context.state:
            if "agent_start_time" not in callback_context.state:
                callback_context.state["agent_start_time"] = "now"
    
    return None

def after_tool_run(*args, **kwargs) -> None:
    """Callback executed after a tool runs."""
    print(f"Tool execution completed")
    print(f"Args received: {args}")
    print(f"Kwargs received: {kwargs}")
    
    # Try to extract tool information from arguments
    if len(args) >= 4:
        tool, tool_args, tool_context, tool_response = args[:4]
        if tool:
            tool_name = getattr(tool, 'name', str(tool))
            print(f"Tool '{tool_name}' executed successfully")
            if tool_response:
                print(f"Tool output: {str(tool_response)[:100]}...")
