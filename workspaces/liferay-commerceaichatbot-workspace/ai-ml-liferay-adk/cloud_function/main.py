import json
import os
import functions_framework
from vertexai import agent_engines

# Get configuration from environment variables
GOOGLE_CLOUD_PROJECT = os.getenv('GOOGLE_CLOUD_PROJECT')
GOOGLE_CLOUD_LOCATION = os.getenv('GOOGLE_CLOUD_LOCATION')
REASONING_ENGINE_ID = os.getenv('REASONING_ENGINE_ID')

# Construct the Agent Engine resource name from environment variables
if not all([GOOGLE_CLOUD_PROJECT, GOOGLE_CLOUD_LOCATION]):
    raise ValueError("Missing required environment variables: GOOGLE_CLOUD_PROJECT, GOOGLE_CLOUD_LOCATION")

# Only initialize Agent Engine if REASONING_ENGINE_ID is provided
remote_agent = None
if REASONING_ENGINE_ID:
    AGENT_ENGINE_RESOURCE_NAME = f"projects/{GOOGLE_CLOUD_PROJECT}/locations/{GOOGLE_CLOUD_LOCATION}/reasoningEngines/{REASONING_ENGINE_ID}"
    
    # Initialize the Agent Engine instance
    try:
        remote_agent = agent_engines.get(AGENT_ENGINE_RESOURCE_NAME)
        print(f"Successfully initialized Agent Engine: {AGENT_ENGINE_RESOURCE_NAME}")
    except Exception as e:
        print(f"Error initializing Agent Engine: {e}")
        remote_agent = None
else:
    print("REASONING_ENGINE_ID not provided, Agent Engine will not be initialized")

# Note: Cloud Functions are stateless, so we can't maintain in-memory caches
# Each webhook invocation will create a new session, but the Agent Engine
# should maintain context through the user_id parameter

