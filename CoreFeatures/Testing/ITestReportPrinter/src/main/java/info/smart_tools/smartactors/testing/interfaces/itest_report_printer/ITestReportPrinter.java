package info.smart_tools.smartactors.testing.interfaces.itest_report_printer;

import info.smart_tools.smartactors.testing.interfaces.itest_report_printer.exception.TestReportPrinterException;

/**
 * This interface define the method how system should print a report - print to file, to console out...
 * * The file path, network URI, etc.. must be hidden in the realisation.
 */
public interface ITestReportPrinter {
    /**
     * Print a report.
     *
     * @param report     is a report body
     * @param reportName is a name of the report. May be useful in case of generating filename.
     *                   TODO: may be we should use InputStream?
     */
    void print(final String report, final String reportName) throws TestReportPrinterException;
}

