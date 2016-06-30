package info.smart_tools.smartactors.core.security.encoding;


import javax.annotation.Nonnull;

/**
 *
 */
public interface IPasswordEncoder extends IEncoder {
    /**
     *
     * @param password
     * @return
     */
    String encode(@Nonnull final String password);

    /**
     *
     * @param password
     * @param salt
     * @return
     */
    String encode(@Nonnull final String password, @Nonnull final String salt);
}
