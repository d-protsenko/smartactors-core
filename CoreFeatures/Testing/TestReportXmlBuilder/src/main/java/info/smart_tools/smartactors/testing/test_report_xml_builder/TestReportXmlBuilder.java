package info.smart_tools.smartactors.testing.test_report_xml_builder;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.testing.interfaces.itest_report_builder.ITestReportBuilder;
import info.smart_tools.smartactors.testing.interfaces.itest_report_builder.exception.TestReportBuilderException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

/**
 * MVN surefire plugin compatible XML report {@see https://github.com/apache/maven-surefire/blob/master/maven-surefire-plugin/src/site/resources/xsd/surefire-test-report.xsd}
 */
public class TestReportXmlBuilder implements ITestReportBuilder {
    @Override
    public String build(final IObject testSuite) throws TestReportBuilderException {
        final StringWriter writer = new StringWriter();
        try {
            final JAXBContext jc = JAXBContext.newInstance(TestSuiteModel.class);
            final Marshaller marshaller = jc.createMarshaller();

            marshaller.marshal(convert(testSuite), writer);
            return writer.toString();
        } catch (Exception e) {
            throw new TestReportBuilderException("Can't generate XML report: " + e.getMessage(), e);
        }
    }

    private TestSuiteModel convert(final IObject testSuite) {
        return new TestSuiteModel();
    }
}
