package uk.gov.di.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.utils.ViewHelper;

public class SignedOutHandler implements Route {

    private static final Logger LOG = LoggerFactory.getLogger(SignedOutHandler.class);

    @Override
    public Object handle(Request request, Response response) {
        LOG.info("Request received in SignedOutHandler");
        return ViewHelper.render(null, "signedout.mustache");
    }
}
