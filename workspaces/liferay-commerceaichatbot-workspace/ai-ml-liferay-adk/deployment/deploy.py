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

"""Deployment script for the Liferay Customer Service Agent."""

import os
import sys
import vertexai
from absl import app, flags
from dotenv import load_dotenv
from vertexai import agent_engines
from vertexai.preview.reasoning_engines import AdkApp

# Add the project root to the Python path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
from LiferayAgent.agent import root_agent

FLAGS = flags.FLAGS
flags.DEFINE_string("project_id", None, "GCP project ID.")
flags.DEFINE_string("location", None, "GCP location.")
flags.DEFINE_string("bucket", None, "GCP bucket.")
flags.DEFINE_string("resource_id", None, "ReasoningEngine resource ID.")

flags.DEFINE_bool("list", False, "List all agents.")
flags.DEFINE_bool("create", False, "Creates a new agent.")
flags.DEFINE_bool("delete", False, "Deletes an existing agent.")
flags.mark_bool_flags_as_mutual_exclusive(["create", "delete"])

def create() -> None:
    """Creates an agent engine for the Liferay Customer Service Agent."""
    # Load environment variables from .env file first
    load_dotenv()
    
    # Debug: Print the environment variables being read
    print("🔍 Environment variables being read:")
    print(f"  LIFERAY_BASE_URL: {os.getenv('LIFERAY_BASE_URL', 'NOT_SET')}")
    print(f"  LIFERAY_USERNAME: {os.getenv('LIFERAY_USERNAME', 'NOT_SET')}")
    print(f"  LIFERAY_PASSWORD: {'*' * len(os.getenv('LIFERAY_PASSWORD', '')) if os.getenv('LIFERAY_PASSWORD') else 'NOT_SET'}")
    print(f"  LIFERAY_SSL_VERIFY: {os.getenv('LIFERAY_SSL_VERIFY', 'NOT_SET')}")
    
    # Define the environment variables to be set in the deployed agent's environment.
    # These are read from the local environment (e.g., your .env file).
    env_vars = {
        "GEN_FAST_MODEL": os.getenv("GEN_FAST_MODEL", "gemini-2.0-flash"),
        "GEN_ADVANCED_MODEL": os.getenv("GEN_ADVANCED_MODEL", "gemini-2.0-flash"),
        "GOOGLE_GENAI_USE_VERTEXAI": os.getenv("GOOGLE_GENAI_USE_VERTEXAI", "True"),
        "LIFERAY_BASE_URL": os.getenv("LIFERAY_BASE_URL", "http://localhost:8080"),
        "LIFERAY_USERNAME": os.getenv("LIFERAY_USERNAME", "test@liferay.com"),
        "LIFERAY_PASSWORD": os.getenv("LIFERAY_PASSWORD", "test"),
        "LIFERAY_API_VERSION": os.getenv("LIFERAY_API_VERSION", "v1.0"),
        "LIFERAY_TIMEOUT": os.getenv("LIFERAY_TIMEOUT", "30"),
        "LIFERAY_SSL_VERIFY": os.getenv("LIFERAY_SSL_VERIFY", "False"),  # Disable SSL verification for UAT
    }
    
    adk_app = AdkApp(
        agent=root_agent,
        env_vars=env_vars,
        enable_tracing=True,
    )

    remote_agent = agent_engines.create(
        adk_app,
        display_name=root_agent.name,
        requirements=[
            "google-adk>=1.10.0",
            "google-cloud-aiplatform[agent_engines]>=1.91.0,!=1.92.0",
            "google-genai>=0.5.0",
            "python-dotenv>=1.0.0",
            "pydantic>=2.10.6,<3.0.0",
            "cloudpickle==3.1.1",
            "requests>=2.31.0",
            "typing-extensions>=4.5.0",
            "absl-py>=2.2.1",
            "python-dateutil>=2.8.2",
            "urllib3>=2.0.0",
            "certifi>=2023.7.22",
            "deprecated>=1.2.0",
            "google-auth>=2.40.0",
            "google-api-core>=2.25.0",
            "google-api-python-client>=2.180.0",
        ],
        extra_packages=["./LiferayAgent"],
    )
    print(f"Created remote agent: {remote_agent.resource_name}")
    print(f"With environment variables: {env_vars}")
    print(f"Resource ID: {remote_agent.resource_name.split('/')[-1]}")
    print("\nNext steps:")
    print("1. Add the REASONING_ENGINE_ID to your .env file")
    print("2. Run: python deployment/test_deploy.py to test the deployment")
    print("3. Your agent is now deployed and ready to use!")

def delete(resource_id: str) -> None:
    remote_agent = agent_engines.get(resource_id)
    remote_agent.delete(force=True)
    print(f"Deleted remote agent: {resource_id}")

def list_agents() -> None:
    remote_agents = agent_engines.list()
    template = """
{agent.name} ("{agent.display_name}")
- Create time: {agent.create_time}
- Update time: {agent.update_time}
"""
    remote_agents_string = "\n".join(
        template.format(agent=agent) for agent in remote_agents
    )
    print(f"All remote agents:\n{remote_agents_string}")

def main(argv=None) -> None:
    if argv is None:
        argv = sys.argv
    
    # Parse flags first
    FLAGS(argv)
    load_dotenv()

    project_id = (
        FLAGS.project_id
        if FLAGS.project_id
        else os.getenv("GOOGLE_CLOUD_PROJECT")
    )
    location = (
        FLAGS.location if FLAGS.location else os.getenv("GOOGLE_CLOUD_LOCATION")
    )
    bucket = (
        FLAGS.bucket
        if FLAGS.bucket
        else os.getenv("GOOGLE_CLOUD_STORAGE_BUCKET")
    )

    print(f"PROJECT: {project_id}")
    print(f"LOCATION: {location}")
    print(f"BUCKET: {bucket}")

    if not project_id:
        print("Missing required environment variable: GOOGLE_CLOUD_PROJECT")
        return
    elif not location:
        print("Missing required environment variable: GOOGLE_CLOUD_LOCATION")
        return
    elif not bucket:
        print(
            "Missing required environment variable: GOOGLE_CLOUD_STORAGE_BUCKET"
        )
        return

    vertexai.init(
        project=project_id,
        location=location,
        staging_bucket=f"gs://{bucket}",
    )

    if FLAGS.list:
        list_agents()
    elif FLAGS.create:
        create()
    elif FLAGS.delete:
        if not FLAGS.resource_id:
            print("resource_id is required for delete")
            return
        delete(FLAGS.resource_id)
    else:
        print("Usage:")
        print("  List agents: python deployment/deploy.py --list")
        print("  Create agent: python deployment/deploy.py --create")
        print("  Delete agent: python deployment/deploy.py --delete --resource_id=<id>")

if __name__ == "__main__":
    app.run(main)
