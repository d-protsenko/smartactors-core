package info.smart_tools.smartactors.core.chain_testing;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.test.itest_runner.ITestRunner;

/**
 * Runs tests on receiver chains.
 *
 * If the chain should be completed without exceptions then the description should have the following format:
 *
 * <pre>
 *     {
 *         "name": "Unique test name",                      // Unique name of the test
 *         "entryPoint": "httpEndpoint",                    // The entry point (chain, httpEndpoint, etc)
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
public class TestRunner implements ITestRunner {




    /**
     * Run a test described by the given {@link IObject}.
     *
     * @param description    description of the test to run
     * @param callback       callback to call when test is completed (with {@code null} as the only argument in case of success or with
     *                       exception describing failure reasons in case of failure)
     * @throws InvalidArgumentException if {@code description} is {@code null}
     * @throws InvalidArgumentException if {@code callback} is {@code null}
     */
    public void runTest(final IObject description, final IAction<Throwable> callback)
            throws InvalidArgumentException {

    }
}
