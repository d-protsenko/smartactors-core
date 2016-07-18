package info.smart_tools.smartactors.core.response;


import info.smart_tools.smartactors.core.iresponse.IResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of IResponse with using {@link Map} to save environment
 */
public class Response implements IResponse {
    private byte[] content;
    private Map<String, Object> environment = new HashMap<>();

    @Override
    public void setEnvironment(final String key, final Object environment) {
        this.environment.put(key, environment);
    }

    @Override
    public void setContent(final byte[] response) {
        content = response;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public <T> T getEnvironment(final String key) {
        return (T) environment.get(key);
    }
}
