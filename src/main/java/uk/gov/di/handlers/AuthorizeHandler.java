package uk.gov.di.handlers;

import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.openid.connect.sdk.OIDCClaimsRequest;
import com.nimbusds.openid.connect.sdk.claims.ClaimRequirement;
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

            if (RelyingPartyConfig.clientType().equals("app")) {
                scopes.add("doc-checking-app");
                response.redirect(
                        oidcClient.buildSecureAuthorizeRequest(
                                RelyingPartyConfig.authCallbackUrl(), Scope.parse(scopes)));
                return null;
            }

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

            if (formParameters.containsKey("loc") && !formParameters.get("loc").isEmpty()) {
                vtr = "%s.%s".formatted(formParameters.get("loc"), vtr);
            }

            var claimsSetRequest = new ClaimsSetRequest();

            if (formParameters.containsKey("claims-core-identity")) {
                var identityEntry =
                        new ClaimsSetRequest.Entry(formParameters.get("claims-core-identity")).withClaimRequirement(ClaimRequirement.ESSENTIAL);
                claimsSetRequest.add(identityEntry);
            }

            if (formParameters.containsKey("claims-passport")) {
                var passportEntry =
                        new ClaimsSetRequest.Entry(formParameters.get("claims-passport")).withClaimRequirement(ClaimRequirement.ESSENTIAL);
                claimsSetRequest.add(formParameters.get(passportEntry));
            }

            if (formParameters.containsKey("claims-address")) {
                var addressEntry =
                        new ClaimsSetRequest.Entry(formParameters.get("claims-address")).withClaimRequirement(ClaimRequirement.ESSENTIAL);
                claimsSetRequest.add(formParameters.get(addressEntry));
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
