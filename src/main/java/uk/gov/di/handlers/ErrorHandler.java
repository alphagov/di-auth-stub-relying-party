package uk.gov.di.handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.utils.ViewHelper;

import java.util.HashMap;

public class ErrorHandler implements Route {
    @Override
    public Object handle(Request request, Response response) {
        var model = new HashMap<>();
        model.put("error", request.queryParams("error"));
        model.put("error_description", request.queryParams("error_description"));

        return ViewHelper.render(model, "error.mustache");
    }
}
