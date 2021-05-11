# Python Flask OIDC relying party example

This is an example OIDC client implementation written in Python and uses the [Flask](https://flask.palletsprojects.com/en/1.1.x/) framework. In addition, this application uses the [OpenIDC/pyoidc](https://github.com/OpenIDC/pyoidc) library which handles the majority of the OIDC behaviour.

This application has specifically been configured to run against the [di-auth-oidc-provider](https://github.com/alphagov/di-auth-oidc-provider) OIDC provider.

## Prerequisites

* [docker](https://www.docker.com/) installed on your machine.

## Run the application against the di-auth-oidc-provider which runs on the PaaS

* Run `startup.sh` in the terminal
* Access the application home page via `http://0.0.0.0:5000/` on a web browser

## Run the application against a local version of the di-auth-oidc-provider

* Run `startup.sh --local` in the terminal
* Access the application home page via `http://0.0.0.0:5000/` on a web browser

