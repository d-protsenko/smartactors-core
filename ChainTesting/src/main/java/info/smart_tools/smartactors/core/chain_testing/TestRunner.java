package info.smart_tools.smartactors.core.chain_testing;

import info.smart_tools.smartactors.core.chain_testing.exceptions.AssertionFailureException;
import info.smart_tools.smartactors.core.chain_testing.exceptions.InvalidTestDescriptionException;
import info.smart_tools.smartactors.core.chain_testing.exceptions.TestStartupException;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.core.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.irouter.IRouter;
import info.smart_tools.smartactors.core.irouter.exceptions.RouteNotFoundException;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.message_processing.exceptions.NestedChainStackOverflowException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.text.MessageFormat;
import java.util.List;

/**
 * Runs tests on receiver chains.
 *
 * If the chain should be completed without exceptions then the description should have the following format:
 *
 * <pre>
 *     {
 *         "name": "Unique test name",                      // Unique name of the test
 *         "environment": {
 *             "message": {                                 // The message to send
 *                 "a": 10,
 *                 "b": 42
 *             },
 *             "context": {}                                // Context of the message
 *         },
 *         "chainName": "addChain",                         // Name of the chain to test
 *         "assert": [                                      // List of assertions to check
 *          {
 *              "name": "result should be correct",         // Unique name of the assertion
 *              "type": "eq",                               // Type of the assertion
 *              "value": "message/c"                        // Value to check. The value is described just like getter method of wrapper
 *                                                          // interface
 *              "to": 52,                                   // Additional argument(s) of assertion. Name(s) depend(s) on concrete assertion
 *                                                          // type
 *          }
 *         ]
 *     }
 * </pre>
 *
 * If the tested chain should be completed with exception then the description should have the following format:
 *
 * <pre>
 *     {
 *         "name": "Another test name",
 *         "environment": {
 *             "message": {
 *                 "a": 10,
 *                 "b": 0
 *             },
 *             "context": {}
 *         },
 *         "chainName": "divideChain",
 *         "intercept": {
 *             "class": "java.lang.ArithmeticException",    // Canonical name of exception class
 *             "target": "divideReceiver"                   // Id of the receiver that should throw exception (as if the "intercept" section
 *                                                          // was a description of a object)
 *         }
 *     }
 * </pre>
 */
public class TestRunner {
    private static final int STACK_DEPTH = 5;

    private final IFieldName environmentFieldName;
    private final IFieldName messageFieldName;
    private final IFieldName contextFieldName;
    private final IFieldName chainNameFieldName;
    private final IFieldName interceptFieldName;
    private final IFieldName interceptClassFieldName;
    private final IFieldName assertFieldName;
    private final IFieldName assertTypeFieldName;
    private final IFieldName assertNameFieldName;
    private final IFieldName assertValueFieldName;
    private final IFieldName wrapperFieldName;

