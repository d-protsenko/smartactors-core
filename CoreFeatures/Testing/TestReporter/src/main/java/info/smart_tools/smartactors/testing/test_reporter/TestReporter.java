package info.smart_tools.smartactors.testing.test_reporter;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.testing.interfaces.itest_reporter.ITestReporter;
import info.smart_tools.smartactors.testing.interfaces.itest_reporter.exception.TestReporterException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestReporter implements ITestReporter {
    private Long startTime;
    private final List<IObject> testCases;
    private final String featureName;

    private final IFieldName timestampField;
    private final IFieldName testTimeField;
    private final IFieldName featureNameField;
    private final IFieldName testCasesField;
    private final IFieldName failureField;
    private final IFieldName failuresCountField;
    private final IFieldName testsCountField;


    public TestReporter(final String featureName) throws ResolutionException {
        this.featureName = featureName;
        this.testTimeField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "testTime");
        this.featureNameField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "featureName");
        this.testCasesField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "testCases");
        this.failureField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "failure");
        this.failuresCountField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "failures");
        this.testsCountField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "tests");
        this.timestampField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "timestamp");
        this.testCases = new ArrayList<>();
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
            final IObject failure = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName()));
            failure.setValue(failureField, throwable);
            currentTestCase.setValue(failureField, failure);
        } catch (Exception e) {
            throw new TestReporterException(e.getMessage(), e);
        }
    }

    @Override
    public void make() throws TestReporterException {
        try {
            final IObject testSuite = buildSuite();

            // TODO: send to message bus
//            final String build = reportBuilder.build(testSuite);
//            reportPrinter.print(build, featureName);

        } catch (ReadValueException | InvalidArgumentException | ChangeValueException | ResolutionException e) {
            throw new TestReporterException("Can't build report: " + e.getMessage(), e);
        }
    }

    private IObject buildSuite() throws ReadValueException, InvalidArgumentException, ChangeValueException, ResolutionException {
        int testsCount = testCases.size();
        int failuresCount = 0;
        for (IObject testCase : testCases) {
            final Object failure = testCase.getValue(failureField);
            if (failure != null) failuresCount++;
        }
        final IObject testSuite = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName()));
        testSuite.setValue(featureNameField, featureName);
        testSuite.setValue(testCasesField, testCases);
        testSuite.setValue(failuresCountField, failuresCount);
        testSuite.setValue(testsCountField, testsCount);
        testSuite.setValue(timestampField, new Date().getTime());
        return testSuite;
    }
}
