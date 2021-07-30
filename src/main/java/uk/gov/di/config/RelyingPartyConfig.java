package uk.gov.di.config;

public class RelyingPartyConfig {
    public static final String SERVICE_NAME = getConfigValue("SERVICE_NAME", "Sample Government Service");
    public static final String IDP_URL= getConfigValue("OP_BASE_URL", "https://api.build.auth.ida.digital.cabinet-office.gov.uk");
    public static final String CLIENT_ID= getConfigValue("CLIENT_ID", "some_client_id");
    public static final String AUTH_CALLBACK_URL=getConfigValue("STUB_BASE_URL","http://localhost:8081") + "/oidc/authorization-code/callback";
    public static final String POST_LOGOUT_REDIRECT_URL=getConfigValue("STUB_BASE_URL","http://localhost:8081") + "/signed-out";
    public static final String CLIENT_PRIVATE_KEY =getConfigValue("CLIENT_PRIVATE_KEY","PRIVATE-KEY");
    public static final String PORT=getConfigValue("RP_PORT","8081");

    private static String getConfigValue(String key, String defaultValue){
        var envValue = System.getenv(key);
        if(envValue == null){
            return defaultValue;
        }

        return envValue;
    }
}
