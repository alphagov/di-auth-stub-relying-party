package uk.gov.di.config;

import java.net.URI;
import java.util.Optional;

public class RelyingPartyConfig {
    public static final String SERVICE_NAME = getConfigValue("SERVICE_NAME", "Sample Government Service");
    public static final String CLIENT_ID = getConfigValue("CLIENT_ID", "some_client_id");
    public static final String CLIENT_SECRET = getConfigValue("CLIENT_SECRET", "password");
    public static final String OP_AUTHORIZE_URL = getConfigValue("OP_BASE_URL", "http://localhost:8080") + "/authorize";
    public static final String OP_TOKEN_URL = getConfigValue("OP_BASE_URL", "http://localhost:8080") + "/token";
    public static final String OP_USERINFO_URL = getConfigValue("OP_BASE_URL", "http://localhost:8080") + "/userinfo";
    public static final String AUTH_CALLBACK_URL = getConfigValue("STUB_BASE_URL", "http://localhost:8081") + "/oidc/callback";
    public static final String LOGOUT_URL = generateLogoutUrl("OP_BASE_URL", "STUB_BASE_URL", "http://localhost:8080/logout?redirectUri=http://localhost:8081/");
    public static final String PORT = getConfigValue("RP_PORT", "8081");


    private static String getConfigValue(String key, String defaultValue) {
        var envValue = System.getenv(key);
        if (envValue == null) {
            return defaultValue;
        }

        return envValue;
    }

    private static String generateLogoutUrl(String baseUrlKey, String stubBaseUrlKey, String defaultValue) {
        var baseUrlEnv = System.getenv(baseUrlKey);
        var stubBaseUrlEnv = System.getenv(stubBaseUrlKey);

        if (baseUrlEnv == null || stubBaseUrlEnv == null) {
            return defaultValue;
        }
        return baseUrlEnv + "/logout?redirectUri=" + stubBaseUrlEnv;
    }
}
