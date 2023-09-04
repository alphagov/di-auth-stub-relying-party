package uk.gov.di.utils;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;

public class PrivateKeyReader {
    private String privateKey;

    public PrivateKeyReader(String privateKey) {
        this.privateKey = privateKey;
    }

    public RSAPrivateKey get() {
        try {
            var kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey)
                    kf.generatePrivate(new PKCS8EncodedKeySpec(format(this.privateKey)));
        } catch (InvalidKeySpecException | IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] format(String privateKey) throws IOException {
        var parse = Pattern.compile("(?m)(?s)^---*BEGIN.*---*$(.*)^---*END.*---*$.*");
        var encoded = parse.matcher(privateKey).replaceFirst("$1");

        return Base64.getMimeDecoder().decode(encoded);
    }
}
