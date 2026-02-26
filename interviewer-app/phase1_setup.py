import boto3
import json
import os

AWS_ACCESS_KEY_ID     = os.environ["IV_AWS_ACCESS_KEY_ID"]
AWS_SECRET_ACCESS_KEY = os.environ["IV_AWS_SECRET_ACCESS_KEY"]
AWS_REGION            = os.environ.get("IV_AWS_REGION", "us-east-1")

session = boto3.Session(
    aws_access_key_id=AWS_ACCESS_KEY_ID,
    aws_secret_access_key=AWS_SECRET_ACCESS_KEY,
    region_name=AWS_REGION,
)

dynamodb = session.client("dynamodb")
s3 = session.client("s3")

# ── DynamoDB Tables ──────────────────────────────────────────────

TABLES = [
    {
        "TableName": "IV_Users",
        "KeySchema": [{"AttributeName": "userId", "KeyType": "HASH"}],
        "AttributeDefinitions": [{"AttributeName": "userId", "AttributeType": "S"}],
        "BillingMode": "PAY_PER_REQUEST",
    },
    {
        "TableName": "IV_InterviewSessions",
        "KeySchema": [{"AttributeName": "sessionId", "KeyType": "HASH"}],
        "AttributeDefinitions": [{"AttributeName": "sessionId", "AttributeType": "S"}],
        "BillingMode": "PAY_PER_REQUEST",
    },
    {
        "TableName": "IV_SessionMessages",
        "KeySchema": [
            {"AttributeName": "sessionId", "KeyType": "HASH"},
            {"AttributeName": "messageId", "KeyType": "RANGE"},
        ],
        "AttributeDefinitions": [
            {"AttributeName": "sessionId", "AttributeType": "S"},
            {"AttributeName": "messageId", "AttributeType": "S"},
        ],
        "BillingMode": "PAY_PER_REQUEST",
    },
    {
        "TableName": "IV_CodeSubmissions",
        "KeySchema": [
            {"AttributeName": "sessionId", "KeyType": "HASH"},
            {"AttributeName": "submissionId", "KeyType": "RANGE"},
        ],
        "AttributeDefinitions": [
            {"AttributeName": "sessionId", "AttributeType": "S"},
            {"AttributeName": "submissionId", "AttributeType": "S"},
        ],
        "BillingMode": "PAY_PER_REQUEST",
    },
    {
        "TableName": "IV_ParsedKnowledgeFiles",
        "KeySchema": [{"AttributeName": "fileId", "KeyType": "HASH"}],
        "AttributeDefinitions": [{"AttributeName": "fileId", "AttributeType": "S"}],
        "BillingMode": "PAY_PER_REQUEST",
    },
    {
        "TableName": "IV_HiringEvaluations",
        "KeySchema": [{"AttributeName": "sessionId", "KeyType": "HASH"}],
        "AttributeDefinitions": [{"AttributeName": "sessionId", "AttributeType": "S"}],
        "BillingMode": "PAY_PER_REQUEST",
    },
]

print("Creating DynamoDB tables...")
for table in TABLES:
    try:
        dynamodb.create_table(**table)
        print(f"  ✓ Created {table['TableName']}")
    except dynamodb.exceptions.ResourceInUseException:
        print(f"  ~ Already exists: {table['TableName']}")
    except Exception as e:
        print(f"  ✗ Failed {table['TableName']}: {e}")

# ── S3 Bucket ────────────────────────────────────────────────────

print("\nCreating S3 bucket...")
try:
    s3.create_bucket(Bucket="interviewverse-uploads")
    # Block all public access
    s3.put_public_access_block(
        Bucket="interviewverse-uploads",
        PublicAccessBlockConfiguration={
            "BlockPublicAcls": True,
            "IgnorePublicAcls": True,
            "BlockPublicPolicy": True,
            "RestrictPublicBuckets": True,
        },
    )
    print("  ✓ Created interviewverse-uploads (public access blocked)")
except s3.exceptions.BucketAlreadyOwnedByYou:
    print("  ~ Already exists: interviewverse-uploads")
except Exception as e:
    print(f"  ✗ Failed S3: {e}")

print("\n✅ Phase 1 complete — AWS infrastructure ready.")
