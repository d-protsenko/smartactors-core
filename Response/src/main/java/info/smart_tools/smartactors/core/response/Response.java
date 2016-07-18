package info.smart_tools.smartactors.core.response;


import info.smart_tools.smartactors.core.iresponse.IResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of IResponse with using {@link Map} to save environment
 */
public class Response implements IResponse {
    private byte[] context;
    private Map<String, Object> environment = new HashMap<>();

    @Override
    public void setEnvironment(final String key, final Object environment) {
        this.environment.put(key, environment);
    }

    @Override
    public void setContent(final byte[] response) {
        context = response;
    }

    @Override
    public byte[] getBody() {
        return context;
    }

    @Override
    public <T> T getEnvironment(String key) {
        return (T) environment.get(key);
    }
}
