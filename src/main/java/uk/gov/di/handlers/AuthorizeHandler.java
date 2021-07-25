package uk.gov.di.handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.utils.Oidc;

import static uk.gov.di.config.RelyingPartyConfig.AUTH_CALLBACK_URL;

public class AuthorizeHandler implements Route {

    private Oidc oidcClient;

    public AuthorizeHandler(Oidc oidc){
        this.oidcClient = oidc;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.redirect(oidcClient.buildAuthorizeRequest(AUTH_CALLBACK_URL));
        return null;
    }
}
