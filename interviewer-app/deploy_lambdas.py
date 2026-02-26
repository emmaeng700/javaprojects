"""
Deploys Lambda functions to AWS.
Packages handler + shared layer, uploads zip, creates or updates function.
"""

import boto3
import zipfile
import os
import io
import json

AWS_ACCESS_KEY_ID     = os.environ["IV_AWS_ACCESS_KEY_ID"]
AWS_SECRET_ACCESS_KEY = os.environ["IV_AWS_SECRET_ACCESS_KEY"]
AWS_REGION            = os.environ.get("IV_AWS_REGION", "us-east-1")

session = boto3.Session(
    aws_access_key_id=AWS_ACCESS_KEY_ID,
    aws_secret_access_key=AWS_SECRET_ACCESS_KEY,
    region_name=AWS_REGION,
)

lambda_client = session.client("lambda")
iam           = session.client("iam")

BASE = os.path.dirname(os.path.abspath(__file__))
SHARED_DIR = os.path.join(BASE, "lambdas", "shared")

ENV_VARS = {
    "Variables": {
        "IV_AWS_ACCESS_KEY_ID":     AWS_ACCESS_KEY_ID,
        "IV_AWS_SECRET_ACCESS_KEY": AWS_SECRET_ACCESS_KEY,
        "IV_AWS_REGION":            AWS_REGION,
        "GEMINI_API_KEY":           os.environ["GEMINI_API_KEY"],
    }
}

LAMBDAS = [
    {"name": "IV_startSession",      "dir": "startSession",      "handler": "handler.lambda_handler"},
    {"name": "IV_transitionSection", "dir": "transitionSection", "handler": "handler.lambda_handler"},
    {"name": "IV_parseResume",       "dir": "parseResume",       "handler": "handler.lambda_handler"},
    {"name": "IV_generateQuestion",  "dir": "generateQuestion",  "handler": "handler.lambda_handler"},
    {"name": "IV_evaluateAnswer",         "dir": "evaluateAnswer",         "handler": "handler.lambda_handler"},
    {"name": "IV_handleFollowUp",         "dir": "handleFollowUp",         "handler": "handler.lambda_handler"},
    {"name": "IV_executeCode",              "dir": "executeCode",              "handler": "handler.lambda_handler"},
    {"name": "IV_validateComplexityAnswer", "dir": "validateComplexityAnswer", "handler": "handler.lambda_handler"},
    {"name": "IV_interrogateCode",          "dir": "interrogateCode",          "handler": "handler.lambda_handler"},
    {"name": "IV_escalateCodingQuestion",   "dir": "escalateCodingQuestion",   "handler": "handler.lambda_handler"},
    {"name": "IV_endSession",               "dir": "endSession",               "handler": "handler.lambda_handler"},
    {"name": "IV_getHiringEvaluation",      "dir": "getHiringEvaluation",      "handler": "handler.lambda_handler"},
    {"name": "IV_evaluateDesign",           "dir": "evaluateDesign",           "handler": "handler.lambda_handler"},
    {"name": "IV_stressTestDesign",         "dir": "stressTestDesign",         "handler": "handler.lambda_handler"},
    {"name": "IV_getModeContext",           "dir": "getModeContext",           "handler": "handler.lambda_handler"},
    {"name": "IV_parseCheatSheet",          "dir": "parseCheatSheet",          "handler": "handler.lambda_handler"},
    {"name": "IV_processSpeech",            "dir": "processSpeech",            "handler": "handler.lambda_handler"},
]


def get_or_create_role():
    role_name = "InterviewVerseLambda"
    try:
        resp = iam.get_role(RoleName=role_name)
        print(f"  ~ Using existing IAM role: {role_name}")
        return resp["Role"]["Arn"]
    except iam.exceptions.NoSuchEntityException:
        pass

    trust = {
        "Version": "2012-10-17",
        "Statement": [{
            "Effect": "Allow",
            "Principal": {"Service": "lambda.amazonaws.com"},
            "Action": "sts:AssumeRole",
        }],
    }
    resp = iam.create_role(
        RoleName=role_name,
        AssumeRolePolicyDocument=json.dumps(trust),
        Description="InterviewVerse Lambda execution role",
    )
    arn = resp["Role"]["Arn"]

    # Attach basic + DynamoDB + S3 policies
    for policy in [
        "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole",
        "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess",
        "arn:aws:iam::aws:policy/AmazonS3FullAccess",
    ]:
        iam.attach_role_policy(RoleName=role_name, PolicyArn=policy)

    import time; time.sleep(10)  # IAM propagation delay
    print(f"  ✓ Created IAM role: {role_name}")
    return arn


def zip_lambda(lambda_dir: str) -> bytes:
    buf = io.BytesIO()
    with zipfile.ZipFile(buf, "w", zipfile.ZIP_DEFLATED) as zf:
        # Add handler
        handler_path = os.path.join(BASE, "lambdas", lambda_dir, "handler.py")
        zf.write(handler_path, "handler.py")
        # Add shared modules
        for fname in os.listdir(SHARED_DIR):
            if fname.endswith(".py"):
                zf.write(os.path.join(SHARED_DIR, fname), fname)
    buf.seek(0)
    return buf.read()


def deploy(fn: dict, role_arn: str):
    name    = fn["name"]
    handler = fn["handler"]
    code    = zip_lambda(fn["dir"])

    try:
        lambda_client.get_function(FunctionName=name)
        # Update existing — wait for any in-progress update to settle first
        lambda_client.get_waiter("function_updated").wait(FunctionName=name)
        lambda_client.update_function_code(FunctionName=name, ZipFile=code)
        lambda_client.get_waiter("function_updated").wait(FunctionName=name)
        lambda_client.update_function_configuration(
            FunctionName=name,
            Environment=ENV_VARS,
            Timeout=30,
            MemorySize=256,
        )
        print(f"  ✓ Updated {name}")
    except lambda_client.exceptions.ResourceNotFoundException:
        # Create new
        lambda_client.create_function(
            FunctionName=name,
            Runtime="python3.12",
            Role=role_arn,
            Handler=handler,
            Code={"ZipFile": code},
            Timeout=30,
            MemorySize=256,
            Environment=ENV_VARS,
        )
        print(f"  ✓ Created {name}")


if __name__ == "__main__":
    print("Getting IAM role...")
    role_arn = get_or_create_role()

    print("\nDeploying Lambda functions...")
    for fn in LAMBDAS:
        try:
            deploy(fn, role_arn)
        except Exception as e:
            print(f"  ✗ {fn['name']}: {e}")

    print("\n✅ Phase 2 deployment complete.")
