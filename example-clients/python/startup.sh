export FLASK_APP=oidc-rp.py
pip3 install -r requirements.txt

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

if [[ ${USE_LOCAL_OIDC_PROVIDER} == "1" ]]; then
    export BASE_URL=http://localhost:8080
else
    export BASE_URL=https://di-auth-oidc-provider.london.cloudapps.digital
fi

flask run
