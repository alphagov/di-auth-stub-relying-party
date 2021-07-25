package uk.gov.di.handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.utils.ViewHelper;

public class SignedOutHandler implements Route {
    @Override
    public Object handle(Request request, Response response) {
        return ViewHelper.render(null,"signedout.mustache");
    }
}
