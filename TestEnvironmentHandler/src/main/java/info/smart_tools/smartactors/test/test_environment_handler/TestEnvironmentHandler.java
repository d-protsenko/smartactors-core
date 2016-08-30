package info.smart_tools.smartactors.test.test_environment_handler;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.ienvironment_handler.exception.EnvironmentHandleException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.initialization_exception.InitializationException;
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
import info.smart_tools.smartactors.test.iresult_checker.IResultChecker;
import info.smart_tools.smartactors.test.test_environment_handler.exception.InvalidTestDescriptionException;

import java.util.List;

/**
 * Implementation of {@link IEnvironmentHandler}.
 * This implementation prepares and start processing test chain
 */
public class TestEnvironmentHandler implements IEnvironmentHandler {

    private static final int STACK_DEPTH = 5;

    private final IFieldName environmentFieldName;
    private final IFieldName messageFieldName;
    private final IFieldName contextFieldName;


    /**
     * The constructor.
     *
     * @throws InitializationException if creation of {@link TestEnvironmentHandler} has been failed.
     */
    public TestEnvironmentHandler()
            throws InitializationException {
        try {
            environmentFieldName = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "environment"
            );
            messageFieldName = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "message"
            );
            contextFieldName = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "context"
            );
        } catch (ResolutionException e) {
            throw new InitializationException("Could not create new instance of TestEnvironmentHandler.", e);
        }
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

            IResultChecker checker = createChecker(environment);

            IMessageProcessor[] fmp = new IMessageProcessor[1];

            IObject environmentDesc = (IObject) environment.getValue(environmentFieldName);

            IObject message = (IObject) environmentDesc.getValue(messageFieldName);
            IObject context = (IObject) environmentDesc.getValue(contextFieldName);

            IAction<Throwable> completionCallback = exc -> {
                try {
                    checker.check(fmp[0], exc);

                    callback.execute(null);
                } catch (Exception e) {
                    callback.execute(e);
                }
            };

            MainTestChain mainTestChain = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), MainTestChain.class.getCanonicalName()),
                    receiverChain,
                    completionCallback,
                    checker.getSuccessfulReceiverArguments()
            );

            IMessageProcessingSequence sequence = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IMessageProcessingSequence.class.getCanonicalName()),
                    STACK_DEPTH, mainTestChain
            );



            IQueue<ITask> taskQueue = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), "task_queue")
            );

            IMessageProcessor mp = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IMessageProcessor.class.getCanonicalName()), taskQueue, sequence
            );

            fmp[0] = mp;
            mp.process(message, context);

        } catch (ReadValueException | ResolutionException | ChangeValueException | InitializationException e) {
            throw new EnvironmentHandleException(e);
        } catch (ClassCastException e) {
            throw new EnvironmentHandleException("Could not cast value to required type.", e);
        }
    }

    private IResultChecker createChecker(final IObject description)
            throws InitializationException {
        try {
            IFieldName interceptFieldName = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "intercept"
            );
            IFieldName assertFieldName = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "assert"
            );

            List<IObject> assertions = (List<IObject>) description.getValue(assertFieldName);
            IObject intercept = (IObject) description.getValue(interceptFieldName);

            if ((null == assertions) && (null == intercept)) {
                throw new InvalidTestDescriptionException("None of \"assert\" and \"intercept\" sections are present in test description.");
            }

            if ((null != assertions) && (null != intercept)) {
                throw new InvalidTestDescriptionException("Both \"assert\" and \"intercept\" sections are present in test description.");
            }

            if (null != assertions) {
                return (IResultChecker) IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IResultChecker.class.getCanonicalName() + "#assert"), assertions
                );
            } else {
                return (IResultChecker) IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IResultChecker.class.getCanonicalName() + "#intercept"), intercept
                );
            }
        } catch (ResolutionException | InvalidTestDescriptionException | InvalidArgumentException | ReadValueException e) {
            throw new InitializationException(e);
        }
    }
}
