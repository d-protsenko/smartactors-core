package info.smart_tools.smartactors.testing.test_report_text_builder;

import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.testing.test_report_text_builder.exception.BuildTextReportActorException;
import info.smart_tools.smartactors.testing.test_report_text_builder.wrapper.TestCaseWrapper;
import info.smart_tools.smartactors.testing.test_report_text_builder.wrapper.TestSuiteWrapper;

public class TestReportTextBuilderActor {

    public void build(final TestSuiteWrapper suiteWrapper) throws BuildTextReportActorException {
        try {
            final StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(String.format(
                    "Test set: %s\nTest run: %s, Failures: %s, Time elapsed: %s sec\n",
                    suiteWrapper.getFeatureName(),
                    suiteWrapper.getTests(),
                    suiteWrapper.getFailures(),
                    suiteWrapper.getTime()
            ));

            if (suiteWrapper.getTestCases() != null) {
                for (TestCaseWrapper tc : suiteWrapper.getTestCases()) {
                    if (tc.getFailure() != null) {
                        stringBuilder.append(String.format(
                                "\tFAIL!! TestCase %s. Time elapsed: %s sec. Throwable: %s.\n",
                                tc.getName(),
                                tc.getTime(),
                                tc.getFailure().getMessage()
                        ));
                    } else {
                        stringBuilder.append(String.format(
                                "\tSUCCESS!! TestCase %s. Time elapsed: %s sec.\n",
                                tc.getName(),
                                tc.getTime()
                        ));
                    }
                }
            }

            suiteWrapper.setReport(stringBuilder.toString());
        } catch (ReadValueException e) {
            throw new BuildTextReportActorException("Can't read values from wrapper: " + e.getMessage(), e);
        } catch (ChangeValueException e) {
            throw new BuildTextReportActorException("Can't write report into wrapper: " + e.getMessage(), e);
        }
    }
}
