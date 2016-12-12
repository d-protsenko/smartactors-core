package info.smart_tools.smartactors.testing.test_report_xml_builder;

import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.testing.test_report_xml_builder.exception.BuildXmlReportActorException;
import info.smart_tools.smartactors.testing.test_report_xml_builder.wrapper.TestCaseWrapper;
import info.smart_tools.smartactors.testing.test_report_xml_builder.wrapper.TestSuiteWrapper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * MVN surefire plugin compatible XML report {@see https://github.com/apache/maven-surefire/blob/master/maven-surefire-plugin/src/site/resources/xsd/surefire-test-report.xsd}
 */
public final class TestReportXmlBuilderActor {

    public void build(final TestSuiteWrapper suiteWrapper) throws BuildXmlReportActorException {
        try {
            final TestSuiteModel suiteModel = convertSuite(suiteWrapper);

            final StringWriter writer = new StringWriter();
            final JAXBContext jc = JAXBContext.newInstance(TestSuiteModel.class);
            final Marshaller marshaller = jc.createMarshaller();
            marshaller.marshal(suiteModel, writer);

            suiteWrapper.setReport(writer.toString());
        } catch (ReadValueException e) {
            throw new BuildXmlReportActorException("Can't read values from wrapper: " + e.getMessage(), e);
        } catch (JAXBException e) {
            throw new BuildXmlReportActorException("Can't build xml: " + e.getMessage(), e);
        } catch (ChangeValueException e) {
            throw new BuildXmlReportActorException("Can't write report into wrapper: " + e.getMessage(), e);
        }
    }

    private TestSuiteModel convertSuite(TestSuiteWrapper suiteWrapper) throws ReadValueException {
        final TestSuiteModel suiteModel = new TestSuiteModel();

        suiteModel.setName(suiteWrapper.getFeatureName());
        suiteModel.setTests(suiteWrapper.getTests());
        suiteModel.setFailures(suiteWrapper.getFailures());
        suiteModel.setErrors(0);
        suiteModel.setSkipped(0);
        suiteModel.setTimestamp(suiteWrapper.getTimestamp());
        suiteModel.setTime(suiteWrapper.getTime());
        suiteModel.setTestCaseModels(convertCases(suiteWrapper.getTestCases()));

        return suiteModel;
    }

    private List<TestCaseModel> convertCases(List<TestCaseWrapper> casesWrappers) throws ReadValueException {
        final List<TestCaseModel> models = new ArrayList<>();

        for (final TestCaseWrapper caseWrapper : casesWrappers) {
            final TestCaseModel caseModel = new TestCaseModel();
            caseModel.setTime(caseWrapper.getTime());
            caseModel.setName(caseWrapper.getName());
            caseModel.setFailure(convertFailure(caseWrapper.getFailure()));

            models.add(caseModel);
        }

        return models;
    }

    private TestFailureModel convertFailure(final Throwable throwable) {
        if (throwable == null) return null;

        final TestFailureModel failureModel = new TestFailureModel();
        failureModel.setType(throwable.getClass().getCanonicalName());
        failureModel.setMessage(throwable.getMessage());
        failureModel.setBody(getStackTrace(throwable));
        return failureModel;
    }

    private String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter writer = new PrintWriter(sw);
        throwable.printStackTrace(writer);
        return sw.toString();
    }
}
