package uk.gov.di.handlers;

import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.openid.connect.sdk.claims.ClaimRequirement;
import com.nimbusds.openid.connect.sdk.claims.ClaimsSetRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger LOG = LogManager.getLogger(AuthorizeHandler.class);

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
                LOG.info("Doc Checking App journey initialized");
                scopes.add("doc-checking-app");
                var opURL =
                        oidcClient.buildSecureAuthorizeRequest(
                                RelyingPartyConfig.authCallbackUrl(), Scope.parse(scopes));
                LOG.info("Redirecting to OP");
                response.redirect(opURL);
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
                LOG.info("Email scope requested");
                scopes.add(formParameters.get("scopes-email"));
            }

            if (formParameters.containsKey("scopes-phone")) {
                LOG.info("Phone scope requested");
                scopes.add(formParameters.get("scopes-phone"));
            }

            String vtr = formParameters.get("2fa");

            if (formParameters.containsKey("loc") && !formParameters.get("loc").isEmpty()) {
                vtr = "%s.%s".formatted(formParameters.get("loc"), vtr);
                LOG.info("VTR value selected: {}", vtr);
            }

            var claimsSetRequest = new ClaimsSetRequest();

            if (formParameters.containsKey("claims-core-identity")) {
                LOG.info("Core Identity claim requested");
                var identityEntry =
                        new ClaimsSetRequest.Entry(formParameters.get("claims-core-identity"))
                                .withClaimRequirement(ClaimRequirement.ESSENTIAL);
                claimsSetRequest = claimsSetRequest.add(identityEntry);
            }

            if (formParameters.containsKey("claims-passport")) {
                LOG.info("Passport claim requested");
                var passportEntry =
                        new ClaimsSetRequest.Entry(formParameters.get("claims-passport"))
                                .withClaimRequirement(ClaimRequirement.ESSENTIAL);
                claimsSetRequest = claimsSetRequest.add(passportEntry);
            }

            if (formParameters.containsKey("claims-address")) {
                LOG.info("Address claim requested");
                var addressEntry =
                        new ClaimsSetRequest.Entry(formParameters.get("claims-address"))
                                .withClaimRequirement(ClaimRequirement.ESSENTIAL);
                claimsSetRequest = claimsSetRequest.add(addressEntry);
            }

            var opURL =
                    oidcClient.buildAuthorizeRequest(
                            RelyingPartyConfig.authCallbackUrl(), vtr, scopes, claimsSetRequest);

            LOG.info("Redirecting to OP");
            response.redirect(opURL);
            return null;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
