package info.smart_tools.smartactors.testing.test_report_text_builder;

interface ITestCase {
    Throwable getThrowable();
    String getName();
    Long getTime();
}
