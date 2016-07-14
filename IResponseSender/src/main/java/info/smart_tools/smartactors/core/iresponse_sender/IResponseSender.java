package info.smart_tools.smartactors.core.iresponse_sender;

import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;

public interface IResponseSender {
    void send(IMessageProcessor messageProcessor);
}
