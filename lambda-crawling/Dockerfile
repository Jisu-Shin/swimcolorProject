FROM public.ecr.aws/lambda/python:3.12

# 필요한 시스템 패키지
RUN dnf install -y \
    atk \
    cups-libs \
    gtk3 \
    libXcomposite \
    libXcursor \
    libXdamage \
    libXext \
    libXi \
    libXrandr \
    libXScrnSaver \
    libXtst \
    pango \
    alsa-lib \
    libX11 \
    libXfixes \
    libdrm \
    mesa-libgbm \
    nss \
    && dnf clean all

# playwright 설치
RUN pip install --no-cache-dir playwright \
    && playwright install chromium

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . ${LAMBDA_TASK_ROOT}

CMD ["crawling_lambda_handler.handler"]
