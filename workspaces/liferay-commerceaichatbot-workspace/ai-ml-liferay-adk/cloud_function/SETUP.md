# Cloud Function Setup

## Environment Configuration

This cloud function requires environment variables to be configured. **Important**: Google Cloud Functions does not use `.env` files. Environment variables must be set during deployment using the `--set-env-vars` flag.

## Required Environment Variables

- `GOOGLE_CLOUD_PROJECT`: Your Google Cloud project ID
- `GOOGLE_CLOUD_LOCATION`: The location of your reasoning engine (e.g., us-central1)
- `REASONING_ENGINE_ID`: The ID of your deployed reasoning engine

## Deployment Command

Deploy the cloud function with environment variables using the following command:

```bash
gcloud functions deploy liferay-dialogflow-webhook \
--region=YOUR_REGION \
--runtime=python311 \
--source=. \
--entry-point=liferay_dialogflow_webhook \
--trigger-http \
--allow-unauthenticated \
--memory=512MB \
--set-env-vars \
GOOGLE_CLOUD_PROJECT="<your_project_id>",GOOGLE_CLOUD_LOCATION="<your_location>",REASONING_ENGINE_ID="<your_agent_id>"
```

Replace the following placeholders:
- `YOUR_REGION`: Your Google Cloud region (e.g., us-central1)
- `<your_project_id>`: Your Google Cloud project ID
- `<your_location>`: The location of your reasoning engine
- `<your_agent_id>`: The ID of your deployed reasoning engine

## Local Development

For local testing, you can set environment variables in your shell:

```bash
export GOOGLE_CLOUD_PROJECT="your-project-id"
export GOOGLE_CLOUD_LOCATION="your-location"
export REASONING_ENGINE_ID="your-agent-id"
```

## Setup Steps

1. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

2. Deploy using the gcloud command above with your actual values.

3. The function will be available at the URL provided after deployment.
