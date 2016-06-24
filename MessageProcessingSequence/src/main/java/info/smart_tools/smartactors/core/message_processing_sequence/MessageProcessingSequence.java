package info.smart_tools.smartactors.core.message_processing_sequence;

import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.message_processing.exceptions.NestedChainStackOverflowException;
import info.smart_tools.smartactors.core.message_processing.exceptions.NoExceptionHandleChainException;

import java.util.Arrays;

/**
 * Implementation of {@link IMessageProcessingSequence}.
 */
public class MessageProcessingSequence implements IMessageProcessingSequence {
    private final IReceiverChain mainChain;
    private final IReceiverChain[] chainStack;
    private final int[] stepStack;
    private IMessageReceiver currentReceiver;
    private int stackIndex;

    /**
     * The constructor.
     *
     * @param stackDepth maximum depth of stack of nested chains
     * @param mainChain the {@link IReceiverChain} to start message processing with
     * @throws InvalidArgumentException if stack depth is not a positive number
     * @throws InvalidArgumentException if main chain is {@code null}
     * @throws InvalidArgumentException if main chain contains no receivers
     */
    public MessageProcessingSequence(final int stackDepth, final IReceiverChain mainChain)
            throws InvalidArgumentException {
        if (stackDepth < 1) {
            throw new InvalidArgumentException("Chain stack depth should be a positive number.");
        }

        if (null == mainChain) {
            throw new InvalidArgumentException("Main chain should not be null.");
        }

        if (null == mainChain.get(0)) {
            throw new InvalidArgumentException("Main chain should contain at least one receiver.");
        }

        this.mainChain = mainChain;
        this.chainStack = new IReceiverChain[stackDepth];
        this.stepStack = new int[stackDepth];

        reset();
    }

    @Override
    public void reset() {
        this.chainStack[0] = mainChain;
        this.stepStack[0] = 0;
        this.currentReceiver = mainChain.get(0);
        this.stackIndex = 0;
    }

    @Override
    public boolean next() {
        for (int i = stackIndex; i >= 0; --i) {
            int step = ++stepStack[i];
            currentReceiver = chainStack[i].get(step);

            if (null != currentReceiver) {
                stackIndex = i;
                return true;
            }
        }

        return false;
    }

    @Override
    public IMessageReceiver getCurrentReceiver() {
        return currentReceiver;
    }

    @Override
    public void callChain(final IReceiverChain chain)
            throws NestedChainStackOverflowException {
        int newStackIndex = stackIndex + 1;

        if (newStackIndex >= chainStack.length) {
            throw new NestedChainStackOverflowException(
                    Arrays.copyOf(chainStack, stackIndex + 1),
                    Arrays.copyOf(stepStack, stackIndex + 1));
        }

        chainStack[newStackIndex] = chain;
        stepStack[newStackIndex] = -1;

        stackIndex = newStackIndex;
    }

    @Override
    public void catchException(final Throwable exception)
            throws NoExceptionHandleChainException {
        for (int i = stackIndex; i >= 0; --i) {
            IReceiverChain exceptionalChain = chainStack[i].getExceptionalChain(exception);
            if (null != exceptionalChain) {
                chainStack[i] = exceptionalChain;
                stepStack[i] = -1;
                stackIndex = i;
                return;
            }
        }

        throw new NoExceptionHandleChainException(exception,
                Arrays.copyOf(chainStack, stackIndex + 1),
                Arrays.copyOf(stepStack, stackIndex + 1));
    }
}
