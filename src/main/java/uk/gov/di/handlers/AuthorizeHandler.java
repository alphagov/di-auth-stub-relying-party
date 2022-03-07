package uk.gov.di.handlers;

import com.nimbusds.openid.connect.sdk.claims.ClaimsSetRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.config.RelyingPartyConfig;
import uk.gov.di.utils.Oidc;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthorizeHandler implements Route {

    private Oidc oidcClient;

    public AuthorizeHandler(Oidc oidc) {
        this.oidcClient = oidc;
    }

    @Override
    public Object handle(Request request, Response response) {

        try {
            List<String> scopes = new ArrayList<>();
            scopes.add("openid");

            var claimsSetRequest = new ClaimsSetRequest();

            List<NameValuePair> pairs =
                    URLEncodedUtils.parse(request.body(), Charset.defaultCharset());

            Map<String, String> formParameters =
                    pairs.stream()
                            .collect(
                                    Collectors.toMap(
                                            NameValuePair::getName, NameValuePair::getValue));

            if (formParameters.containsKey("scopes-email")) {
                scopes.add(formParameters.get("scopes-email"));
            }

            if (formParameters.containsKey("scopes-phone")) {
                scopes.add(formParameters.get("scopes-phone"));
            }

            String vtr = formParameters.get("2fa");

            if (formParameters.containsKey("loc")) {
                vtr = "%s.%s".formatted(formParameters.get("loc"), vtr);
            }

            if (formParameters.containsKey("claims-name")) {
                claimsSetRequest.add(formParameters.get("claims-name"));
            }

            if (formParameters.containsKey("claims-birthdate")) {
                claimsSetRequest.add(formParameters.get("claims-birthdate"));
            }

            if (formParameters.containsKey("claims-address")) {
                claimsSetRequest.add(formParameters.get("claims-address"));
            }

            response.redirect(
                    oidcClient.buildAuthorizeRequest(
                            RelyingPartyConfig.authCallbackUrl(), vtr, scopes, claimsSetRequest));
            return null;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
