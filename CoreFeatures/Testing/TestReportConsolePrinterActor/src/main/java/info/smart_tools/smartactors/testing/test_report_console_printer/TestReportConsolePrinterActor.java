package info.smart_tools.smartactors.testing.test_report_console_printer;

import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.testing.test_report_console_printer.exception.ConsolePrintReportActorException;
import info.smart_tools.smartactors.testing.test_report_console_printer.wrapper.PrintWrapper;

/**
 * Dummy console printer.
 */
public class TestReportConsolePrinterActor {
    public void print(PrintWrapper wrapper) throws ConsolePrintReportActorException {
        try {
            System.out.println(String.format("Test report %s\n%s", wrapper.getReportName(), wrapper.getReport()));
        } catch (ReadValueException e) {
            throw new ConsolePrintReportActorException("Can't read values from wrapper: " + e.getMessage(), e);
        }
    }
}
