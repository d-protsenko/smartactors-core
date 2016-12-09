package info.smart_tools.smartactors.testing.test_report_file_printer;

import info.smart_tools.smartactors.testing.interfaces.itest_report_printer.exception.TestReportPrinterException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestReportFilePrinterTest {
    @Test
    public void Should_work() throws TestReportPrinterException, IOException {
        final Path tempFile = Files.createTempFile("SUPER_TEST_REPORT", ".txt");

        String rootPath = tempFile.getParent().toString();
        String fileName = tempFile.getFileName().toString();

        final TestReportFilePrinter printer = new TestReportFilePrinter(rootPath);
        printer.print("it's a report", fileName);

        Files.exists(Paths.get(rootPath, fileName));
        final byte[] bytes = Files.readAllBytes(Paths.get(rootPath, fileName));
        Assert.assertEquals(new String(bytes), "it's a report");
    }
}
