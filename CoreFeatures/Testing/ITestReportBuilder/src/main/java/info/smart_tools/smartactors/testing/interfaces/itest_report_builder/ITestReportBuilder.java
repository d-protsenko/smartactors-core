package info.smart_tools.smartactors.testing.interfaces.itest_report_builder;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.testing.interfaces.itest_report_builder.exception.TestReportBuilderException;

/**
 * It's an interface of report builder. Concrete realisations can build json, xml, txt, etc...
 */
public interface ITestReportBuilder {
    /**
     * Build-method hides a realisation of building report.
     * @param testSuite is model that represent the test's result.
     * @return string of report
     * TODO: may be it would be better to use InputStream?
     */
    String build(final IObject testSuite) throws TestReportBuilderException;
}
