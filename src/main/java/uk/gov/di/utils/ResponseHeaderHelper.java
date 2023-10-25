package uk.gov.di.utils;

import spark.Response;

public class ResponseHeaderHelper {
    private ResponseHeaderHelper() {
        throw new IllegalStateException("Utility Class");
    }

    public static void setHeaders(Response response) {
        response.header("Server", "govuk-sign-in-stub-rp");
        response.header("Content-Security-Policy", "default-src 'self'");
        response.header("X-Frame-Options", "DENY");
        response.header("X-XSS-Protection", "0");
        response.header("X-Content-Type-Options", "nosniff");
    }
}
