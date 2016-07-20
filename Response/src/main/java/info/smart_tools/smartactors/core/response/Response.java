package info.smart_tools.smartactors.core.response;


import info.smart_tools.smartactors.core.iresponse.IResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of IResponse with using {@link Map} to save environment
 */
public class Response implements IResponse {
    private byte[] content;

    @Override
    public void setContent(final byte[] response) {
        content = response;
    }

    @Override
    public byte[] getContent() {
        return content;
    }
}
