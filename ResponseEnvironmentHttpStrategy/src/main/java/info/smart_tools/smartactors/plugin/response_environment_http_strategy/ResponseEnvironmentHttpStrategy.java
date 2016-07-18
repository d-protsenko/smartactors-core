package info.smart_tools.smartactors.plugin.response_environment_http_strategy;

import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iresponse.IResponse;
import info.smart_tools.smartactors.core.iresponse_environment_strategy.IResponseEnvironmentStrategy;

public class ResponseEnvironmentHttpStrategy implements IResponseEnvironmentStrategy {

    @Override
    public void setEnvironment(final IObject environment, IResponse response)
            throws ResolutionException, ReadValueException, InvalidArgumentException {
        response.setEnvironment("headers", HeadersExtractor.extract(environment));
        response.setEnvironment("cookies", CookieExtractor.extract(environment));
    }
}
