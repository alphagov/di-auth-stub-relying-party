ENVIRONMENT=${1}

PROVISION_COMMAND="../../di-devplatform-deploy/stack-orchestration-tool/provisioner.sh"

export AUTO_APPLY_CHANGESET=true
export SKIP_AWS_AUTHENTICATION=true
export AWS_PAGER=""


## Provision ECR
if [ $ENVIRONMENT == "build" ]
then
  echo ecr
  $PROVISION_COMMAND "$ENVIRONMENT" auth-rp-stub-ecr container-image-repository LATEST
fi

## Provision secure pipelines
$PROVISION_COMMAND "$ENVIRONMENT" "$ENVIRONMENT-rp-stub-pipeline" sam-deploy-pipeline LATEST