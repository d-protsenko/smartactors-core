package info.smart_tools.smartactors.testing.test_report_xml_builder;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.testing.interfaces.itest_report_builder.ITestReportBuilder;
import info.smart_tools.smartactors.testing.interfaces.itest_report_builder.exception.TestReportBuilderException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * MVN surefire plugin compatible XML report {@see https://github.com/apache/maven-surefire/blob/master/maven-surefire-plugin/src/site/resources/xsd/surefire-test-report.xsd}
 */
public class TestReportXmlBuilder implements ITestReportBuilder {
    private final IField featureNameField;
    private final IField timestampField;
    private final IField testCountField;
    private final IField testFailuresCountField;
    private final IField testCasesField;
    private final IField testCaseNameField;
    private final IField testCaseFailureField;
    private final IField testTimeField;

    public TestReportXmlBuilder() throws ResolutionException {
        featureNameField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IField.class.getCanonicalName()), "featureName");
        timestampField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IField.class.getCanonicalName()), "timestamp");
        testCasesField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IField.class.getCanonicalName()), "testCases");
        testCountField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IField.class.getCanonicalName()), "tests");
        testFailuresCountField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IField.class.getCanonicalName()), "failures");
        testCaseNameField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IField.class.getCanonicalName()), "name");
        testCaseFailureField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IField.class.getCanonicalName()), "failure");
        testTimeField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IField.class.getCanonicalName()), "time");
    }


    @Override
    public String build(final IObject testSuite) throws TestReportBuilderException {
        final StringWriter writer = new StringWriter();
        try {
            final JAXBContext jc = JAXBContext.newInstance(TestSuiteModel.class);
            final Marshaller marshaller = jc.createMarshaller();

            marshaller.marshal(convertSuite(testSuite), writer);
            return writer.toString();
        } catch (Exception e) {
            throw new TestReportBuilderException("Can't generate XML report: " + e.getMessage(), e);
        }
    }

    private TestSuiteModel convertSuite(final IObject testSuite) throws ReadValueException, InvalidArgumentException {
        final TestSuiteModel suiteModel = new TestSuiteModel();
        suiteModel.setName(featureNameField.in(testSuite));
        suiteModel.setTests(testCountField.in(testSuite));
        suiteModel.setFailures(testFailuresCountField.in(testSuite));
        suiteModel.setErrors(0L);
        suiteModel.setSkipped(0L);
        suiteModel.setTimestamp(timestampField.in(testSuite));
        suiteModel.setTime(testTimeField.in(testSuite));
        final List<IObject> testCases = testCasesField.in(testSuite);
        if (testCases != null) {
            for (IObject tc : testCases) {
                suiteModel.getTestCaseModels().add(convertCase(tc));
            }
        }
        return suiteModel;
    }

    private TestCaseModel convertCase(final IObject testCase) throws ReadValueException, InvalidArgumentException {
        final TestCaseModel model = new TestCaseModel();
        model.setName(testCaseNameField.in(testCase));
        final Throwable throwable = testCaseFailureField.in(testCase);
        if (throwable != null) {
            model.setFailure(convertFailure(throwable));
        }
        model.setTime(testTimeField.in(testCase));
        return model;
    }

    private TestFailureModel convertFailure(final Throwable throwable) {
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
