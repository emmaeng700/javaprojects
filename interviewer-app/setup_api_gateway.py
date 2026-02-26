"""
Creates an AWS HTTP API Gateway (v2) for InterviewVerse.
- One route per Lambda function
- Lambda proxy integration for each
- Auto-deploy to $default stage
- Saves the invoke URL to .env as VITE_API_BASE_URL
"""

import boto3
import json
import os
import re

AWS_ACCESS_KEY_ID     = os.environ["IV_AWS_ACCESS_KEY_ID"]
AWS_SECRET_ACCESS_KEY = os.environ["IV_AWS_SECRET_ACCESS_KEY"]
AWS_REGION            = os.environ.get("IV_AWS_REGION", "us-east-1")

session = boto3.Session(
    aws_access_key_id=AWS_ACCESS_KEY_ID,
    aws_secret_access_key=AWS_SECRET_ACCESS_KEY,
    region_name=AWS_REGION,
)

apigw  = session.client("apigatewayv2")
lc     = session.client("lambda")
sts    = session.client("sts")

ACCOUNT_ID = sts.get_caller_identity()["Account"]

# Lambda name → route path
ROUTES = [
    ("IV_startSession",             "/startSession"),
    ("IV_transitionSection",        "/transitionSection"),
    ("IV_parseResume",              "/parseResume"),
    ("IV_generateQuestion",         "/generateQuestion"),
    ("IV_evaluateAnswer",           "/evaluateAnswer"),
    ("IV_handleFollowUp",           "/handleFollowUp"),
    ("IV_executeCode",              "/executeCode"),
    ("IV_validateComplexityAnswer", "/validateComplexityAnswer"),
    ("IV_interrogateCode",          "/interrogateCode"),
    ("IV_escalateCodingQuestion",   "/escalateCodingQuestion"),
    ("IV_endSession",               "/endSession"),
    ("IV_getHiringEvaluation",      "/getHiringEvaluation"),
    ("IV_evaluateDesign",           "/evaluateDesign"),
    ("IV_stressTestDesign",         "/stressTestDesign"),
    ("IV_getModeContext",           "/getModeContext"),
    ("IV_parseCheatSheet",          "/parseCheatSheet"),
    ("IV_processSpeech",            "/processSpeech"),
]


def get_lambda_arn(name: str) -> str:
    resp = lc.get_function(FunctionName=name)
    return resp["Configuration"]["FunctionArn"]


def add_lambda_permission(fn_name: str, api_id: str, route_path: str):
    stmt_id = f"apigw-{api_id}-{fn_name}"
    source_arn = (
        f"arn:aws:execute-api:{AWS_REGION}:{ACCOUNT_ID}:{api_id}/*/*{route_path}"
    )
    try:
        lc.add_permission(
            FunctionName=fn_name,
            StatementId=stmt_id,
            Action="lambda:InvokeFunction",
            Principal="apigateway.amazonaws.com",
            SourceArn=source_arn,
        )
    except lc.exceptions.ResourceConflictException:
        pass  # permission already exists


# ── Create HTTP API ────────────────────────────────────────────────────────────

print("Creating HTTP API...")
api = apigw.create_api(
    Name="InterviewVerseAPI",
    ProtocolType="HTTP",
    CorsConfiguration={
        "AllowOrigins": ["*"],
        "AllowMethods": ["GET", "POST", "OPTIONS"],
        "AllowHeaders": ["Content-Type", "Authorization"],
    },
)
api_id  = api["ApiId"]
api_url = api["ApiEndpoint"]
print(f"  ✓ API created: {api_id}")
print(f"  ✓ Endpoint:    {api_url}")

# ── Create integrations + routes ───────────────────────────────────────────────

print("\nWiring Lambda integrations...")
for fn_name, route_path in ROUTES:
    try:
        fn_arn = get_lambda_arn(fn_name)

        # Lambda proxy integration
        integration = apigw.create_integration(
            ApiId=api_id,
            IntegrationType="AWS_PROXY",
            IntegrationUri=fn_arn,
            PayloadFormatVersion="2.0",
            TimeoutInMillis=29000,
        )
        integration_id = integration["IntegrationId"]

        # POST route (most endpoints)
        method = "GET" if route_path in ("/getModeContext", "/getHiringEvaluation") else "POST"
        apigw.create_route(
            ApiId=api_id,
            RouteKey=f"{method} {route_path}",
            Target=f"integrations/{integration_id}",
        )

        # Grant API Gateway permission to invoke this Lambda
        add_lambda_permission(fn_name, api_id, route_path)

        print(f"  ✓ {method} {route_path}  →  {fn_name}")
    except Exception as e:
        print(f"  ✗ {route_path}: {e}")

# ── Auto-deploy stage ──────────────────────────────────────────────────────────

print("\nDeploying $default stage...")
apigw.create_stage(
    ApiId=api_id,
    StageName="$default",
    AutoDeploy=True,
)
print("  ✓ Stage deployed")

# ── Save URL to .env ───────────────────────────────────────────────────────────

env_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".env")
with open(env_path, "r") as f:
    contents = f.read()

vite_line = f"VITE_API_BASE_URL={api_url}"
if "VITE_API_BASE_URL" in contents:
    contents = re.sub(r"VITE_API_BASE_URL=.*", vite_line, contents)
else:
    contents = contents.rstrip() + f"\n{vite_line}\n"

with open(env_path, "w") as f:
    f.write(contents)

print(f"\n✅ API Gateway ready.")
print(f"   Invoke URL: {api_url}")
print(f"   Saved to .env as VITE_API_BASE_URL")
print(f"\n   Copy to frontend/.env.local:")
print(f"   VITE_API_BASE_URL={api_url}")
