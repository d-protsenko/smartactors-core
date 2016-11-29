package info.smart_tools.smartactors.testing.test_reporter;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.testing.interfaces.itest_report_builder.ITestReportBuilder;
import info.smart_tools.smartactors.testing.interfaces.itest_report_builder.exception.TestReportBuilderException;
import info.smart_tools.smartactors.testing.interfaces.itest_report_printer.ITestReportPrinter;
import info.smart_tools.smartactors.testing.interfaces.itest_report_printer.exception.TestReportPrinterException;
import info.smart_tools.smartactors.testing.interfaces.itest_reporter.ITestReporter;
import info.smart_tools.smartactors.testing.interfaces.itest_reporter.exception.TestReporterException;

import java.util.List;

public class TestReporter implements ITestReporter {
    private Long startTime;
    private List<IObject> testCases;
    private final String featureName;

    private final ITestReportPrinter reportPrinter;
    private final ITestReportBuilder reportBuilder;

    private final IFieldName testTimeField;
    private final IFieldName featureNameField;
    private final IFieldName testCasesField;

    public TestReporter(final String featureName, final ITestReportPrinter reportPrinter, final ITestReportBuilder reportBuilder) throws ResolutionException {
        this.featureName = featureName;
        this.reportPrinter = reportPrinter;
        this.reportBuilder = reportBuilder;
        this.testTimeField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "testTime");
        this.featureNameField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "featureName");
        this.testCasesField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "testCases");
    }

    @Override
    public void beforeTest(final IObject testInfo) {
        startTime = System.currentTimeMillis();
        testCases.add(testInfo);
    }

    @Override
    public void afterTest(final Throwable throwable) throws TestReporterException {
        try {
            long delay = System.currentTimeMillis() - startTime;
            final IObject currentTestCase = testCases.get(testCases.size() - 1);
            currentTestCase.setValue(testTimeField, delay);
        } catch (Exception e) {
            throw new TestReporterException(e.getMessage(), e);
        }
    }

    @Override
    public void make() throws TestReporterException {
        try {
            final IObject testSuite = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName()));
            testSuite.setValue(featureNameField, featureName);
            testSuite.setValue(testCasesField, testCases);
            final String build = reportBuilder.build(testSuite);
            reportPrinter.print(build, featureName);
        } catch (InvalidArgumentException | ChangeValueException | ResolutionException | TestReportPrinterException | TestReportBuilderException e) {
            throw new TestReporterException("Can't build report: " + e.getMessage(), e);
        }
    }
}
