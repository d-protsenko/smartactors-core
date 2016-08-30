package info.smart_tools.smartactors.test.test_environment_handler;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;

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
    public IReceiverChain getExceptionalChain(final Throwable exception) {
        return null;
    }
}
