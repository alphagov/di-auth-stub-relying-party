package uk.gov.di.handlers;

import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.openid.connect.sdk.claims.ClaimsSet;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.utils.Oidc;

import java.text.ParseException;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.stream.Collectors.toMap;

public class BackChannelLogoutHandler implements Route {

    private final Oidc oidcClient;

    public BackChannelLogoutHandler(Oidc oidcClient) {
        this.oidcClient = oidcClient;
    }

    @Override
    public Object handle(Request request, Response response) {

        var payload =
                URLEncodedUtils.parse(request.body(), defaultCharset()).stream()
                        .collect(toMap(NameValuePair::getName, NameValuePair::getValue))
                        .getOrDefault("logout_token", "");

        try {
            var jwt = SignedJWT.parse(payload);

            oidcClient
                    .validateLogoutToken(jwt)
                    .map(ClaimsSet::toJSONString)
                    .ifPresentOrElse(
                            claims -> {
                                System.out.println("Validated logout token. Claims: " + claims);
                                response.status(200);
                            },
                            () -> {
                                System.out.println("Unable to validate logout token");
                                response.status(400);
                            });

        } catch (ParseException e) {
            e.printStackTrace();
            response.status(500);
        }

        return "";
    }
}
