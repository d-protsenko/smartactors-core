package info.smart_tools.smartactors.testing.test_report_text_builder;

import java.util.List;

interface ITestSuite {
    String getName();

    Long getTime();

    Long getTestsCount();

    Long getErrorsCount();

    Long getFailuresCount();

    Long getSkippedCount();

    List<ITestCase> getTestCases();
}
