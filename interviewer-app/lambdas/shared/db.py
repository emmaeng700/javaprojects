import boto3
import os

AWS_REGION = os.environ.get("IV_AWS_REGION", "us-east-1")
AWS_ACCESS_KEY_ID = os.environ.get("IV_AWS_ACCESS_KEY_ID")
AWS_SECRET_ACCESS_KEY = os.environ.get("IV_AWS_SECRET_ACCESS_KEY")

session = boto3.Session(
    aws_access_key_id=AWS_ACCESS_KEY_ID,
    aws_secret_access_key=AWS_SECRET_ACCESS_KEY,
    region_name=AWS_REGION,
)

dynamodb = session.resource("dynamodb")

TABLES = {
    "sessions":    dynamodb.Table("IV_InterviewSessions"),
    "messages":    dynamodb.Table("IV_SessionMessages"),
    "users":       dynamodb.Table("IV_Users"),
    "code":        dynamodb.Table("IV_CodeSubmissions"),
    "evaluations": dynamodb.Table("IV_HiringEvaluations"),
    "knowledge":   dynamodb.Table("IV_ParsedKnowledgeFiles"),
}
