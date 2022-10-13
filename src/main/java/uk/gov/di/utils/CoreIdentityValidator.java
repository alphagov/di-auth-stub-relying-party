package uk.gov.di.utils;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jwt.SignedJWT;
import uk.gov.di.config.RelyingPartyConfig;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.Optional;

public class CoreIdentityValidator {

    private final ECKey key;

    public enum Result {
        VALID,
        INVALID,
        NOT_VALIDATED
    }

    public static CoreIdentityValidator createValidator() {
        return RelyingPartyConfig.identitySigningPublicKey()
                .map(Base64.getDecoder()::decode)
                .map(X509EncodedKeySpec::new)
                .flatMap(
                        spec -> {
                            try {
                                return Optional.ofNullable(
                                        KeyFactory.getInstance("EC").generatePublic(spec));
                            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                                e.printStackTrace();
                                return Optional.empty();
                            }
                        })
                .map(ECPublicKey.class::cast)
                .map(
                        key ->
                                new ECKey.Builder(Curve.P_256, key)
                                        .keyUse(KeyUse.SIGNATURE)
                                        .algorithm(new Algorithm(JWSAlgorithm.ES256.getName()))
                                        .build())
                .map(CoreIdentityValidator::new)
                .orElse(new NoopCoreIdentityValidator());
    }

    private CoreIdentityValidator(ECKey key) {
        this.key = key;
    }

    public Result isValid(String jwt) {
        try {
            return SignedJWT.parse(jwt).verify(new ECDSAVerifier(this.key))
                    ? Result.VALID
                    : Result.INVALID;
        } catch (JOSEException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    static class NoopCoreIdentityValidator extends CoreIdentityValidator {
        private NoopCoreIdentityValidator() {
            super(null);
        }

        @Override
        public Result isValid(String jwt) {
            return Result.NOT_VALIDATED;
        }
    }
}
