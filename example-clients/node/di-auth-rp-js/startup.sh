npm install

export PORT=8081

export OAUTH_AUTHORIZATION_URL=http://localhost:8080/authorize
export OAUTH_TOKEN_URL=http://localhost:8080/token
export OAUTH_CLIENT_ID=some_client_id
export OAUTH_CLIENT_SECRET=password
export OAUTH_CALLBACK_URL=http://localhost:8081/oidc/callback
export OAUTH_USERINFO_URL=http://localhost:8080/userinfo
export OAUTH_LOGOUT_URL=http://localhost:8080/logout?redirectUri=http://localhost:8081

DEBUG=di-auth-rp-js:* npm start
