package uk.gov.di.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.config.RelyingPartyConfig;
import uk.gov.di.utils.Oidc;

import java.util.UUID;

public class SignOutHandler implements Route {

    private static final Logger LOG = LoggerFactory.getLogger(SignOutHandler.class);

    private final Oidc oidcClient;

    public SignOutHandler(Oidc oidc) {
        this.oidcClient = oidc;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        LOG.info("Generating log out request");
        var logoutUri =
                oidcClient.buildLogoutUrl(
                        request.session().attribute("idToken"),
                        UUID.randomUUID().toString(),
                        RelyingPartyConfig.postLogoutRedirectUrl());
        response.redirect(logoutUri);
        return null;
    }
}
