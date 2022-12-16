package uk.gov.di.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.langtag.LangTag;
import com.nimbusds.langtag.LangTagException;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.JWTAuthenticationClaimsSet;
import com.nimbusds.oauth2.sdk.auth.PrivateKeyJWT;
import com.nimbusds.oauth2.sdk.id.Audience;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCClaimsRequest;
import com.nimbusds.openid.connect.sdk.OIDCScopeValue;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.Prompt;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.ClaimsSetRequest;
import com.nimbusds.openid.connect.sdk.claims.LogoutTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import com.nimbusds.openid.connect.sdk.validators.LogoutTokenValidator;
import net.minidev.json.JSONArray;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.config.RelyingPartyConfig;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class Oidc {

    private static final Logger LOG = LoggerFactory.getLogger(Oidc.class);

    private final OIDCProviderMetadata providerMetadata;
    private final String idpUrl;
    private final ClientID clientId;
    private final PrivateKeyReader privateKeyReader;

    public Oidc(String baseUrl, String clientId, PrivateKeyReader privateKeyReader) {
        this.idpUrl = baseUrl;
        this.clientId = new ClientID(clientId);
        this.providerMetadata = loadProviderMetadata(baseUrl);
        this.privateKeyReader = privateKeyReader;
    }

    private OIDCProviderMetadata loadProviderMetadata(String baseUrl) {
        try {
            return OIDCProviderMetadata.resolve(new Issuer(baseUrl));
        } catch (Exception e) {
            LOG.error("Unexpected exception thrown when loading provider metadata", e);
            throw new RuntimeException(e);
        }
    }

    public UserInfo makeUserInfoRequest(AccessToken accessToken)
            throws IOException, ParseException {
        LOG.info("Making userinfo request");
        var httpResponse =
                new UserInfoRequest(
                                this.providerMetadata.getUserInfoEndpointURI(),
                                new BearerAccessToken(accessToken.toString()))
                        .toHTTPRequest()
                        .send();

        var userInfoResponse = UserInfoResponse.parse(httpResponse);

        if (!userInfoResponse.indicatesSuccess()) {
            LOG.error("Userinfo request was unsuccessful");
            throw new RuntimeException(userInfoResponse.toErrorResponse().toString());
        }

        LOG.info("Userinfo request was successful");

        return userInfoResponse.toSuccessResponse().getUserInfo();
    }

    public OIDCTokens makeTokenRequest(String authCode, String authCallbackUrl)
            throws URISyntaxException {
        LOG.info("Making Token Request");
        var codeGrant =
                new AuthorizationCodeGrant(
                        new AuthorizationCode(authCode), new URI(authCallbackUrl));

        try {
            LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(5);
            Date expiryDate = Date.from(localDateTime.atZone(ZoneId.of("UTC")).toInstant());
            JWTAuthenticationClaimsSet claimsSet =
                    new JWTAuthenticationClaimsSet(
                            this.clientId,
                            new Audience(this.providerMetadata.getTokenEndpointURI().toString()));
            claimsSet.getExpirationTime().setTime(expiryDate.getTime());

            var request =
                    new TokenRequest(
                            this.providerMetadata.getTokenEndpointURI(),
                            new PrivateKeyJWT(signJwtWithClaims(claimsSet.toJWTClaimsSet())),
                            codeGrant,
                            null,
                            null,
                            null);

            var tokenResponse = OIDCTokenResponseParser.parse(request.toHTTPRequest().send());

            if (!tokenResponse.indicatesSuccess()) {
                LOG.error("TokenRequest was unsuccessful");
                throw new RuntimeException(
                        tokenResponse.toErrorResponse().getErrorObject().toString());
            }

            LOG.info("TokenRequest was successful");

            Optional.of(tokenResponse)
                    .map(TokenResponse::toSuccessResponse)
                    .map(AccessTokenResponse::getTokens)
                    .map(Tokens::getAccessToken)
                    .map(AccessToken::getLifetime)
                    .ifPresentOrElse(
                            lifetime -> LOG.info("Access token expires in {}", lifetime),
                            () -> LOG.warn("No expiry on access token"));

            return tokenResponse.toSuccessResponse().getTokens().toOIDCTokens();

        } catch (ParseException | IOException e) {
            LOG.error("Unexpected exception thrown when making token request", e);
            throw new RuntimeException(e);
        }
    }

    public String buildAuthorizeRequest(
            String callbackUrl,
            String vtr,
            List<String> scopes,
            ClaimsSetRequest claimsSetRequest,
            String language,
            String prompt)
            throws URISyntaxException {
        LOG.info("Building Authorize Request");
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(vtr);
        Prompt authRequestPrompt;
        try {
            authRequestPrompt = Prompt.parse(prompt);
        } catch (ParseException e) {
            throw new RuntimeException("Unable to parse prompt", e);
        }

        var authorizationRequestBuilder =
                new AuthenticationRequest.Builder(
                                new ResponseType(ResponseType.Value.CODE),
                                Scope.parse(scopes),
                                this.clientId,
                                new URI(callbackUrl))
                        .state(new State())
                        .nonce(new Nonce())
                        .prompt(authRequestPrompt)
                        .endpointURI(this.providerMetadata.getAuthorizationEndpointURI())
                        .customParameter("vtr", jsonArray.toJSONString());

        if (claimsSetRequest.getEntries().size() > 0) {
            LOG.info("Adding claims to Authorize Request");
            authorizationRequestBuilder.claims(
                    new OIDCClaimsRequest().withUserInfoClaimsRequest(claimsSetRequest));
        }

        if (!language.isBlank()) {
            try {
                LOG.info("Adding ui_locales to Authorize Request {}", language);
                authorizationRequestBuilder.uiLocales(List.of(LangTag.parse(language)));
            } catch (LangTagException e) {
                LOG.error("Unable to parse language {}", language);
            }
        }

        return authorizationRequestBuilder.build().toURI().toString();
    }

    public String buildSecureAuthorizeRequest(String callbackUrl, Scope scopes, String language) {
        LOG.info("Building secure Authorize Request");
        var authRequestBuilder =
                new AuthorizationRequest.Builder(
                                new ResponseType(ResponseType.Value.CODE), this.clientId)
                        .requestObject(generateSignedJWT(scopes, callbackUrl, language))
                        .scope(new Scope(OIDCScopeValue.OPENID))
                        .endpointURI(this.providerMetadata.getAuthorizationEndpointURI());

        return authRequestBuilder.build().toURI().toString();
    }

    public String buildLogoutUrl(String idToken, String state, String postLogoutRedirectUri)
            throws URISyntaxException {
        var logoutUri = new URIBuilder(this.idpUrl + "/logout");
        logoutUri.addParameter("id_token_hint", idToken);
        logoutUri.addParameter("state", state);
        logoutUri.addParameter("post_logout_redirect_uri", postLogoutRedirectUri);

        return logoutUri.build().toString();
    }

    public void validateIdToken(JWT idToken) throws MalformedURLException {
        LOG.info("Validating ID token");
        ResourceRetriever resourceRetriever = new DefaultResourceRetriever(30000, 30000);
        var idTokenValidator =
                new IDTokenValidator(
                        this.providerMetadata.getIssuer(),
                        this.clientId,
                        RelyingPartyConfig.idTokenSigningAlgorithm(),
                        this.providerMetadata.getJWKSetURI().toURL(),
                        resourceRetriever);

        try {
            idTokenValidator.validate(idToken, null);
        } catch (BadJOSEException | JOSEException e) {
            LOG.error("Unexpected exception thrown when validating ID token", e);
            throw new RuntimeException(e);
        }
    }

    public Optional<LogoutTokenClaimsSet> validateLogoutToken(JWT logoutToken) {
        try {
            var validator =
                    new LogoutTokenValidator(
                            this.providerMetadata.getIssuer(),
                            this.clientId,
                            RelyingPartyConfig.idTokenSigningAlgorithm(),
                            this.providerMetadata.getJWKSetURI().toURL(),
                            new DefaultResourceRetriever(30000, 30000));

            return Optional.of(validator.validate(logoutToken));
        } catch (BadJOSEException | JOSEException | MalformedURLException e) {
            LOG.error("Unexpected exception thrown when validating logout token", e);
            return Optional.empty();
        }
    }

    private SignedJWT generateSignedJWT(Scope scopes, String callbackURL, String language) {
        var jwtClaimsSet =
                new JWTClaimsSet.Builder()
                        .audience(this.providerMetadata.getAuthorizationEndpointURI().toString())
                        .claim("redirect_uri", callbackURL)
                        .claim("response_type", ResponseType.CODE.toString())
                        .claim("scope", scopes.toString())
                        .claim("nonce", new Nonce().getValue())
                        .claim("client_id", this.clientId)
                        .claim("state", new State().getValue())
                        .claim("ui_locales", language)
                        .issuer(this.clientId.getValue())
                        .build();
        return signJwtWithClaims(jwtClaimsSet);
    }

    private SignedJWT signJwtWithClaims(JWTClaimsSet jwtClaimsSet) {
        var signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS512), jwtClaimsSet);

        try {
            signedJWT.sign(new RSASSASigner(this.privateKeyReader.get()));
        } catch (JOSEException e) {
            LOG.error("Unable to sign secure request object", e);
            throw new RuntimeException("Unable to sign secure request object", e);
        }

        return signedJWT;
    }
}
