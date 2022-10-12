package uk.gov.di.config;

import io.pivotal.cfenv.core.CfEnv;

import java.util.Optional;

import static java.lang.System.getenv;
import static java.text.MessageFormat.format;
import static java.util.function.Predicate.not;

public class RelyingPartyConfig {

    private static final CfEnv cfEnv = new CfEnv();

    public static String authCallbackUrl() {
        return getCloudFoundryUri() + "/oidc/authorization-code/callback";
    }

    public static String postLogoutRedirectUrl() {
        return getCloudFoundryUri() + "/signed-out";
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
                "https://account-management.integration.auth.ida.digital.cabinet-office.gov.uk/");
    }

    public static String clientId() {
        return configValue("CLIENT_ID", "some_client_id");
    }

    public static String clientType() {
        return configValue("CLIENT_TYPE", "web");
    }

    public static String serviceName() {
        return configValue("SERVICE_NAME", "Sample Government Service");
    }

    public static String oidcProviderUrl() {
        return configValue(
                "OP_BASE_URL", "https://api.build.auth.ida.digital.cabinet-office.gov.uk");
    }

    public static String getCloudFoundryUri() {
        if (cfEnv.isInCf() && cfEnv.getApp().getUris().size() > 0) {
            return format("https://{0}", cfEnv.getApp().getUris().get(0));
        }
        return "http://localhost:8081";
    }

    public static int getCloudfoundryPort() {
        if (cfEnv.isInCf()) {
            return 8080;
        }
        return 8081;
    }

    private static String configValue(String key, String defaultValue) {
        return getenv().getOrDefault(key, defaultValue);
    }
}
