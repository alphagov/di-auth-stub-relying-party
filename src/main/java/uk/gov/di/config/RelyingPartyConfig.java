package uk.gov.di.config;

import io.pivotal.cfenv.core.CfEnv;

import static java.text.MessageFormat.format;

public class RelyingPartyConfig {

    private static final CfEnv cfEnv = new CfEnv();

    public static final String SERVICE_NAME = getConfigValue("SERVICE_NAME", "Sample Government Service");
    public static final String IDP_URL= getConfigValue("OP_BASE_URL", "https://api.build.auth.ida.digital.cabinet-office.gov.uk");
    public static final String CLIENT_ID= getConfigValue("CLIENT_ID", "some_client_id");
    public static final String AUTH_CALLBACK_URL=getCloudFoundryUri("http://localhost:8081") + "/oidc/authorization-code/callback";
    public static final String POST_LOGOUT_REDIRECT_URL=getCloudFoundryUri("http://localhost:8081") + "/signed-out";
    public static final String CLIENT_PRIVATE_KEY =getConfigValue("CLIENT_PRIVATE_KEY","PRIVATE-KEY");
    public static final int PORT=getCloudfoundryPort(8081);
    public static final String MY_ACCOUNT_URL = getConfigValue("MY_ACCOUNT_URL", "https://account-management.integration.auth.ida.digital.cabinet-office.gov.uk/");

    private static String getConfigValue(String key, String defaultValue){
        var envValue = System.getenv(key);
        if(envValue == null){
            return defaultValue;
        }

        return envValue;
    }

    private static String getCloudFoundryUri(String defaultValue) {
        if (cfEnv.isInCf() && cfEnv.getApp().getUris().size() > 0) {
            return format("https://{0}", cfEnv.getApp().getUris().get(0));
        }
        return defaultValue;
    }

    private static int getCloudfoundryPort(int defaultValue) {
        if (cfEnv.isInCf()) {
            return 8080;
        }
        return defaultValue;
    }
}
