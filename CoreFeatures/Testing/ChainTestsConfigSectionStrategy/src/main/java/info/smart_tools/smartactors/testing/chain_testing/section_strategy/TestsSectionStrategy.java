package info.smart_tools.smartactors.testing.chain_testing.section_strategy;

import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.testing.interfaces.itest_reporter.ITestReporter;
import info.smart_tools.smartactors.testing.interfaces.itest_reporter.exception.TestReporterException;
import info.smart_tools.smartactors.testing.interfaces.itest_runner.ITestRunner;
import info.smart_tools.smartactors.testing.interfaces.itest_runner.exception.TestExecutionException;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Strategy processing "tests" section of configuration.
 * <p>
 * Requires thread pool initialized and task dispatcher running to execute tests.
 */
public class TestsSectionStrategy implements ISectionStrategy {
    private IFieldName name;
    //    private ITestRunner runner;
    private IFieldName testRunnerName;
    private IFieldName featureNameField;
    private IFieldName testReporterChainName;

    /**
     * The constructor.
     *
     * @throws ResolutionException if fails to resolve any dependencies
     */
    public TestsSectionStrategy()
            throws ResolutionException {
        this.name = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "tests"
        );
        this.testRunnerName = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "entryPoint"
        );
        this.featureNameField = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "featureName");
        this.testReporterChainName = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "testReporterChainName");
    }

    @Override
    public void onLoadConfig(final IObject config)
            throws ConfigurationProcessingException {
        try {
            System.out.println("--------------------------------- Run testing ---------------------------------");
            IFieldName testNameFieldName = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "name"
            );
            List<IObject> tests = (List<IObject>) config.getValue(name);
            CyclicBarrier barrier = new CyclicBarrier(2);
            AtomicReference<Throwable> eRef = new AtomicReference<>(null);

            ITestReporter testReporter = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), ITestReporter.class.getCanonicalName()));

            for (IObject testDesc : tests) {
                System.out.println("Run test '" + testDesc.getValue(testNameFieldName) + "'.");
                testReporter.beforeTest(testDesc);
                ITestRunner runner = IOC.resolve(
                        IOC.resolve(
                                IOC.getKeyForKeyStorage(),
                                ITestRunner.class.getCanonicalName() + "#" + testDesc.getValue(this.testRunnerName))
                );
                runner.runTest(testDesc, err -> {
                    eRef.set(err);
                    try {
                        barrier.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (BrokenBarrierException e) {
                        throw new ActionExecuteException(e);
                    }
                });

                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                testReporter.afterTest(eRef.get());

                if (null != eRef.get()) {
                    throw new ConfigurationProcessingException("Test failed.", eRef.get());
                } else {
                    System.out.println("Test '" + testDesc.getValue(testNameFieldName) + "' is successful.");
                }
            }
            System.out.println("--------------------------------- Testing completed ---------------------------------");
            testReporter.make((String) config.getValue(featureNameField), config.getValue(testReporterChainName));
        } catch (ReadValueException | ResolutionException | InvalidArgumentException | BrokenBarrierException | ClassCastException e) {
            throw new ConfigurationProcessingException(e);
        } catch (TestExecutionException e) {
            throw new ConfigurationProcessingException("Could not start test.", e);
        } catch (TestReporterException e) {
            throw new ConfigurationProcessingException("Could not make test report.", e);
        }
    }

    @Override
    public IFieldName getSectionName() {
        return name;
    }
}
