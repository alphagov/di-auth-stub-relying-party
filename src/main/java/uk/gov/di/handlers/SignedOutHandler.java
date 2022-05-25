package uk.gov.di.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.utils.ViewHelper;

public class SignedOutHandler implements Route {

    private static final Logger LOG = LogManager.getLogger(SignedOutHandler.class);

    @Override
    public Object handle(Request request, Response response) {
        LOG.info("Request received in SignedOutHandler");
        return ViewHelper.render(null, "signedout.mustache");
    }
}
