package uk.gov.di;

import uk.gov.di.handlers.SignOutHandler;
import uk.gov.di.handlers.HomeHandler;
import uk.gov.di.handlers.AuthorizeHandler;
import uk.gov.di.handlers.AuthCallbackHandler;
import uk.gov.di.handlers.SignedOutHandler;
import uk.gov.di.handlers.ErrorHandler;
import uk.gov.di.utils.Oidc;
import uk.gov.di.utils.PrivateKeyReader;

import static spark.Spark.port;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;
import static spark.Spark.path;
import static spark.Spark.internalServerError;
import static uk.gov.di.config.RelyingPartyConfig.CLIENT_ID;
import static uk.gov.di.config.RelyingPartyConfig.PORT;
import static uk.gov.di.config.RelyingPartyConfig.CLIENT_PRIVATE_KEY;
import static uk.gov.di.config.RelyingPartyConfig.IDP_URL;


public class OidcRp {
    public OidcRp(){
        staticFileLocation("/public");
        port(Integer.parseInt(PORT));

        initRoutes();
    }

    public void initRoutes(){
        var oidcClient = new Oidc(IDP_URL, CLIENT_ID, new PrivateKeyReader(CLIENT_PRIVATE_KEY));

        var homeHandler = new HomeHandler();
        var authorizeHandler = new AuthorizeHandler(oidcClient);
        var authCallbackHandler = new AuthCallbackHandler(oidcClient);
        var logoutHandler = new SignOutHandler(oidcClient);
        var signedOutHandler = new SignedOutHandler();
        var errorHandler = new ErrorHandler();

        get("/", homeHandler);

        path("/oidc", () -> {
            get("/auth", authorizeHandler);
            get("/authorization-code/callback", authCallbackHandler);
        });

        post("/logout", logoutHandler);
        get("/signed-out", signedOutHandler);

        internalServerError(errorHandler);
    }
}
