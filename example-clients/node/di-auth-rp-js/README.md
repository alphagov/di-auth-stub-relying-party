# di-auth-rp-js

Sample stub RP built on node.js using:

- https://expressjs.com/
- http://www.passportjs.org/
- http://www.passportjs.org/packages/passport-oauth2/

## How to build and run

Requires a local installation of node.js and npm:

    brew install node

Run `startup.sh` to install the packages and start the server which runs at http://localhost:8081.

Currently connects to the [local oidc provider](https://github.com/alphagov/di-auth-oidc-provider) which must be started at the same time in order to run the flow.

Update the environment variables in `startup.sh` to point to another OIDC provider or to use a different client.
