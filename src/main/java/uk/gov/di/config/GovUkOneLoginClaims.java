package uk.gov.di.config;

import com.nimbusds.openid.connect.sdk.claims.ClaimRequirement;
import com.nimbusds.openid.connect.sdk.claims.ClaimsSetRequest.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum GovUkOneLoginClaims {
    CORE_IDENTITY("https://vocab.account.gov.uk/v1/coreIdentityJWT"),
    PASSPORT("https://vocab.account.gov.uk/v1/passport"),
    ADDRESS("https://vocab.account.gov.uk/v1/address"),
    DRIVING_PERMIT("https://vocab.account.gov.uk/v1/drivingPermit"),
    SOCIAL_SECURITY_RECORD("https://vocab.account.gov.uk/v1/socialSecurityRecord"),
    RETURN_CODE("https://vocab.account.gov.uk/v1/returnCode"),
    INHERITED_IDENTITY("https://vocab.account.gov.uk/v1/inheritedIdentityJWT");

    private static final Logger LOG = LoggerFactory.getLogger(GovUkOneLoginClaims.class);

    private final String claim;

    GovUkOneLoginClaims(String claim) {
        this.claim = claim;
    }

    public Entry asEntry() {
        LOG.info("{} claim requested", name());
        return new Entry(this.claim).withClaimRequirement(ClaimRequirement.ESSENTIAL);
    }
}
