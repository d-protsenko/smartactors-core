package info.smart_tools.smartactors.testing.test_report_text_builder;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.testing.interfaces.itest_report_builder.ITestReportBuilder;
import info.smart_tools.smartactors.testing.interfaces.itest_report_builder.exception.TestReportBuilderException;

public class TestReportTextBuilder implements ITestReportBuilder {
    @Override
    public String build(final IObject testInfo) throws TestReportBuilderException {
        final StringBuilder stringBuilder = new StringBuilder();
//        ITestSuite testSuite = convert();
//        stringBuilder.append(String.format(
//                "Test '%s' completed in %s seconds. All: %s, Error: %s, Failed: %s, Skipped: %s.\n",
//                testSuite.getName(),
//                testSuite.getTime(),
//                testSuite.getTestsCount(),
//                testSuite.getErrorsCount(),
//                testSuite.getFailuresCount(),
//                testSuite.getSkippedCount()));
//
//        testSuite.getTestCases().forEach(test -> {
//            if (test.getThrowable() != null) {
//                stringBuilder.append(String.format("\tFAIL!! TestCase %s in %s seconds with %s.\n", test.getName(), test.getTime(), test.getThrowable().getMessage()));
//            } else {
//                stringBuilder.append(String.format("\tSUCCESS!! TestCase %s in %s seconds.\n", test.getName(), test.getTime()));
//            }
//        });
//
        return stringBuilder.toString();
    }

    private ITestSuite convert() {
        return null;
    }
}
