package info.smart_tools.smartactors.testing.test_report_text_builder;

import info.smart_tools.smartactors.testing.test_report_text_builder.wrapper.TestCaseWrapper;
import info.smart_tools.smartactors.testing.test_report_text_builder.wrapper.TestSuiteWrapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class TestReportTextBuilderTestActor {
    private TestReportTextBuilderActor reportBuilder;

    @Before
    public void setup() {
        reportBuilder = new TestReportTextBuilderActor();
    }

    @Test
    public void Should_BuildText() throws Exception {
        final TestSuiteWrapper mock = buildTestSuite();
        reportBuilder.build(mock);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mock).setReport(captor.capture());
        String report = captor.getValue();
        System.out.println(report);
        assertNotNull(report);
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
}
