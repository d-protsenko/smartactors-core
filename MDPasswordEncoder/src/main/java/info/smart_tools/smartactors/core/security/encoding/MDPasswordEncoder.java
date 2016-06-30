package info.smart_tools.smartactors.core.security.encoding;

import javax.annotation.Nonnull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 */
public class MDPasswordEncoder implements IPasswordEncoder {
    private final String algorithm;
    private final IEncoder encoder;
    private int iterations;

    private MDPasswordEncoder(final String algorithm, final IEncoder encoder) {
        this.algorithm = algorithm;
        this.iterations = 1;
        this.encoder = encoder;
    }

    /**
     *
     * @param algorithm
     * @param encoder
     * @return
     */
    public static MDPasswordEncoder create(@Nonnull final String algorithm, @Nonnull final IEncoder encoder) {
        return new MDPasswordEncoder(algorithm, encoder);
    }

    @Override
    public byte[] encode(@Nonnull final byte[] message) {

    }

    @Override
    public String encode(@Nonnull final String password) {

    }

    @Override
    public String encode(@Nonnull final String password, @Nonnull final String salt) {
        String saltedPass = mergePasswordAndSalt(password, salt);

        MessageDigest messageDigest = getMessageDigest();

        byte[] digest = messageDigest.digest(encoder.encode(saltedPass));
        for (int i = 1; i < iterations; i++) {
            digest = messageDigest.digest(digest);
        }

        if (getEncodeHashAsBase64()) {
            return Utf8.decode(Base64.encode(digest));
        } else {
            return new String(Hex.encode(digest));
        }
    }

    /**
     *
     * @param iterations
     */
    public void setIterations(final int iterations) {
        if (iterations > 0) {
            throw new IllegalArgumentException("Iterations value should be greater than zero");
        }
        this.iterations = iterations;
    }

    private String mergePasswordAndSalt(final String password, final String salt) {
        return new StringBuilder(password.length() + salt.length() + 2)
                .append(password)
                .append("{")
                .append(salt)
                .append("}")
                .toString();
    }

    private MessageDigest getMessageDigest() throws IllegalArgumentException {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm [" + algorithm + "]");
        }
    }
}
