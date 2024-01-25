package uk.gov.di.config;

import com.nimbusds.jose.JWSAlgorithm;

import java.util.Optional;

import static java.lang.System.getenv;
import static java.util.function.Predicate.not;

public class RelyingPartyConfig {

    public static String authCallbackUrl() {
        return getStubUri() + "/oidc/authorization-code/callback";
    }

    public static String postLogoutRedirectUrl() {
        return getStubUri() + "/signed-out";
    }

    public static String clientPrivateKey() {
        return configValue("CLIENT_PRIVATE_KEY", "PRIVATE-KEY");
    }

    public static Optional<String> identitySigningPublicKey() {
        return Optional.of(getenv())
                .map(env -> env.get("IDENTITY_SIGNING_PUBLIC_KEY"))
                .filter(not(String::isBlank));
    }

    public static String accountManagementUrl() {
        return configValue(
                "MY_ACCOUNT_URL",
                "https://home.build.account.gov.uk/");
    }

    public static String clientId() {
        return configValue("CLIENT_ID", "some_client_id");
    }

    public static String clientType() {
        return configValue("CLIENT_TYPE", "web");
    }

    public static JWSAlgorithm idTokenSigningAlgorithm() {
        return JWSAlgorithm.parse(configValue("ID_TOKEN_SIGNING_ALGORITHM", "ES256"));
    }

    public static String serviceName() {
        return configValue("SERVICE_NAME", "Sample Government Service");
    }

    public static String oidcProviderUrl() {
        return configValue("OP_BASE_URL", "https://oidc.build.account.gov.uk");
    }

    public static String getStubUri() {
        return getenv().getOrDefault("STUB_URL", "http://localhost:8080");
    }

    private static String configValue(String key, String defaultValue) {
        return getenv().getOrDefault(key, defaultValue);
    }
}
