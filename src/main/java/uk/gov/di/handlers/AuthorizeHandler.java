package uk.gov.di.handlers;

import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.claims.ClaimRequirement;
import com.nimbusds.openid.connect.sdk.claims.ClaimsSetRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.config.RelyingPartyConfig;
import uk.gov.di.utils.Oidc;
import uk.gov.di.utils.ViewHelper;

import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthorizeHandler implements Route {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizeHandler.class);

    private final Oidc oidcClient;

    public AuthorizeHandler(Oidc oidc) {
        this.oidcClient = oidc;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            List<String> scopes = new ArrayList<>();
            scopes.add("openid");

            List<NameValuePair> pairs =
                    URLEncodedUtils.parse(request.body(), Charset.defaultCharset());

            Map<String, String> formParameters =
                    pairs.stream()
                            .collect(
                                    Collectors.toMap(
                                            NameValuePair::getName, NameValuePair::getValue));

            String language = formParameters.get("lng");

            if (RelyingPartyConfig.clientType().equals("app")) {
                LOG.info("Doc Checking App journey initialized");
                scopes.add("doc-checking-app");
                var opURL =
                        oidcClient.buildDocAppAuthorizeRequest(
                                RelyingPartyConfig.authCallbackUrl(),
                                Scope.parse(scopes),
                                language);
                LOG.info("Redirecting to OP");
                response.redirect(opURL);
                return null;
            }

            if (formParameters.containsKey("scopes-email")) {
                LOG.info("Email scope requested");
                scopes.add(formParameters.get("scopes-email"));
            }

            if (formParameters.containsKey("scopes-phone")) {
                LOG.info("Phone scope requested");
                scopes.add(formParameters.get("scopes-phone"));
            }

            String vtr = formParameters.get("2fa");

            var prompt = formParameters.get("prompt");

            String rpSid = formParameters.get("rp-sid");

            String idToken = formParameters.get("reauth-id-token");

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

            if (formParameters.containsKey("claims-driving-permit")) {
                LOG.info("Driving permit claim requested");
                var drivingPermitEntry =
                        new ClaimsSetRequest.Entry(formParameters.get("claims-driving-permit"))
                                .withClaimRequirement(ClaimRequirement.ESSENTIAL);
                claimsSetRequest = claimsSetRequest.add(drivingPermitEntry);
            }

            if (formParameters.containsKey("claims-social-security-record")) {
                LOG.info("Social security record claim requested");
                var socialSecurityRecordEntry =
                        new ClaimsSetRequest.Entry(
                                        formParameters.get("claims-social-security-record"))
                                .withClaimRequirement(ClaimRequirement.ESSENTIAL);
                claimsSetRequest = claimsSetRequest.add(socialSecurityRecordEntry);
            }

            if (formParameters.containsKey("claims-return-code")) {
                LOG.info("Return code claim requested");
                var returnCodeEntry =
                        new ClaimsSetRequest.Entry(formParameters.get("claims-return-code"))
                                .withClaimRequirement(ClaimRequirement.ESSENTIAL);
                claimsSetRequest = claimsSetRequest.add(returnCodeEntry);
            }

            if (formParameters.containsKey("claims-inherited-identity")) {
                if (!formParameters.get("claims-inherited-identity").trim().isEmpty()) {
                    LOG.info("Inherited Identity record claim requested");
                    var inheritedIdentityEntry =
                            new ClaimsSetRequest.Entry(
                                            "https://vocab.account.gov.uk/v1/inheritedIdentityJWT")
                                    .withValues(
                                            List.of(
                                                    formParameters.get(
                                                            "claims-inherited-identity")));
                    claimsSetRequest = claimsSetRequest.add(inheritedIdentityEntry);
                } else {
                    claimsSetRequest.delete("claims-inherited-identity");
                }
            }

            var authRequest =
                    buildAuthorizeRequest(
                            formParameters,
                            vtr,
                            scopes,
                            claimsSetRequest,
                            language,
                            prompt,
                            rpSid,
                            idToken);

            if (formParameters.containsKey("method")
                    && formParameters.get("method").equals("post")) {
                var model = new HashMap<>();
                model.put("servicename", RelyingPartyConfig.serviceName());
                model.put("endpoint_address", oidcClient.getAuthorizationEndpoint());
                authRequest
                        .toParameters()
                        .forEach(
                                (key, value) ->
                                        model.putIfAbsent(
                                                key,
                                                value.stream().count() > 1 ? value : value.get(0)));
                return ViewHelper.render(model, "post-page.mustache");
            }

            LOG.info("Redirecting to OP");
            response.redirect(authRequest.toURI().toString());
            return null;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private AuthenticationRequest buildAuthorizeRequest(
            Map<String, String> formParameters,
            String vtr,
            List<String> scopes,
            ClaimsSetRequest claimsSetRequest,
            String language,
            String prompt,
            String rpSid,
            String idToken)
            throws URISyntaxException {
        if ("object".equals(formParameters.getOrDefault("request", "query"))) {
            LOG.info("Building authorize request with JAR");
            return oidcClient.buildJarAuthorizeRequest(
                    RelyingPartyConfig.authCallbackUrl(),
                    vtr,
                    scopes,
                    claimsSetRequest,
                    language,
                    prompt,
                    rpSid,
                    idToken);
        } else {
            LOG.info("Building authorize request with query params");
            return oidcClient.buildQueryParamAuthorizeRequest(
                    RelyingPartyConfig.authCallbackUrl(),
                    vtr,
                    scopes,
                    claimsSetRequest,
                    language,
                    prompt,
                    rpSid);
        }
    }
}
