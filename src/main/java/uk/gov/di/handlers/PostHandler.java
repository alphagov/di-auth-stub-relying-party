package uk.gov.di.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.config.RelyingPartyConfig;
import uk.gov.di.utils.Oidc;
import uk.gov.di.utils.ViewHelper;

import java.util.HashMap;

public class PostHandler implements Route {

    private static final Logger LOG = LoggerFactory.getLogger(PostHandler.class);
    private final Oidc oidcClient;

    public PostHandler(Oidc oidc) {
        this.oidcClient = oidc;
    }

    @Override
    public Object handle(Request request, Response response) {
        request.session(true);

        var model = new HashMap<>();
        model.put("servicename", RelyingPartyConfig.serviceName());
        model.put("endpoint_address", oidcClient.getAuthorizationEndpoint());
        request.queryParams().forEach(i -> model.putIfAbsent(i, request.queryParams(i)));

        LOG.info(
                "Rendering RP with serviceName: {} and clientType: {}",
                RelyingPartyConfig.serviceName(),
                RelyingPartyConfig.clientType());
        return ViewHelper.render(model, "post-page.mustache");
    }
}
