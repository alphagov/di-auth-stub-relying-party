import logging
from flask import Flask, request, session, render_template, redirect
from os import environ
from oic.oic import Client
from oic.utils.authn.client import CLIENT_AUTHN_METHOD
from oic.oic.message import AuthorizationResponse
from oic import rndstr
from oic.oauth2.grant import Grant
from oic.utils.http_util import Redirect

app = Flask(__name__)
app.secret_key = '0D8BBA24-0150-4C7E-8B06-8F379BC46687'
cache = {}

logging.basicConfig()
logger = logging.getLogger('oic.oauth2')
app.config['BASE_URL'] = environ.get('BASE_URL')
app.config['CLIENT_ID'] = environ.get('CLIENT_ID')
app.config['CLIENT_SECRET'] = environ.get('CLIENT_SECRET')

@app.route('/')
def home():
    return render_template('home.html')

@app.route('/register')
def register():
    client = Client(client_authn_method=CLIENT_AUTHN_METHOD, verify_ssl=False)
    provider_info = _make_wellknown_request(client)
    registration_response = _make_registration_request(client, provider_info["registration_endpoint"])
    return registration_response.values()

@app.route('/authorize')
def make_authorization_request():
    client = Client(client_authn_method=CLIENT_AUTHN_METHOD, verify_ssl=False)
    provider_info = _make_wellknown_request(client)
    client.store_registration_info(_generate_reg_info())
    session['state'] = rndstr()
    session["nonce"] = rndstr()
    args = {
        "client_id": client.client_id,
        "response_type": "code",
        "scope": ["openid"],
        "redirect_uri": "http://localhost:5000/callback",
        "state": session.get('state'),
        "nonce": session.get('nonce')
    }
    auth_req = client.construct_AuthorizationRequest(request_args=args)
    login_url = auth_req.request(client.authorization_endpoint)
    cache['client'] = client
    return Redirect(login_url)

@app.route('/callback')
def callback():
    client = cache['client']
    auth_response = client.parse_response(AuthorizationResponse, info=request.query_string.decode('utf-8'), sformat="urlencoded")
    token_response = _make_token_request(client, auth_response["code"])
    userinfo_response = _make_userinfo_request(client, token_response["access_token"])

    return render_template('userinfo.html', email = userinfo_response.to_dict()['email'])

@app.route('/logout', methods=['GET', 'POST'])
def logout():
    return redirect(app.config['BASE_URL'] + "/logout?redirectUri=http://localhost:5000/", code=302)

def _make_wellknown_request(client):
    uid = app.config['BASE_URL']
    return client.provider_config(uid)

def _make_registration_request(client, registration_endpoint): 
    args = {
        "client_name": "python-client",
        "redirect_uris": ['http://localhost:5000/callback'],
        "contacts": ["foo@example.com"]
    }
    return client.register(registration_endpoint, **args)

def _make_token_request(client, code):
    args = {
        "code": code,
        "redirect_uri": 'http://localhost:5000/callback',
        "client_id": client.client_id,
        "client_secret": client.client_secret
    }
    return client.do_access_token_request(scope="openid", state=session.get("state"), method="POST", request_args=args, authn_method="client_secret_post")

def _make_userinfo_request(client, access_token):
    return client.do_user_info_request(method="GET", token=access_token, behavior="use_authorization_header")

def _generate_reg_info():
    reginfo = dict()
    reginfo["client_secret"] = app.config['CLIENT_SECRET']
    reginfo["client_id"] = app.config['CLIENT_ID']
    reginfo["redirect_uris"] = 'http://localhost:5000/callback'
    return reginfo

