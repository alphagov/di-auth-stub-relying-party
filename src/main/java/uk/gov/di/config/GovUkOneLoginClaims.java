package uk.gov.di.config;

import com.nimbusds.openid.connect.sdk.claims.ClaimRequirement;
import com.nimbusds.openid.connect.sdk.claims.ClaimsSetRequest;

public enum GovUkOneLoginClaims {
    CORE_IDENTITY("https://vocab.account.gov.uk/v1/coreIdentityJWT"),
    PASSPORT("https://vocab.account.gov.uk/v1/passport"),
    ADDRESS("https://vocab.account.gov.uk/v1/address"),
    DRIVING_PERMIT("https://vocab.account.gov.uk/v1/drivingPermit"),
    SOCIAL_SECURITY_RECORD("https://vocab.account.gov.uk/v1/socialSecurityRecord");

    private final String claim;

    GovUkOneLoginClaims(String claim) {
        this.claim = claim;
    }

    public ClaimsSetRequest.Entry asEntry() {
        return new ClaimsSetRequest.Entry(this.claim).withClaimRequirement(ClaimRequirement.ESSENTIAL);
    }
}
