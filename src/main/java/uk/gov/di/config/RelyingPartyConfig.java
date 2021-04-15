package uk.gov.di.config;

public class RelyingPartyConfig {
    public static final String CLIENT_ID= getConfigValue("CLIENT_ID", "dummy-rp");
    public static final String CLIENT_SECRET = getConfigValue("CLIENT_SECRET", "password");
    public static final String OP_AUTHORIZE_URL = getConfigValue("OP_AUTHORIZE_URL","http://localhost:8080/authorize");
    public static final String OP_TOKEN_URL=getConfigValue("OP_TOKEN_URL","http://localhost:8080/token");
    public static final String OP_USERINFO_URL=getConfigValue("OP_USERINFO_URL","http://localhost:8080/userinfo");
    public static final String AUTH_CALLBACK_URL=getConfigValue("AUTH_CALLBACK_URL","http://localhost:8081/oidc/callback");
    public static final String PORT=getConfigValue("RP_PORT","8081");

    private static String getConfigValue(String key, String defaultValue){
        var envValue = System.getenv(key);
        if(envValue == null){
            return defaultValue;
        }

        return envValue;
    }
}