@functions_framework.http
def liferay_dialogflow_webhook(request):
    """HTTP Cloud Function that acts as a webhook for Dialogflow."""

    # Get the request body from Dialogflow
    request_json = request.get_json(silent=True)
    if not remote_agent:
        return json.dumps({
            "fulfillmentResponse": {
                "messages": [
                    {
                        "text": {
                            "text": ["Error: Agent Engine is not initialized."]
                        }
                    }
                ]
            }
        })

    try:
        # Debug: Print the entire request to understand the structure
        print(f"Full request JSON: {json.dumps(request_json, indent=2)}")
        
        # Extract the user's query from the request
        # Handle multiple Dialogflow formats
        user_query = None
        
        # Try Dialogflow CX format first (text field in messages)
        if 'text' in request_json:
            user_query = request_json.get('text')
        
        # Try Dialogflow ES format (queryResult.queryText)
        elif 'queryResult' in request_json:
            user_query = request_json.get('queryResult', {}).get('queryText')
        
        # Try Dialogflow CX format (queryText in pageInfo)
        elif 'pageInfo' in request_json and 'formInfo' in request_json.get('pageInfo', {}):
            form_info = request_json.get('pageInfo', {}).get('formInfo', {})
            if 'parameterInfo' in form_info:
                for param in form_info.get('parameterInfo', []):
                    if param.get('displayName') == 'queryText':
                        user_query = param.get('value')
                        break
        
        # Try simple message format for testing
        elif 'message' in request_json:
            user_query = request_json.get('message')
        
        # Try 'query' field as another common format
        elif 'query' in request_json:
            user_query = request_json.get('query')

        if not user_query:
            # Return debug info to help understand the request structure
            debug_info = f"Available fields in request: {list(request_json.keys()) if request_json else 'No JSON data'}"
            return json.dumps({
                "fulfillmentResponse": {
                    "messages": [
                        {
                            "text": {
                                "text": [f"No query text found in the request. {debug_info}"]
                            }
                        }
                    ]
                }
            })

        # Extract Dialogflow session ID to use as Agent Engine's user_id
        # This ensures we maintain the same session across the entire conversation
        session_id = None
        
        # Try multiple ways to extract the session ID
        if 'session' in request_json:
            session_path = request_json.get('session', '')
            if session_path:
                session_id = session_path.split('/')[-1]
                print(f"Extracted session_id from 'session' field: {session_id}")
        
        # Also try sessionInfo.session if available
        if not session_id and 'sessionInfo' in request_json:
            session_info = request_json.get('sessionInfo', {})
            if 'session' in session_info:
                session_path = session_info.get('session', '')
                if session_path:
                    session_id = session_path.split('/')[-1]
                    print(f"Extracted session_id from 'sessionInfo.session' field: {session_id}")
        
        # Use a fallback user_id if no session ID is available
        if not session_id:
            session_id = "dialogflow_user"
            print(f"Using fallback session_id: {session_id}")
        
        print(f"Final session_id being used: {session_id}")
        
        # Try to get an existing session first, then create a new one if none exists
        session = None
        try:
            # First, try to list existing sessions for this user
            print(f"Calling list_sessions for user_id: {session_id}")
            list_sessions_response = remote_agent.list_sessions(user_id=session_id)
            print(f"list_sessions returned: {list_sessions_response}")
            
            # Extract the sessions array from the response
            existing_sessions = list_sessions_response.get('sessions', []) if list_sessions_response else []
            print(f"Number of existing sessions: {len(existing_sessions)}")
            
            if existing_sessions and len(existing_sessions) > 0:
                # Sort sessions by lastUpdateTime (most recent first) and use the latest one
                sorted_sessions = sorted(existing_sessions, key=lambda x: x.get('lastUpdateTime', 0), reverse=True)
                session = sorted_sessions[0]
                print(f"Retrieved existing session for user_id: {session_id} (agent_session_id: {session['id']})")
                print(f"Session details: {session}")
            else:
                # No existing sessions, create a new one
                print(f"No existing sessions found, creating new session for user_id: {session_id}")
                session = remote_agent.create_session(user_id=session_id)
                print(f"Created new session for user_id: {session_id} (agent_session_id: {session['id']})")
        except Exception as e:
            print(f"Error managing sessions for user_id {session_id}: {e}")
            import traceback
            print(f"Full traceback: {traceback.format_exc()}")
            # Fallback: create a new session
            session = remote_agent.create_session(user_id=session_id)
            print(f"Created fallback session for user_id: {session_id} (agent_session_id: {session['id']})")

        # Send the query to the Agent Engine and get the final output
        # Use stream_query and collect the final response
        final_response = ""
        print(f"Sending query to Agent Engine: '{user_query}'")
        print(f"Using session_id: {session_id}, agent_session_id: {session['id']}")
        
        event_count = 0
        function_call_detected = False
        for event in remote_agent.stream_query(
            user_id=session_id,
            session_id=session["id"],
            message=user_query
        ):
            event_count += 1
            print(f"Event #{event_count}: {list(event.keys())}")
            print(f"Event content: {event}")
            
            # Look for function calls
            if "content" in event and "parts" in event["content"]:
                parts = event["content"]["parts"]
                for part in parts:
                    if "function_call" in part:
                        function_call_detected = True
                        print(f"Function call detected: {part['function_call']}")
                    elif "text" in part:
                        final_response = part["text"]
                        print(f"Found text response: {final_response}")
                        break
            
            # Also check for errors
            if "error" in event:
                print(f"Agent Engine error: {event['error']}")
        
        # If we detected a function call but no text response, wait a bit more
        if function_call_detected and not final_response:
            print("Function call detected but no response yet. This indicates the function call is not being executed.")
            print("This could be due to:")
            print("1. Function execution timeout")
            print("2. External API call failure")
            print("3. Agent engine configuration issue")
            print("4. Network connectivity issues")
            
            # Try to get more events by waiting a bit longer
            import time
            time.sleep(2)
            
            # Try to get more events
            print("Attempting to get more events...")
            for event in remote_agent.stream_query(
                user_id=session_id,
                session_id=session["id"],
                message=""
            ):
                print(f"Additional event: {event}")
                if "content" in event and "parts" in event["content"]:
                    parts = event["content"]["parts"]
                    for part in parts:
                        if "text" in part:
                            final_response = part["text"]
                            print(f"Found delayed text response: {final_response}")
                            break
                if final_response:
                    break
        
        print(f"Final response: '{final_response}'")

        # Format the response for Dialogflow CX
        # Standard webhook response format - no expectUserResponse field needed
        dialogflow_response = {
            "fulfillmentResponse": {
                "messages": [
                    {
                        "text": {
                            "text": [final_response]
                        }
                    }
                ]
            }
        }
        
        print(f"Sending response to Dialogflow: {json.dumps(dialogflow_response, indent=2)}")
        return json.dumps(dialogflow_response)

    except Exception as e:
        print(f"Error processing webhook: {e}")
        return json.dumps({
            "fulfillmentResponse": {
                "messages": [
                    {
                        "text": {
                            "text": ["An error occurred while processing your request."]
                        }
                    }
                ]
            }
        })