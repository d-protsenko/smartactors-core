package info.smart_tools.smartactors.testing.test_report_xml_builder.wrapper;

import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

import java.util.List;

public interface TestSuiteWrapper {
    String getFeatureName() throws ReadValueException;
    Long getTimestamp() throws ReadValueException;
    Long getTime() throws ReadValueException;
    Integer getTests() throws ReadValueException;
    Integer getFailures() throws ReadValueException;
    List<TestCaseWrapper> getTestCases() throws ReadValueException;

    void setReport(String report) throws ChangeValueException;
}
