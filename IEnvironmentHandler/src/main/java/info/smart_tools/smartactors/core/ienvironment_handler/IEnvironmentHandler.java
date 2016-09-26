package info.smart_tools.smartactors.core.ienvironment_handler;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.core.ienvironment_handler.exception.EnvironmentHandleException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;

/**
 * Interface of environment handler, that should create MessageProcessor and process it
 */
public interface IEnvironmentHandler {
    /**
     * Method for handle environment
     * @param environment Environment of the {@link info.smart_tools.smartactors.core.message_processing.IMessageProcessor}
     * @param receiverChain Chain that should receive environment
     * @param callback the callback for processing exception
     */
    void handle(IObject environment, IReceiverChain receiverChain, IAction<Throwable> callback)
            throws InvalidArgumentException, EnvironmentHandleException;
}
