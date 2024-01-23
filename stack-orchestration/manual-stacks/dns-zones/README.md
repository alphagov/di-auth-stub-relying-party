# Manual stacks - DNS
## Intro

The SAM template creates a Record and certificate for `<subdomain>.stubs.account.gov.uk`
or `<subdomain>.<environment>.stubs.account.gov.uk` if environment is not `production`.

This Stack is deployed manually once per environment
as part of the DNS set up process. The production stack lives in `di-ipv-stubs-prod`
and all non-production environments live in `di-ipv-stubs-build` .

The records are created in a hosted zone owned by IPV. They also reference API gateways in the
[template file](../../../template.yaml). 

### Domains

The template creates record for the following subdomain(s):

- `rp`
- `doc-app-rp`

## Deployment

To deploy the template to the appropriate AWS account.

Replace `<environment>` with `dev`, `build`, `staging`, `integration`, `production` in either of the commands below.

```bash
sam deploy --stack-name orch-dns-zones-<environment> \
  --template-file template.yaml \
  --parameter-overrides Environment=<environment>
```