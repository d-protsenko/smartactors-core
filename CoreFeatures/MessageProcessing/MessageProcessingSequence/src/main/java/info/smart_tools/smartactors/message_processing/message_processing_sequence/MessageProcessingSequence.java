package info.smart_tools.smartactors.message_processing.message_processing_sequence;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.class_management.module_manager.ModuleManager;
import info.smart_tools.smartactors.dumpable_interface.idumpable.IDumpable;
import info.smart_tools.smartactors.dumpable_interface.idumpable.exceptions.DumpException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.ChainChoiceException;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.NestedChainStackOverflowException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.NoExceptionHandleChainException;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Implementation of {@link IMessageProcessingSequence}.
 */
public class MessageProcessingSequence implements IMessageProcessingSequence, IDumpable {
    private final IChainStorage chainStorage;
    private final IKey chainIdStrategyKey;
    private final IReceiverChain mainChain;
    private final IReceiverChain[] chainStack;
    private final int[] stepStack;
    private IMessageReceiver currentReceiver;
    private IObject currentArguments;
    private IObject message;
    private int stackIndex;
    private Object scopeRestorationChain;

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
    //private final IFieldName chainsDumpFieldName;
    //private final IFieldName excludeExceptionalFieldName;
    //private final IFieldName skipChainsFieldName;
    private final IFieldName externalAccessFieldName;
    private final IFieldName fromExternalFieldName;
    private final IFieldName accessForbiddenFieldName;

