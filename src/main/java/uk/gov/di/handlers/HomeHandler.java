package uk.gov.di.handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.config.RelyingPartyConfig;
import uk.gov.di.utils.ViewHelper;

import java.util.HashMap;

public class HomeHandler implements Route {
    @Override
    public Object handle(Request request, Response response) {
        request.session(true);

        var model = new HashMap<>();
        model.put("servicename", RelyingPartyConfig.serviceName());
        return ViewHelper.render(model, "home.mustache");
    }
}
