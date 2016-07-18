package info.smart_tools.smartactors.plugin.response_environment_http_strategy;

import info.smart_tools.smartactors.core.iobject.IObject;
import io.netty.handler.codec.http.HttpHeaders;

/**
 * Utility class for extracting headers from environment
 */
final class HeadersExtractor {
    private HeadersExtractor() {
    }

    /**
     * Method, that extract http headers from environment
     *
     * @param environment Environment of the MessageProcessor
     * @return extracted http headers
     */
    static HttpHeaders extract(final IObject environment) {
        return null;
    }
}
