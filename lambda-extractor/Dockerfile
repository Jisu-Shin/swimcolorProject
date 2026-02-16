FROM public.ecr.aws/lambda/python:3.9

COPY requirements.txt .

RUN pip install --upgrade pip

# torch/torchvision PyTorch 인덱스에서 설치
RUN pip install --no-cache-dir \
    torch==2.5.0 \
    torchvision \
    --index-url https://download.pytorch.org/whl/cpu

RUN pip install --no-cache-dir -r requirements.txt

COPY . .

CMD ["color_lambda_handler.handler"]