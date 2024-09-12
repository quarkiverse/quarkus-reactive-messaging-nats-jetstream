package io.quarkiverse.reactive.messaging.nats.jetstream.graal;

import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;

@TargetClass(EdDSAEngine.class)
public final class Target_EdDSAEngine {

    @Alias
    private MessageDigest digest;

    @Alias
    private ByteArrayOutputStream baos;

    @Alias
    private EdDSAKey key;

    @Alias
    private boolean oneShotMode;

    @Alias
    private byte[] oneShotBytes;

    @Substitute
    private void reset() {
        if (digest != null)
            digest.reset();
        if (baos != null)
            baos.reset();
        oneShotMode = false;
        oneShotBytes = null;
    }

    @Substitute
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        reset();
        if (publicKey instanceof EdDSAPublicKey) {
            key = (EdDSAPublicKey) publicKey;

            if (digest == null) {
                // Instantiate the digest from the key parameters
                try {
                    digest = MessageDigest.getInstance(key.getParams().getHashAlgorithm());
                } catch (NoSuchAlgorithmException e) {
                    throw new InvalidKeyException(
                            "cannot get required digest " + key.getParams().getHashAlgorithm() + " for private key.");
                }
            } else if (!key.getParams().getHashAlgorithm().equals(digest.getAlgorithm()))
                throw new InvalidKeyException("Key hash algorithm does not match chosen digest");
        } else if (publicKey.getFormat().equals("X.509")) {
            // X509Certificate will sometimes contain an X509Key rather than the EdDSAPublicKey itself; the contained
            // key is valid but needs to be instanced as an EdDSAPublicKey before it can be used.
            EdDSAPublicKey parsedPublicKey;
            try {
                parsedPublicKey = new EdDSAPublicKey(new X509EncodedKeySpec(publicKey.getEncoded()));
            } catch (InvalidKeySpecException ex) {
                throw new InvalidKeyException("cannot handle X.509 EdDSA public key: " + publicKey.getAlgorithm());
            }
            engineInitVerify(parsedPublicKey);
        } else {
            throw new InvalidKeyException("cannot identify EdDSA public key: " + publicKey.getClass());
        }
    }
}
