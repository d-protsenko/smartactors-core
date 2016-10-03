package info.smart_tools.smartactors.testing.test_environment_handler;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;

/**
 * Implementation of {@link IReceiverChain} used as a exceptional chain for exceptions processing in testing chain.
 */
class ExceptionalTestChain implements IReceiverChain {

    @Override
    public IMessageReceiver get(final int index) {
        return null;
    }

    @Override
    public IObject getArguments(final int index) {
        return null;
    }

    @Override
    public String getName() {
        return "exceptional test chain";
    }

    @Override
    public IObject getExceptionalChainAndEnvironments(Throwable exception) {
        return null;
    }
}
