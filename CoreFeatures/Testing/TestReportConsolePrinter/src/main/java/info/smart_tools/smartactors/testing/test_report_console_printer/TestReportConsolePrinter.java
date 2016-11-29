package info.smart_tools.smartactors.testing.test_report_console_printer;

import info.smart_tools.smartactors.testing.interfaces.itest_report_printer.ITestReportPrinter;
import info.smart_tools.smartactors.testing.interfaces.itest_report_printer.exception.TestReportPrinterException;

/**
 * Dummy console printer.
 * It's default implementation, that user can change in his project.
 */
public class TestReportConsolePrinter implements ITestReportPrinter {
    @Override
    public void print(final String report, final String reportName) throws TestReportPrinterException {
        System.out.println(String.format("Test report %s\n%s", reportName, report));
    }
}
