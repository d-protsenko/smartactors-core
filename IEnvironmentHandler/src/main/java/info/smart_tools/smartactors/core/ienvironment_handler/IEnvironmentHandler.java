package info.smart_tools.smartactors.core.ienvironment_handler;

import info.smart_tools.smartactors.core.ienvironment_handler.exception.RequestHandlerInternalException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;

/**
 * Interface of environment handler, that should create MessageProcessor and process it
 */
public interface IEnvironmentHandler {
    /**
     * Method for handle environment
     *
     * @param environment   Environment of the {@link info.smart_tools.smartactors.core.message_processing.IMessageProcessor}
     * @param receiverChain Chain that should receive environment
     * @throws RequestHandlerInternalException if there are problems on handling environment
     */
    void handle(IObject environment, IReceiverChain receiverChain) throws RequestHandlerInternalException;
}
