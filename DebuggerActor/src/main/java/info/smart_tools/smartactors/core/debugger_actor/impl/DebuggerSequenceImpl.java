package info.smart_tools.smartactors.core.debugger_actor.impl;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.debugger_actor.interfaces.IDebuggerSequence;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.message_processing.exceptions.NestedChainStackOverflowException;
import info.smart_tools.smartactors.core.message_processing.exceptions.NoExceptionHandleChainException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

/**
 *
 */
public class DebuggerSequenceImpl implements IDebuggerSequence {
    private final IMessageProcessingSequence wrapped;

    public DebuggerSequenceImpl(final IMessageProcessingSequence sequence)
            throws InvalidArgumentException {
        this.wrapped = sequence;
    }

    @Override
    public boolean isExceptionOccurred() {
        return false;
    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    @Override
    public void stop() {

    }

    @Override
    public Throwable getException() {
        return null;
    }

    @Override
    public boolean processException() {
        return false;
    }

    @Override
    public void reset() {
        wrapped.reset();
    }

    @Override
    public boolean next() {
        // TODO: Implement
        return false;
    }

    @Override
    public void goTo(final int level, final int step) throws InvalidArgumentException {
        wrapped.goTo(level, step);
    }

    @Override
    public void end() {
        wrapped.end();
    }

    @Override
    public int getCurrentLevel() {
        return wrapped.getCurrentLevel();
    }

    @Override
    public int getStepAtLevel(final int level) throws InvalidArgumentException {
        return wrapped.getStepAtLevel(level);
    }

    @Override
    public IMessageReceiver getCurrentReceiver() {
        // TODO: Implement
        return null;
    }

    @Override
    public IObject getCurrentReceiverArguments() {
        // TODO: Implement
        return null;
    }

    @Override
    public void callChain(final IReceiverChain chain) throws NestedChainStackOverflowException {
        wrapped.callChain(chain);
    }

    @Override
    public void catchException(final Throwable exception, final IObject context)
            throws NoExceptionHandleChainException, NestedChainStackOverflowException, ChangeValueException,
            InvalidArgumentException, ReadValueException {

    }
}
