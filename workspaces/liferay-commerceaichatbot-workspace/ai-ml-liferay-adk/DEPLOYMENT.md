# Liferay Customer Service Agent - Deployment Guide

This guide explains how to deploy your Liferay Customer Service Agent to Google Cloud using Agent Engines.

## Prerequisites

1. **Google Cloud Project** with the following APIs enabled:
   - Vertex AI API
   - Cloud Storage API
   - Cloud Functions API
   - Cloud Build API (required for Cloud Functions)

2. **Authentication** set up:
   ```bash
   gcloud auth login
   gcloud auth application-default login
   ```

3. **Environment Variables** configured in your `.env` file:
   ```bash
   # Google Cloud Configuration
   GOOGLE_CLOUD_PROJECT=your-project-id
   GOOGLE_CLOUD_LOCATION=us-central1
   GOOGLE_CLOUD_STORAGE_BUCKET=your-gcs-bucket-name
   GOOGLE_GENAI_USE_VERTEXAI=True
   
   # Liferay Configuration
   LIFERAY_BASE_URL=https://your-liferay-instance.com
   LIFERAY_USERNAME=your-username
   LIFERAY_PASSWORD=your-password
   
   # Deployment Configuration (filled after deployment)
   REASONING_ENGINE_ID=
   ```

## Deployment Steps

### 1. Deploy to Agent Engine

Deploy your agent to Google Cloud Vertex AI Agent Engine:

```bash
# Deploy the agent
poetry run deploy-agent --create

# Or use Python directly
python deployment/deploy.py --create
```

This will:
- Package your Liferay agent
- Deploy it to Vertex AI Agent Engine
- Return a `REASONING_ENGINE_ID` that you need to save

### 2. Deploy Cloud Function

Deploy the Cloud Function that acts as a webhook for Dialogflow:

```bash
# Deploy the Cloud Function (2nd gen)
gcloud functions deploy liferay-dialogflow-webhook \
  --gen2 \
  --region=us-central1 \
  --runtime=python311 \
  --source=./cloud_function \
  --entry-point=liferay_dialogflow_webhook \
  --trigger-http \
  --allow-unauthenticated \
  --memory=512MB \
  --set-env-vars GOOGLE_CLOUD_PROJECT="your-project-id",GOOGLE_CLOUD_LOCATION="us-central1",REASONING_ENGINE_ID="your-reasoning-engine-id"
```

**Note:** Replace `your-project-id` and `your-reasoning-engine-id` with the actual values from step 1.

This will:
- Deploy a Cloud Function that acts as a webhook
- Configure it to connect to your deployed reasoning engine
- Return a webhook URL for Dialogflow integration

### 3. Update Environment Variables

After deployment, update your `.env` file with the returned `REASONING_ENGINE_ID`:

```bash
REASONING_ENGINE_ID=projects/your-project/locations/us-central1/reasoningEngines/123456789
```

### 4. Test the Deployed Agent

Test your deployed agent to ensure it's working correctly:

```bash
# Test the deployed agent
poetry run test-deployed-agent

# Or use Python directly
python deployment/test_deploy.py
```

This will create an interactive session where you can test queries like:
- "What can you help me with?"
- "Find orders for Nexus Accessories"
- "Show me system status"

### 5. Configure Dialogflow Integration

After both deployments are complete, you'll have:

1. **Reasoning Engine ID**: `projects/your-project/locations/us-central1/reasoningEngines/123456789`
2. **Cloud Function Webhook URL**: `https://us-central1-your-project.cloudfunctions.net/liferay-dialogflow-webhook`

Configure your Dialogflow agent to use the Cloud Function webhook URL for fulfillment.

### 6. Your Agent is Ready!

Your Liferay Customer Service Agent is now deployed and ready to use. You can:

- Test it using the test script
- Access it programmatically using the REASONING_ENGINE_ID
- Integrate it into your applications

## Management Commands

### List Deployed Agents
```bash
poetry run deploy-agent --list
```

### Delete an Agent
```bash
poetry run deploy-agent --delete --resource_id=<reasoning-engine-id>
```

### Cloud Function Management

#### List Cloud Functions
```bash
gcloud functions list --region=us-central1
```

#### Get Cloud Function Details
```bash
gcloud functions describe liferay-dialogflow-webhook --region=us-central1
```

#### Update Cloud Function Environment Variables
```bash
gcloud functions deploy liferay-dialogflow-webhook \
  --gen2 \
  --region=us-central1 \
  --update-env-vars REASONING_ENGINE_ID="new-reasoning-engine-id"
```

#### Delete Cloud Function
```bash
gcloud functions delete liferay-dialogflow-webhook --region=us-central1
```

#### View Cloud Function Logs
```bash
gcloud functions logs read liferay-dialogflow-webhook --region=us-central1
```

## Environment Variables Reference

### Required for Deployment
- `GOOGLE_CLOUD_PROJECT`: Your GCP project ID
- `GOOGLE_CLOUD_LOCATION`: GCP region (e.g., us-central1)
- `GOOGLE_CLOUD_STORAGE_BUCKET`: GCS bucket for staging
- `GOOGLE_GENAI_USE_VERTEXAI`: Set to "True"

### Required for Liferay Integration
- `LIFERAY_BASE_URL`: Your Liferay instance URL
- `LIFERAY_USERNAME`: Liferay username
- `LIFERAY_PASSWORD`: Liferay password

### Generated After Deployment
- `REASONING_ENGINE_ID`: Returned after successful deployment

## Troubleshooting

### Common Issues

1. **Authentication Errors**
   ```bash
   gcloud auth application-default login
   ```

2. **Missing APIs**
   ```bash
   gcloud services enable aiplatform.googleapis.com
   gcloud services enable storage.googleapis.com
   gcloud services enable cloudfunctions.googleapis.com
   gcloud services enable cloudbuild.googleapis.com
   ```

3. **Permission Errors**
   - Ensure your account has the necessary IAM roles:
     - Vertex AI User (or aiplatform.admin)
     - Storage Admin
     - Cloud Functions Admin (or cloudfunctions.admin)

4. **Cloud Function Issues**
   - **Container failed to start**: Check that REASONING_ENGINE_ID is set correctly
   - **Permission denied**: Ensure Cloud Functions Admin role is assigned
   - **Build failed**: Check Cloud Build logs for detailed error messages
   - **Function not responding**: Verify the reasoning engine is deployed and accessible

5. **Liferay Connection Issues**
   - Verify your Liferay credentials
   - Check that your Liferay instance is accessible
   - Ensure the API endpoints are available

### Getting Help

- Check the deployment logs for detailed error messages
- Verify all environment variables are set correctly
- Test the Liferay connection locally first with `poetry run test-liferay-client`

## Cost Considerations

- **Agent Engine**: Charges based on usage (queries, compute time)
- **Cloud Functions**: Charges based on invocations, compute time, and memory usage
- **Cloud Storage**: Minimal cost for staging files
- **Cloud Build**: Charges for build minutes (usually minimal for small functions)

Monitor your usage in the Google Cloud Console to track costs.
