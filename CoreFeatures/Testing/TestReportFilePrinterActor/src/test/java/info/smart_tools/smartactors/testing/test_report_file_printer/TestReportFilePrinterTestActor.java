package info.smart_tools.smartactors.testing.test_report_file_printer;

import info.smart_tools.smartactors.testing.test_report_file_printer.wrapper.PrintWrapper;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestReportFilePrinterTestActor {
    @Test
    public void Should_work() throws Exception {
        final Path tempFile = Files.createTempFile("SUPER_TEST_REPORT", ".txt");

        String rootPath = tempFile.getParent().toString();
        String fileName = tempFile.getFileName().toString();

        final TestReportFilePrinterActor printer = new TestReportFilePrinterActor(rootPath);
        PrintWrapper wrapper = mock(PrintWrapper.class);
        when(wrapper.getReport()).thenReturn("it's a report");
        when(wrapper.getReportName()).thenReturn(fileName);

        printer.print(wrapper);

        Files.exists(Paths.get(rootPath, fileName));
        final byte[] bytes = Files.readAllBytes(Paths.get(rootPath, fileName));
        Assert.assertEquals(new String(bytes), "it's a report");
    }
}
