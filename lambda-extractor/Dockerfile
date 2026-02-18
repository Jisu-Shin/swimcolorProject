FROM public.ecr.aws/lambda/python:3.9

# 시스템 라이브러리
RUN yum install -y \
    mesa-libGL \
    libXext \
    libSM \
    libXrender \
    && yum clean all

# Lambda writable 디렉토리 설정
ENV MPLCONFIGDIR=/tmp/matplotlib
ENV PYTHONUNBUFFERED=1

COPY requirements.txt .

RUN pip install --upgrade pip
RUN pip install --no-cache-dir -r requirements.txt

COPY . ${LAMBDA_TASK_ROOT}

CMD ["color_lambda_handler.handler"]