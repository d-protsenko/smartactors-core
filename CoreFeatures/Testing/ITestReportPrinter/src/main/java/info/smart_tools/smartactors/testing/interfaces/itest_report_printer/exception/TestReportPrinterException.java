package info.smart_tools.smartactors.testing.interfaces.itest_report_printer.exception;

public class TestReportPrinterException extends Exception {
    public TestReportPrinterException() {
    }

    public TestReportPrinterException(final String message) {
        super(message);
    }

    public TestReportPrinterException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TestReportPrinterException(final Throwable cause) {
        super(cause);
    }
}
