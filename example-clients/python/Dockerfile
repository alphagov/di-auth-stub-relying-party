FROM python:3.9.5-slim
COPY . /app
WORKDIR /app
RUN pip3 install --upgrade pip && \
    pip3 install -r /app/requirements.txt
ENV FLASK_APP="app/oidc-rp.py"
ENV BASE_URL=https://di-auth-oidc-provider.london.cloudapps.digital
EXPOSE 80
CMD ["flask", "run", "--host=0.0.0.0"]