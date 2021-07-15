package uk.gov.di.handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.utils.ViewHelper;

import java.util.HashMap;

import static uk.gov.di.config.RelyingPartyConfig.SERVICE_NAME;

public class HomeHandler {
    public static Route serveHomePage = (Request request, Response response) -> {
        var model = new HashMap<>();
        model.put("servicename", SERVICE_NAME);
        return ViewHelper.render(model, "home.mustache");
    };
}
