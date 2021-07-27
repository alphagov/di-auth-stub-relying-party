package uk.gov.di.handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.utils.Oidc;

import java.util.UUID;

import static uk.gov.di.config.RelyingPartyConfig.POST_LOGOUT_REDIRECT_URL;

public class SignOutHandler implements Route {
    private Oidc oidcClient;

    public SignOutHandler(Oidc oidc){
        this.oidcClient = oidc;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        var logoutUri = oidcClient.buildLogoutUrl(request.session().attribute("idToken"), UUID.randomUUID().toString(), POST_LOGOUT_REDIRECT_URL);
        response.redirect(logoutUri);
        return null;
    }
}
