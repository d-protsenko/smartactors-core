package info.smart_tools.smartactors.testing.test_report_file_printer.wrapper;

import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

public interface PrintWrapper {
    String getReport() throws ReadValueException;

    String getReportName() throws ReadValueException;
}
