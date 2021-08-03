package uk.gov.di.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.PrivateKeyJWT;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.singletonList;

public class Oidc {

    public static final String WELL_KNOWN_OPENID_CONFIGURATION = "/.well-known/openid-configuration";

    private final OIDCProviderMetadata providerMetadata;
    private final String idpUrl;
    private final String clientId;
    private final PrivateKeyReader privateKeyReader;

    public Oidc(String baseUrl, String clientId, PrivateKeyReader privateKeyReader) {
        this.idpUrl = baseUrl;
        this.clientId = clientId;
        this.providerMetadata = loadProviderMetadata();
        this.privateKeyReader = privateKeyReader;
    }

    private OIDCProviderMetadata loadProviderMetadata() {
        try {
            var base = new URL(this.idpUrl);
            var providerConfigurationURL = new URL(base + WELL_KNOWN_OPENID_CONFIGURATION);
            var stream = providerConfigurationURL.openStream();

            String providerInfo;
            try (java.util.Scanner s = new java.util.Scanner(stream)) {
                providerInfo = s.useDelimiter("\\A").hasNext() ? s.next() : "";
            }

            return OIDCProviderMetadata.parse(providerInfo);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public UserInfo makeUserInfoRequest(AccessToken accessToken) throws IOException, ParseException {
        var httpResponse = new UserInfoRequest(this.providerMetadata.getUserInfoEndpointURI(), new BearerAccessToken(accessToken.toString()))
                .toHTTPRequest()
                .send();

        var userInfoResponse = UserInfoResponse.parse(httpResponse);

        if (!userInfoResponse.indicatesSuccess()) {
            throw new RuntimeException(userInfoResponse.toErrorResponse().toString());
        }

        return userInfoResponse.toSuccessResponse().getUserInfo();
    }

    public OIDCTokens makeTokenRequest(String authCode, String authCallbackUrl) throws URISyntaxException {
        var codeGrant = new AuthorizationCodeGrant(new AuthorizationCode(authCode), new URI(authCallbackUrl));

        try {
            var privateKeyJWT =
                    new PrivateKeyJWT(
                            new ClientID(this.clientId),
                            URI.create(this.providerMetadata.getTokenEndpointURI().toString()),
                            JWSAlgorithm.RS512,
                            privateKeyReader.get(),
                            null,
                            null);

            var extraParams = new HashMap<String, List<String>>();
            extraParams.put("client_id", singletonList(this.clientId));

            var request = new TokenRequest(this.providerMetadata.getTokenEndpointURI(), privateKeyJWT, codeGrant, null, null, extraParams);

            var tokenResponse = OIDCTokenResponseParser.parse(request.toHTTPRequest().send());

            if (!tokenResponse.indicatesSuccess()) {
                throw new RuntimeException(tokenResponse.toErrorResponse().getErrorObject().toString());
            }

            return tokenResponse.toSuccessResponse().getTokens().toOIDCTokens();

        } catch (JOSEException | ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String buildAuthorizeRequest(String callbackUrl) throws URISyntaxException {
        var authorizationRequest = new AuthorizationRequest.Builder(
                new ResponseType(ResponseType.Value.CODE), new ClientID(this.clientId))
                .scope(new Scope("openid", "phone", "email"))
                .state(new State())
                .customParameter("nonce", generateNonce())
                .redirectionURI(new URI(callbackUrl))
                .endpointURI(this.providerMetadata.getAuthorizationEndpointURI())
                .build();

        return authorizationRequest.toURI().toString();
    }

    public String buildLogoutUrl(String idToken, String state, String postLogoutRedirectUri) throws URISyntaxException {
        var logoutUri = new URIBuilder(this.idpUrl + "/logout");
        logoutUri.addParameter("id_token_hint", idToken);
        logoutUri.addParameter("state", state);
        logoutUri.addParameter("post_logout_redirect_uri", postLogoutRedirectUri);

        return logoutUri.build().toString();
    }

    public void validateIdToken(JWT idToken) throws MalformedURLException {
        var iss = new Issuer(this.providerMetadata.getIssuer());
        var clientID = new ClientID(this.clientId);
        var jwsAlg = JWSAlgorithm.RS512;

        var idTokenValidator = new IDTokenValidator(iss, clientID, jwsAlg, this.providerMetadata.getJWKSetURI().toURL());

        try {
            idTokenValidator.validate(idToken, null);
        } catch (BadJOSEException | JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateNonce() {
        var random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
