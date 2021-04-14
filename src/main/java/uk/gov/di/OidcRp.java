package uk.gov.di;

import static spark.Spark.port;
import static spark.Spark.staticFileLocation;
import static spark.Spark.path;
import static spark.Spark.get;
import static spark.Spark.internalServerError;
import static uk.gov.di.handlers.HomeHandler.serveHomePage;
import static uk.gov.di.handlers.OidcHandler.doAuthorize;
import static uk.gov.di.handlers.OidcHandler.doAuthCallback;

public class OidcRp {
    public OidcRp(){
        staticFileLocation("/public");
        port(8081);

        InitRoutes();
    }

    public void InitRoutes(){
        get("/", serveHomePage);

        path("/oidc", () -> {
            get("/auth", doAuthorize);
            get("/callback", doAuthCallback);
        });

        internalServerError("<html><body><h1>Oops something went wrong</h1></body></html>");
    }
}
