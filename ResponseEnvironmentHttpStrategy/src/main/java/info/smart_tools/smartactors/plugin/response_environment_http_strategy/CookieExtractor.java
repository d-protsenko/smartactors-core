package info.smart_tools.smartactors.plugin.response_environment_http_strategy;

import info.smart_tools.smartactors.core.iobject.IObject;
import io.netty.handler.codec.http.Cookie;

import java.util.List;

/**
 * Utility class for extracting cookies from environment
 */
final class CookieExtractor {

    private CookieExtractor() {
    }

    /**
     * Method, that extract cookies from environment
     *
     * @param environment Environment of the MessageProcessor
     * @return extracted cookies
     */
    static List<Cookie> extract(final IObject environment) {
        return null;
    }
}
