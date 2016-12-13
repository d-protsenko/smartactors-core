package info.smart_tools.smartactors.testing.interfaces.itest_reporter;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.testing.interfaces.itest_reporter.exception.TestReporterException;

/**
 * Interface to fill test metrics and to report.
 */
public interface ITestReporter {
    /**
     * Before action. This method could start gathering metrics from the system and parse testInfo.
     *
     * @param testInfo is all data about test case. Take a look into feature's test description.
     */
    void beforeTest(final IObject testInfo) throws TestReporterException;

    /**
     * Mark that test case completed, may be exceptionally or not.
     *
     * @param throwable is exception that test may throw.
     */
    void afterTest(final Throwable throwable) throws TestReporterException;

    /**
     * This method build report. What exactly this method do is hidden in the realisation.
     * Supposed that implementation build string representation of the report in a format of xml or json or plain text.
     * Then it prints result in some output - System.our, Database, file, etc...
     *
     * @throws TestReporterException may be thrown on some error during building and sending report.
     */
    void make(final String name, final Object chainName) throws TestReporterException;
}
