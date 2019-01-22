package info.smart_tools.smartactors.http_endpoint.strategy.get_header_from_request;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

/**
 * The rule that extract header from request
 */
public class GetHeaderFromRequestRule implements IResolutionStrategy {
    /**
     * Extracts HTTP header from given HTTP request.
     * @param args is needed parameters for resolve dependency,
     *             an HTTP request from which data will be extracted and name of needed header
     * @return an String with HTTP header
     */
    @Override
    public <T> T resolve(final Object... args) throws ResolutionStrategyException {
        try {
            FullHttpRequest request = (FullHttpRequest) args[0];
            HttpHeaders headers = request.headers();
            String headerName = (String) args[1];
            return (T) headers.get(headerName);
        } catch (ClassCastException e) {
            throw new ResolutionStrategyException("Some of args or header value can't be casted");
        }
    }
}
