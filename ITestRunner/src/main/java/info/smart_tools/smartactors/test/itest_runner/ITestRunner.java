package info.smart_tools.smartactors.test.itest_runner;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.test.itest_runner.exception.InvalidTestDescriptionException;
import info.smart_tools.smartactors.test.itest_runner.exception.TestStartupException;

/**
 * Interface for realize different test runners.
 */
public interface ITestRunner {

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
    void runTest(final IObject description, final IAction<Throwable> callback)
            throws InvalidArgumentException, InvalidTestDescriptionException, TestStartupException;
}
