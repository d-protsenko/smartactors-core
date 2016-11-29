package info.smart_tools.smartactors.testing.test_report_xml_builder;

import javax.xml.bind.annotation.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@XmlRootElement(name = "testsuite")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
final class TestSuiteModel {
    private String name;
    private Long tests;
    private Long skipped;
    private Long errors;
    private Long failures;
    private Long timestamp;
    private Long time;
    private List<TestCaseModel> testCaseModels;

    private final DateFormat format = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss");

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    @XmlAttribute(name = "tests")
    public Long getTests() {
        return tests;
    }

    @XmlAttribute(name = "skipped")
    public Long getSkipped() {
        return skipped;
    }

    @XmlAttribute(name = "errors")
    public Long getErrors() {
        return errors;
    }

    @XmlAttribute(name = "failures")
    public Long getFailures() {
        return failures;
    }

    @XmlAttribute(name = "timestamp")
    public String getTimestamp() {
        return format.format(new Date(timestamp));
    }

    @XmlAttribute(name = "time")
    public Long getTime() {
        return time;
    }

    @XmlElement(name = "testcase")
    public List<TestCaseModel> getTestCaseModels() {
        return testCaseModels;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setTests(final Long tests) {
        this.tests = tests;
    }

    public void setSkipped(final Long skipped) {
        this.skipped = skipped;
    }

    public void setErrors(final Long errors) {
        this.errors = errors;
    }

    public void setTimestamp(final Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTime(final Long time) {
        this.time = time;
    }

    public void setTestCaseModels(final List<TestCaseModel> testCaseModels) {
        this.testCaseModels = testCaseModels;
    }

    public void setFailures(final Long failures) {
        this.failures = failures;
    }
}
