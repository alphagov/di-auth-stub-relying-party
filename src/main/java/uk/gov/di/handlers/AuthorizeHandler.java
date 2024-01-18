package uk.gov.di.handlers;

import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.claims.ClaimsSetRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.config.GovUkOneLoginClaims;
import uk.gov.di.config.RelyingPartyConfig;
import uk.gov.di.utils.Oidc;
import uk.gov.di.utils.ViewHelper;

import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

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

            final var claimsSetRequest = new ClaimsSetRequest();

            if (formParameters.containsKey("claims-core-identity")) {
                claimsSetRequest.add(GovUkOneLoginClaims.CORE_IDENTITY.asEntry());
            }

            if (formParameters.containsKey("claims-passport")) {
                claimsSetRequest.add(GovUkOneLoginClaims.PASSPORT.asEntry());
            }

            if (formParameters.containsKey("claims-address")) {
                claimsSetRequest.add(GovUkOneLoginClaims.ADDRESS.asEntry());
            }

            if (formParameters.containsKey("claims-driving-permit")) {
                claimsSetRequest.add(GovUkOneLoginClaims.DRIVING_PERMIT.asEntry());
            }

            if (formParameters.containsKey("claims-social-security-record")) {
                claimsSetRequest.add(GovUkOneLoginClaims.SOCIAL_SECURITY_RECORD.asEntry());
            }

            if (formParameters.containsKey("claims-return-code")) {
                claimsSetRequest.add(GovUkOneLoginClaims.RETURN_CODE.asEntry());
            }

            Optional.ofNullable(formParameters.get("claims-inherited-identity"))
                    .map(String::trim)
                    .filter(not(String::isEmpty))
                    .map(Collections::singletonList)
                    .map(GovUkOneLoginClaims.INHERITED_IDENTITY.asEntry()::withValues)
                    .ifPresent(claimsSetRequest::add);

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
