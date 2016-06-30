package info.smart_tools.smartactors.core.security.encoding;

import javax.annotation.Nonnull;

/**
 *
 */
public interface IEncoder {
    /**
     *
     * @param message
     * @return
     */
    byte[] encode(@Nonnull final CharSequence message);
}
