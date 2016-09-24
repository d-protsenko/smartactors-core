package info.smart_tools.smartactors.core.chain_testing;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.core.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.core.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.ienvironment_handler.exception.EnvironmentHandleException;
import info.smart_tools.smartactors.core.ienvironment_handler.exception.RequestHandlerInternalException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.initialization_exception.InitializationException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.test.itest_runner.ITestRunner;
import info.smart_tools.smartactors.test.itest_runner.exception.TestExecutionException;

/**
 * Runs tests on receiver chains.
 *
 * If the chain should be completed without exceptions then the description should have the following format:
 *
 * <pre>
 *     {
 *         "name": "Unique test name",                      // Unique name of the test
 *         "entryPoint": "chain",                    // The entry point (chain, httpEndpoint, etc)
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
 *         "entryPoint": "chain",
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
public class ChainTestRunner implements ITestRunner {

    private final IFieldName chainNameFieldName;

    /**
     * Default constructor.
     * @throws InitializationException if instance creation was failed
     */
    public ChainTestRunner()
            throws InitializationException {
        try {
            this.chainNameFieldName = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "chainName"
            );
        } catch (ResolutionException e) {
            throw new InitializationException("Could not create new instance of ChainTestRunner.", e);
        }
    }

    /**
     * Run a test described by the given {@link IObject}.
     *
     * @param description    description of the test to run
     * @param callback       callback to call when test is completed (with {@code null} as the only argument in case of success or with
     *                       exception describing failure reasons in case of failure)
     * @throws InvalidArgumentException if {@code description} is {@code null}
     * @throws InvalidArgumentException if {@code callback} is {@code null}
     * @throws TestExecutionException if test execution failed
     */
    public void runTest(final IObject description, final IAction<Throwable> callback)
            throws InvalidArgumentException, TestExecutionException {
        if (null == description) {
            throw new InvalidArgumentException("Description should not be null.");
        }
        if (null == callback) {
            throw new InvalidArgumentException("Callback should not be null.");
        }

        try {
            String chainName = (String) description.getValue(chainNameFieldName);
            Object chainId = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), "chain_id_from_map_name"), chainName
            );
            IChainStorage chainStorage = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IChainStorage.class.getCanonicalName())
            );
            IReceiverChain testedChain = chainStorage.resolve(chainId);

            IEnvironmentHandler handler = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), "test environment handler"));
            handler.handle(description, testedChain, callback);
        } catch (ResolutionException | ReadValueException | ChainNotFoundException |
                EnvironmentHandleException e) {
            throw new TestExecutionException(e);
        }
    }
}
