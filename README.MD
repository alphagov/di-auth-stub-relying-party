# di-auth-stub-relying party

## Summary
This stub allows you to set various paramaters (see [here]("https://docs.sign-in.service.gov.uk/integrate-with-integration-environment/")) and mock calls from an RP.

## Local Setup
You'll need to set up environment variables to run locally. Make a copy of the `.env.template` file, and rename it `.env`.
The stub is currently set up to connect to the build environment (as sandpit blocks requests from localhost). Once you 
have a copy of `.env`, you'll need to go into the `build-client-registry` table in DynamoDB, and find the client with ClientName `di-auth-stub-relying-party-build`.
Update the value of `CLIENT_ID` to the ClientID of this client.

To run the stub locally, run `docker compose up`.

It will be accessible on port 8080.

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

## Adding secrets

IPV have a tool that manages secrets in the stubs AWS account. If you need to add any further Secrets Manager secrets, 
you should add the name of the secret to the externally managed list [here](https://github.com/govuk-one-login/ipv-stubs-common-infra/blob/main/utils/config-mgmt/app/configs/stubs.build.params.yaml) for non prod,
or [here](https://github.com/govuk-one-login/ipv-stubs-common-infra/blob/main/utils/config-mgmt/app/configs/stubs.production.params.yaml) for prod.