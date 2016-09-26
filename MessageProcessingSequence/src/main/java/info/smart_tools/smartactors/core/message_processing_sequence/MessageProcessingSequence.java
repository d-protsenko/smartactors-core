package info.smart_tools.smartactors.core.message_processing_sequence;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
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
    private IObject currentArguments;
    private int stackIndex;

    private boolean isException = false;
    private IAction<IMessageProcessingSequence> afterExceptionAction = null;

    private final IFieldName causeLevelFieldName;
    private final IFieldName causeStepFieldName;
    private final IFieldName catchLevelFieldName;
    private final IFieldName catchStepFieldName;
    private final IFieldName exceptionFieldName;
    private final IFieldName chainFieldName;
    private final IFieldName afterExceptionActionFieldName;

    /**
     * The constructor.
     *
     * @param stackDepth maximum depth of stack of nested chains
     * @param mainChain the {@link IReceiverChain} to start message processing with
     * @throws InvalidArgumentException if stack depth is not a positive number
     * @throws InvalidArgumentException if main chain is {@code null}
     * @throws InvalidArgumentException if main chain contains no receivers
     * @throws ResolutionException if resolution of any dependencies fails
     */
    public MessageProcessingSequence(final int stackDepth, final IReceiverChain mainChain)
            throws InvalidArgumentException, ResolutionException {
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

        causeLevelFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "causeLevel");
        causeStepFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "causeStep");
        catchLevelFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "catchLevel");
        catchStepFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "catchStep");
        exceptionFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "exception");
        this.afterExceptionActionFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "after");
        this.chainFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "chain");

        reset();
    }

    @Override
    public void reset() {
        this.chainStack[0] = mainChain;
        this.stepStack[0] = 0;
        this.currentReceiver = mainChain.get(0);
        this.currentArguments = mainChain.getArguments(0);
        this.stackIndex = 0;
    }

    @Override
    public boolean next() {
        for (int i = stackIndex; i >= 0; --i) {
            int step = ++stepStack[i];
            currentReceiver = chainStack[i].get(step);

            if (null != currentReceiver) {
                currentArguments = chainStack[i].getArguments(step);
                stackIndex = i;
                return true;
            }

            if (this.isException) {
                this.isException = false;
                try {
                    this.afterExceptionAction.execute(this);
                    if (this.stackIndex < 0) {
                        return false;
                    }
                } catch (Throwable e) {
                    return false;
                }
            }
        }

        return false;
    }

    @Override
    public void goTo(final int level, final int step) throws InvalidArgumentException {
        if (level > stackIndex || level < 0) {
            throw new InvalidArgumentException("Invalid level value");
        }

        this.stackIndex = level;
        this.stepStack[level] = step - 1;
    }

    @Override
    public void end() {
        this.stackIndex = -1;
    }

    @Override
    public int getCurrentLevel() {
        return stackIndex;
    }

    @Override
    public int getStepAtLevel(final int level) throws InvalidArgumentException {
        if (level < 0 || level > stackIndex) {
            throw new InvalidArgumentException("Level index is out of range.");
        }

        return stepStack[level];
    }

    @Override
    public IMessageReceiver getCurrentReceiver() {
        return currentReceiver;
    }

    @Override
    public IObject getCurrentReceiverArguments() {
        return currentArguments;
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
    public void catchException(final Throwable exception, final IObject context)
            throws NoExceptionHandleChainException,
                   NestedChainStackOverflowException,
                   ChangeValueException,
                   InvalidArgumentException,
                   ReadValueException {
        int causedLevel = stackIndex;
        int causedStep = stepStack[causedLevel];
        int caughtLevel;
        int caughtStep;

        for (int i = stackIndex; i >= 0; --i) {
            IObject exceptionalChainAndEnv = chainStack[i].getExceptionalChainAndEnvironments(exception);
            if (null != exceptionalChainAndEnv) {
                this.afterExceptionAction = (IAction<IMessageProcessingSequence>) exceptionalChainAndEnv.getValue(this.afterExceptionActionFieldName);
                IReceiverChain exceptionalChain = (IReceiverChain) exceptionalChainAndEnv.getValue(this.chainFieldName);
                caughtLevel = i;
                caughtStep = stepStack[caughtLevel];

                context.setValue(causeLevelFieldName, causedLevel);
                context.setValue(causeStepFieldName, causedStep);
                context.setValue(catchLevelFieldName, caughtLevel);
                context.setValue(catchStepFieldName, caughtStep);
                context.setValue(exceptionFieldName, exception);

                this.isException = true;
                callChain(exceptionalChain);
                return;
            }
        }

        throw new NoExceptionHandleChainException(exception,
                Arrays.copyOf(chainStack, stackIndex + 1),
                Arrays.copyOf(stepStack, stackIndex + 1));
    }
}
