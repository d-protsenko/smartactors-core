package info.smart_tools.smartactors.message_processing.message_processing_sequence;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.dumpable_interface.idumpable.IDumpable;
import info.smart_tools.smartactors.dumpable_interface.idumpable.exceptions.DumpException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.NestedChainStackOverflowException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.NoExceptionHandleChainException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link IMessageProcessingSequence}.
 */
public class MessageProcessingSequence implements IMessageProcessingSequence, IDumpable {
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
    private final IFieldName stepsStackFieldName;
    private final IFieldName chainsStackFieldName;
    private final IFieldName maxDepthFieldName;
    private final IFieldName chainsDumpFieldName;
    private final IFieldName excludeExceptionalFieldName;
    private final IFieldName skipChainsFieldName;

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

        stepsStackFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "stepsStack");
        chainsStackFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "chainsStack");
        maxDepthFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "maxDepth");
        chainsDumpFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "chainsDump");
        excludeExceptionalFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "excludeExceptional");
        skipChainsFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "skipChains");

        reset();
    }

    private void uncheckedGoTo(final int level, final int step) {
        this.stackIndex = level;
        this.stepStack[level] = step - 1;
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
        while (stackIndex >= 0) {
            int step = ++stepStack[stackIndex];
            currentReceiver = chainStack[stackIndex].get(step);

            if (null != currentReceiver) {
                currentArguments = chainStack[stackIndex].getArguments(step);
                return true;
            }

            if (this.isException) {
                this.isException = false;
                try {
                    this.afterExceptionAction.execute(this);
                } catch (Throwable e) {
                    return false;
                }
            }

            --stackIndex;
        }

        stackIndex = 0;
        return false;
    }

    @Override
    public void goTo(final int level, final int step) throws InvalidArgumentException {
        if (level > stackIndex || level < 0) {
            throw new InvalidArgumentException("Invalid level value");
        }

        uncheckedGoTo(level, step);
    }

    @Override
    public void end() {
        uncheckedGoTo(0, -1);
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

    /**
     * Create a description (dump) of current state of this sequence.
     *
     * <p>
     *     The returned object will look like the following:
     * </p>
     * <pre>
     *     {
     *         "stepsStack": [1, 3, 0, 2],               // Step indexes: steps[i] = getStepAtLevel(i)
     *         "chainsStack":                            // Names of chains at levels
     *              ["rootChain", "nestedChain",
     *              "otherChain", "oneMreChain"],
     *         "maxDepth": 5,                            // Depth limit
     *         "chainsDump": {                           // Chins in the same format as they are described in configuration
     *             "rootChain": {
     *                 ...
     *             },
     *             "nestedChain": {
     *                 ... ,
     *                 "exceptional": [
     *                     {
     *                         "class": "java.lang.NullPointerException",
     *                         "chain": "npeChain",
     *                         "after": "break"
     *                     }
     *                 ]
     *             },
     *             "npeChain": {...}
     *         }
     *     }
     * </pre>
     *
     * <p>
     *     Options may contain the following fields:
     * </p>
     * <ul>
     *     <li>{@code "excludeExceptional"} - {@code true} if exceptional chains should not be included in {@code "chainsDump"}</li>
     *     <li>{@code "skipChains"} - {@code true} if {@code "chainsDump"} should be empty</li>
     * </ul>
     *
     * @param options    dump creation options
     * @return the description of current sequence's state
     * TODO: Replace by serialization method
     */
    @Override
    public IObject dump(final IObject options) throws DumpException {
        try {
            IObject dump = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

            dump.setValue(maxDepthFieldName, chainStack.length);
            dump.setValue(stepsStackFieldName,
                    Arrays.stream(Arrays.copyOf(stepStack, stackIndex + 1)).boxed().collect(Collectors.toList()));
            dump.setValue(chainsStackFieldName,
                    Arrays.stream(Arrays.copyOf(chainStack, stackIndex + 1)).map(IReceiverChain::getName).collect(Collectors.toList()));

            Object skipChains = options.getValue(skipChainsFieldName);
            Object excludeExceptional = options.getValue(excludeExceptionalFieldName);

            if (skipChains == null || !(boolean) skipChains) {
                IObject chainsDump = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

                dump.setValue(chainsDumpFieldName, chainsDump);

                Set<IReceiverChain> dumpedChains = new HashSet<>();

                for (int i = 0; i <= stackIndex; i++) {
                    addChainsToDump(dumpedChains, chainStack[i],
                            excludeExceptional != null && (boolean) excludeExceptional);
                }

                for (IReceiverChain chain : dumpedChains) {
                    Object chainDump = IOC.resolve(Keys.getOrAdd("make dump"), chain, options);
                    IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), chain.getName());

                    chainsDump.setValue(fieldName, chainDump);
                }
            }

            return dump;
        } catch (ResolutionException | ChangeValueException | ReadValueException | InvalidArgumentException e) {
            throw new DumpException("Error occurred creating dump of message processing sequence.", e);
        }
    }

    private void addChainsToDump(final Set<IReceiverChain> toDump, final IReceiverChain chain, final boolean skipExceptional) {
        if (toDump.add(chain) && !skipExceptional) {
            for (IReceiverChain exceptionalChain : chain.getExceptionalChains()) {
                addChainsToDump(toDump, exceptionalChain, false);
            }
        }
    }
}
