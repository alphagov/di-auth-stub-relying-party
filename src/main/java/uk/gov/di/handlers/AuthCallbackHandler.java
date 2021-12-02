package uk.gov.di.handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.utils.Oidc;
import uk.gov.di.utils.ViewHelper;

import java.util.HashMap;

import static uk.gov.di.config.RelyingPartyConfig.AUTH_CALLBACK_URL;
import static uk.gov.di.config.RelyingPartyConfig.MY_ACCOUNT_URL;

public class AuthCallbackHandler implements Route {

    private Oidc oidcClient;

    public AuthCallbackHandler(Oidc oidc){
        this.oidcClient = oidc;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        var tokens = oidcClient.makeTokenRequest(request.queryParams("code"), AUTH_CALLBACK_URL);

        oidcClient.validateIdToken(tokens.getIDToken());
        request.session().attribute("idToken",
                tokens.getIDToken().getParsedString());

        var userInfo = oidcClient.makeUserInfoRequest(tokens.getAccessToken());

        var model = new HashMap<>();
        model.put("email", userInfo.getEmailAddress());
        model.put("phone_number", userInfo.getPhoneNumber());
        model.put("my_account_url", MY_ACCOUNT_URL);
        model.put("id_token", tokens.getIDToken().getParsedString());

        return ViewHelper.render(model, "userinfo.mustache");
    }
}
