package uk.gov.di.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.config.RelyingPartyConfig;
import uk.gov.di.utils.ViewHelper;

import java.util.HashMap;

public class HomeHandler implements Route {

    private static final Logger LOG = LoggerFactory.getLogger(HomeHandler.class);

    @Override
    public Object handle(Request request, Response response) {
        request.session(true);

        var model = new HashMap<>();
        model.put("servicename", RelyingPartyConfig.serviceName());
        LOG.info(
                "Rendering RP with serviceName: {} and clientType: {}",
                RelyingPartyConfig.serviceName(),
                RelyingPartyConfig.clientType());
        if (RelyingPartyConfig.clientType().equals("app")) {
            return ViewHelper.render(model, "app-home.mustache");
        } else {
            return ViewHelper.render(model, "home.mustache");
        }
    }
}
