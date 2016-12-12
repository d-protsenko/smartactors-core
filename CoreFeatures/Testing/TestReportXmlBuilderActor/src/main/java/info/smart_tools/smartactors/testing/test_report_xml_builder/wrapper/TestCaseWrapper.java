package info.smart_tools.smartactors.testing.test_report_xml_builder.wrapper;

import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

public interface TestCaseWrapper {
    String getName() throws ReadValueException;
    Throwable getFailure() throws ReadValueException;
    Long getTime() throws ReadValueException;
}
