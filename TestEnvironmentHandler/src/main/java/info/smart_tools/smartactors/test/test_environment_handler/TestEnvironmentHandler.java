package info.smart_tools.smartactors.test.test_environment_handler;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.ienvironment_handler.exception.EnvironmentHandleException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.message_processing.exceptions.NestedChainStackOverflowException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.test.test_environment_handler.exception.InvalidTestDescriptionException;
import info.smart_tools.smartactors.test.test_environment_handler.exception.TestStartupException;
import info.smart_tools.smartactors.test.test_environment_handler.checkers.TestResultChecker;

/**
 * Implementation of {@link IEnvironmentHandler}.
 * This implementation prepares and start processing test chain
 */
public class TestEnvironmentHandler implements IEnvironmentHandler {

    private static final int STACK_DEPTH = 5;

    private final IFieldName environmentFieldName;
    private final IFieldName messageFieldName;
    private final IFieldName contextFieldName;
//    private final IFieldName chainNameFieldName;

    /**
     * The constructor.
     *
     * @throws ResolutionException if resolution of any dependencies fails
     */
    public TestEnvironmentHandler()
            throws ResolutionException {
        environmentFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "environment");
        messageFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message");
        contextFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "context");
//        chainNameFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "chainName");
    }

    @Override
    public void handle(final IObject environment, final IReceiverChain receiverChain, final IAction<Throwable> callback)
            throws InvalidArgumentException, EnvironmentHandleException {
        if (null == environment) {
            throw new InvalidArgumentException("Description should not be null.");
        }

        if (null == callback) {
            throw new InvalidArgumentException("Callback should not be null.");
        }

        if (null == receiverChain) {
            throw new InvalidArgumentException("Receiver chain should not be null.");
        }

        try {
            TestResultChecker checker = TestResultChecker.createChecker(environment);

            IMessageProcessor[] fmp = new IMessageProcessor[1];

            IObject environmentDesc = (IObject) environment.getValue(environmentFieldName);

            IObject message = (IObject) environmentDesc.getValue(messageFieldName);
            IObject context = (IObject) environmentDesc.getValue(contextFieldName);

//            String chainName = (String) environment.getValue(chainNameFieldName);

//            Object chainId = IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), chainName);

//            IChainStorage chainStorage = IOC.resolve(Keys.getOrAdd(IChainStorage.class.getCanonicalName()));
//
//            IReceiverChain testedChain = chainStorage.resolve(chainId);

            IAction<Throwable> completionCallback = exc -> {
                try {
                    checker.check(fmp[0], exc);

                    callback.execute(null);
                } catch (Exception e) {
                    callback.execute(e);
                }
            };

            MainTestChain mainTestChain = IOC.resolve(Keys.getOrAdd(MainTestChain.class.getCanonicalName()),
                    completionCallback, checker.getSuccessfulReceiverArguments());

            IMessageProcessingSequence sequence = IOC.resolve(Keys.getOrAdd(IMessageProcessingSequence.class.getCanonicalName()),
                    STACK_DEPTH, mainTestChain);

            sequence.callChain(receiverChain);

            IQueue<ITask> taskQueue = IOC.resolve(Keys.getOrAdd("task_queue"));

            IMessageProcessor mp = IOC.resolve(Keys.getOrAdd(IMessageProcessor.class.getCanonicalName()), taskQueue, sequence);
            fmp[0] = mp;

            mp.process(message, context);
        } catch (ReadValueException | ResolutionException | NestedChainStackOverflowException
                | ChangeValueException | TestStartupException | InvalidTestDescriptionException e) {
            //throw new TestStartupException(e);
            throw new EnvironmentHandleException(e);
        } catch (ClassCastException e) {
            //throw new InvalidTestDescriptionException("Could not cast value to required type.", e);
            throw new EnvironmentHandleException("Could not cast value to required type.", e);
        }
    }
}
