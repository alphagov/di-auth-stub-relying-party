package uk.gov.di.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.config.RelyingPartyConfig;
import uk.gov.di.utils.CoreIdentityValidator;
import uk.gov.di.utils.Oidc;
import uk.gov.di.utils.ViewHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AuthCallbackHandler implements Route {

    private static final Logger LOG = LoggerFactory.getLogger(AuthCallbackHandler.class);

    private final Oidc oidcClient;
    private final CoreIdentityValidator validator;

    public AuthCallbackHandler(Oidc oidc, CoreIdentityValidator validator) {
        this.oidcClient = oidc;
        this.validator = validator;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        LOG.info("Callback received");
        var tokens =
                oidcClient.makeTokenRequest(
                        request.queryParams("code"), RelyingPartyConfig.authCallbackUrl());
        oidcClient.validateIdToken(tokens.getIDToken());
        request.session().attribute("idToken", tokens.getIDToken().getParsedString());

        var userInfo = oidcClient.makeUserInfoRequest(tokens.getAccessToken());

        var model = new HashMap<>();
        var templateName = "userinfo.mustache";
        if (RelyingPartyConfig.clientType().equals("app")) {
            List<String> docAppCredential = (List<String>) userInfo.getClaim("doc-app-credential");
            model.put("doc_app_credential", docAppCredential.get(0));
            templateName = "doc-app-userinfo.mustache";
        } else {
            model.put("email", userInfo.getEmailAddress());
            model.put("phone_number", userInfo.getPhoneNumber());

            var coreIdentityJWT =
                    userInfo.getStringClaim("https://vocab.account.gov.uk/v1/coreIdentityJWT");
            boolean coreIdentityClaimPresent = Objects.nonNull(coreIdentityJWT);
            model.put("core_identity_claim_present", coreIdentityClaimPresent);
            model.put("core_identity_claim", coreIdentityJWT);

            if (coreIdentityClaimPresent) {
                model.put("core_identity_claim_signature", validator.isValid(coreIdentityJWT));
            }

            boolean addressClaimPresent =
                    Objects.nonNull(userInfo.getClaim("https://vocab.account.gov.uk/v1/address"));
            boolean passportClaimPresent =
                    Objects.nonNull(userInfo.getClaim("https://vocab.account.gov.uk/v1/passport"));
            boolean drivingPermitClaimPresent =
                    Objects.nonNull(userInfo.getClaim("https://vocab.account.gov.uk/v1/drivingPermit"));
            model.put("address_claim_present", addressClaimPresent);
            model.put("passport_claim_present", passportClaimPresent);
            model.put("driving_permit_claim_present", drivingPermitClaimPresent);
        }
        model.put("my_account_url", RelyingPartyConfig.accountManagementUrl());
        model.put("id_token", tokens.getIDToken().getParsedString());

        return ViewHelper.render(model, templateName);
    }
}
