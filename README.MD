# di-auth-stub-relying party

## Summary
This stub allows you to set various paramaters (see [here]("https://docs.sign-in.service.gov.uk/integrate-with-integration-environment/")) and mock calls from an RP.

## Local Setup
You will need to have the `di-authentication-api` repo locally (In the same parent directory by default)

To run the stub locally, and make calls to sandpit (defined by `OP_BASE_URL`) you can run:
```shell
export OP_BASE_URL="https://oidc.sandpit.account.gov.uk/";
./startup.sh
```

It will be accessible on port 8081.

## Deploy To Sandpit
**This is due to be moved to aws. It is difficult to deploy as PaaS is EOL.**

To deploy to the sandpit enviroment, you will need access to the PaaS service. You should then enable SSO (see [here](https://docs.cloud.service.gov.uk/get_started.html#enable-single-sign-on)).

You also need cloud foundry (cf) locally setup.
`brew install cloudfoundry/tap/cf-cli@8`

You first login using:
```
cf login -a api.london.cloud.service.gov.uk --sso
```

Then choose sandpit:
```
Select a space:
1. build
2. integration
3. production
4. sandbox
5. sandpit
6. staging
```

Next execute `./deploy-sandpit.sh`