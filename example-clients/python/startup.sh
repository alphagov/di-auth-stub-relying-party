if ! docker info >/dev/null 2>&1; then
  echo "Docker isn't running. Start docker and try again"
  exit 1
fi


USE_LOCAL_OIDC_PROVIDER=0
while [[ $# -gt 0 ]]; do
  case $1 in
  --local)
    USE_LOCAL_OIDC_PROVIDER=1
    ;;
  --paas)
    USE_LOCAL_OIDC_PROVIDER=0
    ;;
  esac
  shift
done

docker build -t python-client .
if [[ ${USE_LOCAL_OIDC_PROVIDER} == "1" ]]; then
    docker run -it -p 5000:5000 \
      -e BASE_URL=http://localhost:8080 \
      -e CLIENT_ID=some_client_id \
      -e CLIENT_SECRET=password \
      python-client 
else
    docker run -it -p 5000:5000 \
      -e BASE_URL=https://di-auth-oidc-provider.london.cloudapps.digital \
      -e CLIENT_ID=some_client_id \
      -e CLIENT_SECRET=password \
      python-client
fi