    /**
     * The constructor.
     *
     * @throws ResolutionException if resolution of any dependencies fails
     */
    public TestRunner()
            throws ResolutionException {

        environmentFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "environment");
        messageFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message");
        contextFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "context");
        chainNameFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "chainName");
        assertFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "assert");
        assertTypeFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "type");
        assertNameFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
        assertValueFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "value");
        interceptFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "intercept");
        interceptClassFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "class");
        wrapperFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "wrapper");
    }

    /**
     * Run a test described by the given {@link IObject}.
     *
     * @param description    description of the test to run
     * @param callback       callback to call when test is completed (with {@code null} as the only argument in case of success or with
     *                       exception describing failure reasons in case of failure)
     * @throws InvalidArgumentException if {@code description} is {@code null}
     * @throws InvalidArgumentException if {@code callback} is {@code null}
     * @throws InvalidTestDescriptionException if test description has invalid format
     * @throws TestStartupException if failed to start the test
     */
    public void runTest(final IObject description, final IAction<Throwable> callback)
            throws InvalidArgumentException, InvalidTestDescriptionException, TestStartupException {
        if (null == description) {
            throw new InvalidArgumentException("Description should not be null.");
        }

        if (null == callback) {
            throw new InvalidArgumentException("Callback should not be null.");
        }

        try {
            final IMessageProcessor[] fmp = new IMessageProcessor[1];

            IObject environmentDesc = (IObject) description.getValue(environmentFieldName);

            IObject message = (IObject) environmentDesc.getValue(messageFieldName);
            IObject context = (IObject) environmentDesc.getValue(contextFieldName);

            String chainName = (String) environmentDesc.getValue(chainNameFieldName);

            Object chainId = IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), chainName);

            IChainStorage chainStorage = IOC.resolve(Keys.getOrAdd(IChainStorage.class.getCanonicalName()));

            IReceiverChain testedChain = chainStorage.resolve(chainId);

            IAction<Throwable> completionCallback = exc -> {
                try {
                    if (exc != null) {
                        verifyException(exc, description, fmp[0]);
                    } else {
                        verifyAssertions(description, fmp[0]);
                    }

                    callback.execute(null);
                } catch (Exception e) {
                    callback.execute(e);
                }
            };

            IObject successReceiverArgs = prepareSuccessReceiverArguments(description);

            MainTestChain mainTestChain = IOC.resolve(Keys.getOrAdd(MainTestChain.class.getCanonicalName()),
                    completionCallback, successReceiverArgs);

            IMessageProcessingSequence sequence = IOC.resolve(Keys.getOrAdd(IMessageProcessingSequence.class.getCanonicalName()),
                    STACK_DEPTH, mainTestChain);

            sequence.callChain(testedChain);

            IQueue<ITask> taskQueue = IOC.resolve(Keys.getOrAdd("task_queue"));

            IMessageProcessor mp = IOC.resolve(Keys.getOrAdd(IMessageProcessor.class.getCanonicalName()), taskQueue, sequence);
            fmp[0] = mp;

            mp.process(message, context);
        } catch (ReadValueException | ResolutionException | ChainNotFoundException | NestedChainStackOverflowException
                | ChangeValueException e) {
            throw new TestStartupException(e);
        } catch (ClassCastException e) {
            throw new InvalidTestDescriptionException("Could not cast value to required type.", e);
        }
    }

    private void verifyException(final Throwable thrown, final IObject description, final IMessageProcessor mp)
            throws AssertionFailureException, ReadValueException, InvalidArgumentException {
        IObject interceptor = (IObject) description.getValue(interceptFieldName);

        if (null == interceptor) {
            throw new AssertionFailureException("Unexpected exception.", thrown);
        }

        verifyExceptionClass(thrown, interceptor);
        verifyExceptionLocation(interceptor, mp);
    }

    private void verifyExceptionClass(final Throwable thrown, final IObject interceptor)
            throws AssertionFailureException, ReadValueException, InvalidArgumentException {
        try {
            String expectedName = (String) interceptor.getValue(interceptClassFieldName);
            Class<?> expectedClass = getClass().getClassLoader().loadClass(expectedName);

            for (Throwable t = thrown; t != null; t = t.getCause()) {
                if (expectedClass.isInstance(t)) {
                    return;
                }
            }

            throw new AssertionFailureException(
                    MessageFormat.format("Chain has thrown exception of class {0} instead of expected {1}.",
                            thrown.getClass().getCanonicalName(), expectedName), thrown);
        } catch (ClassNotFoundException e) {
            throw new AssertionFailureException("Could not load expected exception class.", e);
        }
    }

    private void verifyExceptionLocation(final IObject interceptor, final IMessageProcessor mp)
            throws AssertionFailureException {
        try {
            Object expectedReceiverId = IOC.resolve(Keys.getOrAdd("receiver_id_from_iobject"), interceptor);
            IRouter router = IOC.resolve(Keys.getOrAdd(IRouter.class.getCanonicalName()));
            IMessageReceiver expectedReceiver = router.route(expectedReceiverId);

            if (expectedReceiver != mp.getSequence().getCurrentReceiver()) {
                throw new AssertionFailureException("Exception thrown by unexpected receiver.");
            }
        } catch (ResolutionException e) {
            throw new AssertionFailureException("Error verifying thrown exception.", e);
        } catch (RouteNotFoundException e) {
            throw new AssertionFailureException("Expected receiver not found.");
        }
    }

    /**
     * Prepare arguments for receiver reached by message in case of successful completion.
     *
     * The resulting object will contain configuration of wrapper object reading values required by assertions from message environment.
     * E.g. for assertions
     *
     * <pre>
     *     [
     *      {
     *          "name": "assert1",
     *          "value": "message/a",
     *          . . .
     *      },
     *      {
     *          "name": "assert2",
     *          "value": "context/c",
     *          . . .
     *      }
     *     ]
     * </pre>
     *
     * will be created the following object:
     *
     * <pre>
     *     {
     *         "wrapper": {
     *             "in_assert1": "message/a",
     *             "in_assert2": "context/c"
     *         }
     *     }
     * </pre>
     */
    private IObject prepareSuccessReceiverArguments(final IObject description)
            throws InvalidArgumentException, ReadValueException, ChangeValueException, ResolutionException {
        List<IObject> assertions = (List<IObject>) description.getValue(assertFieldName);

        IObject wrapperConfig = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

        for (IObject assertion : assertions) {
            String name = (String) assertion.getValue(assertNameFieldName);
            Object rule = (Object) assertion.getValue(assertValueFieldName);
            IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "in_" + name);
            wrapperConfig.setValue(fieldName, rule);
        }

        IObject args = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

        args.setValue(wrapperFieldName, wrapperConfig);

        return args;
    }

    private void verifyAssertions(final IObject description, final IMessageProcessor mp)
            throws AssertionFailureException, ReadValueException, InvalidArgumentException {
        List<IObject> assertions = (List<IObject>) description.getValue(assertFieldName);
        try {
            for (IObject assertion : assertions) {
                String name = (String) assertion.getValue(assertNameFieldName);
                String type = (String) assertion.getValue(assertTypeFieldName);
                IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), name);

                try {
                    Assertion assertion1 = IOC.resolve(Keys.getOrAdd("assertion of type " + type));

                    assertion1.check(assertion, mp.getEnvironment().getValue(fieldName));
                } catch (ResolutionException e) {
                    throw new AssertionFailureException(
                            MessageFormat.format("Could not resolve assertion \"{0}\" of type \"{1}\".", name, type), e);
                } catch (AssertionFailureException e) {
                    throw new AssertionFailureException(
                            MessageFormat.format("Assertion \"{0}\" failed.", name), e);
                }
            }
        } catch (ResolutionException e) {
            throw new AssertionFailureException("Could not check assertions.", e);
        }
    }
}
