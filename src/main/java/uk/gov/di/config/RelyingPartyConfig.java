package uk.gov.di.config;

import io.pivotal.cfenv.core.CfEnv;

import static java.text.MessageFormat.format;

public class RelyingPartyConfig {

    private static final CfEnv cfEnv = new CfEnv();

    public static String authCallbackUrl() {
        return getCloudFoundryUri() + "/oidc/authorization-code/callback";
    }

    public static String postLogoutRedirectUrl() {
        return getCloudFoundryUri() + "/signed-out";
    }

    public static String clientPrivateKey() {
        return getConfigValue("CLIENT_PRIVATE_KEY", "PRIVATE-KEY");
    }

    public static String accountManagementUrl() {
        return getConfigValue(
                "MY_ACCOUNT_URL",
                "https://account-management.integration.auth.ida.digital.cabinet-office.gov.uk/");
    }

    public static String clientId() {
        return getConfigValue("CLIENT_ID", "some_client_id");
    }

    public static String serviceName() {
        return getConfigValue("SERVICE_NAME", "Sample Government Service");
    }

    public static String oidcProviderUrl() {
        return getConfigValue(
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

    private static String getConfigValue(String key, String defaultValue) {
        var envValue = System.getenv(key);
        if (envValue == null) {
            return defaultValue;
        }

        return envValue;
    }
}
