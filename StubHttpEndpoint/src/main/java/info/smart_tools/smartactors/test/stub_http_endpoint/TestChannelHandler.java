package info.smart_tools.smartactors.test.stub_http_endpoint;

import info.smart_tools.smartactors.core.ichannel_handler.IChannelHandler;

import java.util.List;

/**
 * Created by sevenbits on 8/15/16.
 */
public class TestChannelHandler implements IChannelHandler {

    private List<Object> responses;

    public TestChannelHandler(final List<Object> testResponses) {
        this.responses = testResponses;
    }

    @Override
    public void init(final Object channelHandler) {
        this.responses = (List<Object>) channelHandler;
    }

    @Override
    public void send(final Object response) {
        this.responses.add(response);
    }
}
