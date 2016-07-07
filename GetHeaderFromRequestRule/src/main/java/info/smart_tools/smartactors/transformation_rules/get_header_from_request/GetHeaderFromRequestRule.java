package info.smart_tools.smartactors.transformation_rules.get_header_from_request;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * The rule that extract header from request
 */
public class GetHeaderFromRequestRule implements IResolveDependencyStrategy {
    /**
     * Extracts HTTP header from given HTTP request.
     * @param args is needed parameters for resolve dependency,
     *             an HTTP request from which data will be extracted and name of needed header
     * @return an String with HTTP header
     */
    @Override
    public <T> T resolve(final Object... args) throws ResolveDependencyStrategyException {
        return (T) ((FullHttpRequest) args[0]).headers().get((String) args[1]);
    }
}
