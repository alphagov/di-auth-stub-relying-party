package uk.gov.di.utils;

public class CoreIdentityValidator {

    public enum Result {
        NOT_VALIDATED
    }

    public static CoreIdentityValidator createValidator() {
        return new CoreIdentityValidator();
    }

    public Result isValid(String jwt) {
        return Result.NOT_VALIDATED;
    }
}
