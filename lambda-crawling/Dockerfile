FROM mcr.microsoft.com/playwright/python:v1.58.0-jammy

# Lambda Runtime Interface Client
RUN pip install awslambdaric

# Lambda Runtime Interface Emulator 다운로드
ADD https://github.com/aws/aws-lambda-runtime-interface-emulator/releases/latest/download/aws-lambda-rie /usr/local/bin/aws-lambda-rie
RUN chmod +x /usr/local/bin/aws-lambda-rie

WORKDIR /var/task

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY crawling/ ./crawling/
COPY crawling_lambda_handler.py .

ENTRYPOINT ["/usr/local/bin/aws-lambda-rie", "python", "-m", "awslambdaric"]
CMD ["crawling_lambda_handler.handler"]