    /**
     * The constructor.
     *
     * @param stackDepth maximum depth of stack of nested chains
     * @param mainChainName the {@link IReceiverChain} to start message processing with
     * @throws InvalidArgumentException if stack depth is not a positive number
     * @throws InvalidArgumentException if main chain is {@code null}
     * @throws InvalidArgumentException if main chain contains no receivers
     * @throws ResolutionException if resolution of any dependencies fails
     */
    public MessageProcessingSequence(final int stackDepth, final Object mainChainName, final IObject message)
            throws InvalidArgumentException, ResolutionException, ChainNotFoundException {
        if (stackDepth < 1) {
            throw new InvalidArgumentException("Chain stack depth should be a positive number.");
        }

        if (null == mainChainName) {
            throw new InvalidArgumentException("Main chain should not be null.");
        }



        this.scopeRestorationChain = null;
        this.chainStack = new IReceiverChain[stackDepth];
        this.stepStack = new int[stackDepth];

        causeLevelFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "causeLevel");
        causeStepFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "causeStep");
        catchLevelFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "catchLevel");
        catchStepFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "catchStep");
        exceptionFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "exception");
        this.afterExceptionActionFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "after");
        this.chainFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "chain");

        stepsStackFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "stepsStack");
        chainsStackFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "chainsStack");
        maxDepthFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "maxDepth");
        //chainsDumpFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "chainsDump");
        //excludeExceptionalFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "excludeExceptional");
        //skipChainsFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "skipChains");
        externalAccessFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "externalAccess");
        fromExternalFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "fromExternal");
        accessForbiddenFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "accessToChainForbiddenError");

        chainIdStrategyKey = Keys.getOrAdd("chain_id_from_map_name_and_message");
        chainStorage = IOC.resolve(Keys.getOrAdd(IChainStorage.class.getCanonicalName()));
        this.message = message;

        this.mainChain = resolveChain(mainChainName, message);

        if (null == this.mainChain.get(0)) {
            throw new InvalidArgumentException("Main chain should contain at least one receiver.");
        }

        reset();
    }

    private IReceiverChain resolveChain(final Object chainName, IObject message)
            throws ResolutionException, ChainNotFoundException {
        Object chainId = IOC.resolve(chainIdStrategyKey, chainName, message);
        return chainStorage.resolve(chainId);
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
    public void callChainSecurely(final Object chainName, IMessageProcessor processor)
            throws NestedChainStackOverflowException, ResolutionException, ChainNotFoundException,
            ChainChoiceException, ScopeProviderException {

        IReceiverChain chain = resolveChain(chainName, message);
        if (processor != null) {
            checkAccess(chain, processor);
        }
        putChainToStack(chain);
    }

    @Override
    public void callChain(final Object chainName)
            throws NestedChainStackOverflowException, ResolutionException, ChainNotFoundException, ScopeProviderException {

        IReceiverChain chain = resolveChain(chainName, message);
        putChainToStack(chain);
    }

    private void putChainToStack(IReceiverChain chain)
            throws NestedChainStackOverflowException, ScopeProviderException {
        int newStackIndex = stackIndex + 1;

        if (newStackIndex >= chainStack.length) {
            throw new NestedChainStackOverflowException(
                    Arrays.copyOf(chainStack, stackIndex + 1),
                    Arrays.copyOf(stepStack, stackIndex + 1));
        }

        chainStack[newStackIndex] = chain;
        stepStack[newStackIndex] = -1;

        stackIndex = newStackIndex;

        if (chain.getName().equals(scopeRestorationChain)) {
            ScopeProvider.setCurrentScope(chain.getScope());
            ModuleManager.setCurrentModule(chain.getModule());
        }
    }

    private void checkAccess(final IReceiverChain chain, final IMessageProcessor processor)
            throws ChainChoiceException {
        try {
            boolean isExternal = (boolean) chain.getChainDescription().getValue(this.externalAccessFieldName);
            Boolean fromExternal = (Boolean) processor.getContext().getValue(fromExternalFieldName);
            if (null != fromExternal && fromExternal) {
                processor.getContext().setValue(fromExternalFieldName, false);
                if (!isExternal) {
                    processor.getContext().setValue(this.accessForbiddenFieldName, true);
                    throw new ChainChoiceException("External access forbidden to chain - " + chain.getId() + ".");
                }
            }
        } catch (ReadValueException | ChangeValueException | InvalidArgumentException e) {
            throw new ChainChoiceException("Access forbidden.", e);
        }
    }

    @Override
    public void catchException(final Throwable exception, final IObject context)
            throws NoExceptionHandleChainException,
                   NestedChainStackOverflowException,
                   ChangeValueException,
                   InvalidArgumentException,
                   ChainNotFoundException,
                   ResolutionException,
                   ReadValueException,
                   ScopeProviderException {
        int causedLevel = stackIndex;
        int causedStep = stepStack[causedLevel];
        int caughtLevel;
        int caughtStep;

        for (int i = stackIndex; i >= 0; --i) {
            IObject exceptionalChainAndEnv = chainStack[i].getExceptionalChainNamesAndEnvironments(exception);
            if (null != exceptionalChainAndEnv) {
                this.afterExceptionAction = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), exceptionalChainAndEnv.getValue(this.afterExceptionActionFieldName))
                );
                caughtLevel = i;
                caughtStep = stepStack[caughtLevel];

                context.setValue(causeLevelFieldName, causedLevel);
                context.setValue(causeStepFieldName, causedStep);
                context.setValue(catchLevelFieldName, caughtLevel);
                context.setValue(catchStepFieldName, caughtStep);
                context.setValue(exceptionFieldName, exception);

                this.isException = true;
                callChain(exceptionalChainAndEnv.getValue(this.chainFieldName));
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
            IObject dump = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"));

            dump.setValue(maxDepthFieldName, chainStack.length);
            dump.setValue(stepsStackFieldName,
                    Arrays.stream(Arrays.copyOf(stepStack, stackIndex + 1)).boxed().collect(Collectors.toList()));
            dump.setValue(chainsStackFieldName,
                    Arrays.stream(Arrays.copyOf(chainStack, stackIndex + 1)).map(IReceiverChain::getId).collect(Collectors.toList()));

            // ToDo: check if entire chain dump is necessary ?
            /*
            Object skipChains = options.getValue(skipChainsFieldName);
            Object excludeExceptional = options.getValue(excludeExceptionalFieldName);

            if (skipChains == null || !(boolean) skipChains) {
                IObject chainsDump = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"));

                dump.setValue(chainsDumpFieldName, chainsDump);

                Set<IReceiverChain> dumpedChains = new HashSet<>();

                for (int i = 0; i <= stackIndex; i++) {
                    addChainsToDump(dumpedChains, chainStack[i],
                            excludeExceptional != null && (boolean) excludeExceptional);
                }

                for (IReceiverChain chain : dumpedChains) {
                    Object chainDump = IOC.resolve(Keys.getOrAdd("make dump"), chain, options);
                    IFieldName fieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), chain.getId());

                    chainsDump.setValue(fieldName, chainDump);
                }
            }
            */
            return dump;
        } catch (ResolutionException | ChangeValueException | InvalidArgumentException
                /*| ReadValueException | ChainNotFoundException*/ e) {
            throw new DumpException("Error occurred creating dump of message processing sequence.", e);
        }
    }

    /*
    private void addChainsToDump(final Set<IReceiverChain> toDump, final IReceiverChain chain, final boolean skipExceptional)
            throws ChainNotFoundException, ResolutionException {
        if (toDump.add(chain) && !skipExceptional) {
            for (Object exceptionalChain : chain.getExceptionalChainNames()) {
                addChainsToDump(toDump, resolveChain(exceptionalChain, message), false);
            }
        }
    }
    */
}
