package uk.gov.di.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.config.RelyingPartyConfig;
import uk.gov.di.utils.Oidc;

import java.util.UUID;

public class SignOutHandler implements Route {

    private static final Logger LOG = LogManager.getLogger(SignOutHandler.class);

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
