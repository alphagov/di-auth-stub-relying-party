package uk.gov.di.handlers;

import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.utils.ViewHelper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import static uk.gov.di.config.RelyingPartyConfig.*;

public class OidcHandler {

    static Logger logger = LoggerFactory.getLogger(OidcHandler.class);

    public static Route doAuthorize = (Request request, Response response) -> {
        var authorizationRequest = new AuthorizationRequest.Builder(
                new ResponseType(ResponseType.Value.CODE), new ClientID(CLIENT_ID))
                .scope(new Scope("openid", "profile", "email"))
                .state(new State())
                .redirectionURI(new URI(AUTH_CALLBACK_URL))
                .endpointURI(new URI(OP_AUTHORIZE_URL))
                .build();

        response.redirect(authorizationRequest.toURI().toString());
        return null;
    };

    public static Route doAuthCallback = (Request request, Response response) -> {
        var accessToken = getToken(request.queryParams("code"));
        var userInfo = getUserInfo(accessToken);

        var model = new HashMap<>();
        model.put("email", userInfo.getEmailAddress());
        model.put("surname", userInfo.getFamilyName());
        model.put("forename", userInfo.getGivenName());

        return ViewHelper.render(model, "userinfo.mustache");
    };

    private static UserInfo getUserInfo(AccessToken accessToken) throws IOException, URISyntaxException, ParseException {
        var httpResponse = new UserInfoRequest(new URI(OP_USERINFO_URL), new BearerAccessToken(accessToken.toString()))
                .toHTTPRequest()
                .send();

        var userInfoResponse = UserInfoResponse.parse(httpResponse);

        if (! userInfoResponse.indicatesSuccess()) {
            logger.error("Userinfo request failed:" + userInfoResponse.toErrorResponse().getErrorObject().toString());
            throw new RuntimeException();
        }

        return userInfoResponse.toSuccessResponse().getUserInfo();
    }

    private static AccessToken getToken(String authcode) throws URISyntaxException, ParseException, IOException {
        var codeGrant = new AuthorizationCodeGrant(new AuthorizationCode(authcode), new URI(AUTH_CALLBACK_URL));

        var clientSecretPost = new ClientSecretPost(new ClientID(CLIENT_ID), new Secret(CLIENT_SECRET));
        var request = new TokenRequest(new URI(OP_TOKEN_URL), clientSecretPost, codeGrant, new Scope("openid"));
        var tokenResponse = OIDCTokenResponseParser.parse(request.toHTTPRequest().send());

        if (! tokenResponse.indicatesSuccess()) {
            logger.error("Token endpoint request failed:" + tokenResponse.toErrorResponse().getErrorObject().toString());
            throw new RuntimeException();
        }

        var successResponse = (OIDCTokenResponse)tokenResponse.toSuccessResponse();
        return successResponse.getOIDCTokens().getAccessToken();
    }
}
