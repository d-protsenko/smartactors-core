package info.smart_tools.smartactors.testing.interfaces.itest_report_builder.exception;

public class TestReportBuilderException extends Exception {
    public TestReportBuilderException() {
    }

    public TestReportBuilderException(final String message) {
        super(message);
    }

    public TestReportBuilderException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TestReportBuilderException(final Throwable cause) {
        super(cause);
    }
}
