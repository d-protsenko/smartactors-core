package info.smart_tools.smartactors.testing.test_report_text_builder;

import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.testing.interfaces.itest_report_builder.ITestReportBuilder;
import info.smart_tools.smartactors.testing.interfaces.itest_report_builder.exception.TestReportBuilderException;

import java.util.List;

public class TestReportTextBuilder implements ITestReportBuilder {
    private final IField featureNameField;
    private final IField testCountField;
    private final IField testFailuresCountField;
    private final IField testCasesField;
    private final IField testCaseNameField;
    private final IField testCaseFailureField;
    private final IField testTimeField;

    public TestReportTextBuilder() throws ResolutionException {
        featureNameField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IField.class.getCanonicalName()), "featureName");
        testCasesField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IField.class.getCanonicalName()), "testCases");
        testCountField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IField.class.getCanonicalName()), "tests");
        testFailuresCountField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IField.class.getCanonicalName()), "failures");
        testCaseNameField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IField.class.getCanonicalName()), "name");
        testCaseFailureField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IField.class.getCanonicalName()), "failure");
        testTimeField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IField.class.getCanonicalName()), "time");
    }

    @Override
    public String build(final IObject testInfo) throws TestReportBuilderException {
        final StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append(String.format(
                    "Test set: %s\nTest run: %s, Failures: %s, Time elapsed: %s sec\n",
                    featureNameField.in(testInfo),
                    testCountField.in(testInfo),
                    testFailuresCountField.in(testInfo),
                    testTimeField.in(testInfo)
            ));

            final List<IObject> testCases = testCasesField.in(testInfo);
            if (testCases != null) {
                for (IObject tc : testCases) {
                    Throwable throwable = testCaseFailureField.in(tc);
                    String name = testCaseNameField.in(tc);
                    Long time = testTimeField.in(tc);
                    if (throwable != null) {
                        stringBuilder.append(String.format(
                                "\tFAIL!! TestCase %s. Time elapsed: %s sec. Throwable: %s.\n",
                                name,
                                time,
                                throwable.getMessage()
                        ));
                    } else {
                        stringBuilder.append(String.format(
                                "\tSUCCESS!! TestCase %s. Time elapsed: %s sec.\n",
                                name,
                                time));
                    }
                }
            }
        } catch (Exception e) {
            throw new TestReportBuilderException("Can't build text report: " + e.getMessage(), e);
        }

        return stringBuilder.toString();
    }
}
