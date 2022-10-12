package uk.gov.di.utils;

import uk.gov.di.config.RelyingPartyConfig;

public class CoreIdentityValidator {

    public enum Result {
        NOT_VALIDATED
    }

    public static CoreIdentityValidator createValidator() {
        return RelyingPartyConfig.identitySigningPublicKey()
                .map(CoreIdentityValidator::new)
                .orElse(new NoopCoreIdentityValidator());
    }

    private CoreIdentityValidator(Object jwt) {}

    public Result isValid(String jwt) {
        return Result.NOT_VALIDATED;
    }

    static class NoopCoreIdentityValidator extends CoreIdentityValidator {
        private NoopCoreIdentityValidator() {
            super(null);
        }

        @Override
        public Result isValid(String jwt) {
            return Result.NOT_VALIDATED;
        }
    }
}
