package info.smart_tools.smartactors.testing.test_report_xml_builder;

import info.smart_tools.smartactors.testing.test_report_xml_builder.wrapper.TestCaseWrapper;
import info.smart_tools.smartactors.testing.test_report_xml_builder.wrapper.TestSuiteWrapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class TestReportXmlBuilderActorTest {
    private TestReportXmlBuilderActor reportBuilder;

    @Before
    public void setup() {
        reportBuilder = new TestReportXmlBuilderActor();
    }

    @Test
    public void Should_BuildXML() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        TestSuiteWrapper suite = buildTestSuite();
        reportBuilder.build(suite);
        verify(suite).setReport(captor.capture());
        String report = captor.getValue();

        System.out.println(report);
        InputStream xml = new ByteArrayInputStream(report.getBytes());
        InputStream xsd = getClass().getResourceAsStream("/surefire-test-report.xsd");
        assertTrue(validateAgainstXSD(xml, xsd));
    }

    private TestSuiteWrapper buildTestSuite() throws Exception {
        TestSuiteWrapper suite = mock(TestSuiteWrapper.class);
        final List<TestCaseWrapper> testCases = buildTestCases();
        when(suite.getFeatureName()).thenReturn("TestReportXmlBuilderActorTest");
        when(suite.getTimestamp()).thenReturn(new Date().getTime());
        when(suite.getTime()).thenReturn(10L);
        when(suite.getTests()).thenReturn(2);
        when(suite.getFailures()).thenReturn(1);
        when(suite.getTestCases()).thenReturn(testCases);
        return suite;
    }

    private List<TestCaseWrapper> buildTestCases() throws Exception {
        List<TestCaseWrapper> testCases = new ArrayList<>();

        testCases.add(buildSuccessfulTestCase());
        testCases.add(buildFailedTestCase());

        return testCases;
    }

    private TestCaseWrapper buildSuccessfulTestCase() throws Exception {
        TestCaseWrapper testCase = mock(TestCaseWrapper.class);
        when(testCase.getName()).thenReturn("Should_BuildXML");
        when(testCase.getTime()).thenReturn(4L);
        return testCase;
    }

    private TestCaseWrapper buildFailedTestCase() throws Exception {
        TestCaseWrapper testCase = mock(TestCaseWrapper.class);
        when(testCase.getName()).thenReturn("Should_Fail");
        when(testCase.getTime()).thenReturn(6L);
        when(testCase.getFailure()).thenReturn(new Exception("It's a failure"));
        return testCase;
    }

    private boolean validateAgainstXSD(InputStream xml, InputStream xsd) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsd));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
