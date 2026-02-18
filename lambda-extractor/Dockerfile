FROM public.ecr.aws/lambda/python:3.9

# libGL 설치
RUN yum install -y mesa-libGL

COPY requirements.txt .

RUN pip install --upgrade pip

RUN pip install --no-cache-dir -r requirements.txt

COPY . .

CMD ["color_lambda_handler.handler"]