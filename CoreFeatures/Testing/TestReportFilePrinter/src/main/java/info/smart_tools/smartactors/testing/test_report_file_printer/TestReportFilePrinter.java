package info.smart_tools.smartactors.testing.test_report_file_printer;

import info.smart_tools.smartactors.testing.interfaces.itest_report_printer.ITestReportPrinter;
import info.smart_tools.smartactors.testing.interfaces.itest_report_printer.exception.TestReportPrinterException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class TestReportFilePrinter implements ITestReportPrinter {
    private final String rootPath;

    /**
     * @param rootPath is a path to a directory where reports should be saved in.
     */
    public TestReportFilePrinter(final String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public void print(final String report, final String reportName) throws TestReportPrinterException {
        try {
            final Path path = Paths.get(rootPath, reportName);
            final InputStream is = new ByteArrayInputStream(report.getBytes());
            Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new TestReportPrinterException("Can't write report to file: " + e.getMessage(), e);
        }
    }
}
