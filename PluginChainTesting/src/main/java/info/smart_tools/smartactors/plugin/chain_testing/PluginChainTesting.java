package info.smart_tools.smartactors.plugin.chain_testing;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.chain_testing.ChainTestRunner;
import info.smart_tools.smartactors.core.chain_testing.HttpEndpointTestRunner;
import info.smart_tools.smartactors.core.chain_testing.section_strategy.TestsSectionStrategy;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iconfiguration_manager.IConfigurationManager;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.initialization_exception.InitializationException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.test.itest_runner.ITestRunner;
import info.smart_tools.smartactors.test.test_environment_handler.MainTestChain;
import info.smart_tools.smartactors.test.test_environment_handler.TestEnvironmentHandler;

/**
 * Plugin that registers strategy processing "tests" section of configuration and some related components.
 */
public class PluginChainTesting implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap the bootstrap
     */
    public PluginChainTesting(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> testHandlerItem = new BootstrapItem("test environment handler");
            testHandlerItem
                    .after("IOC")
                    .after("test checkers")
                    .after("iobject")
                    .after("test assertions")
                    .process(
                            () -> {
                                try {
                                    IEnvironmentHandler testHandler = new TestEnvironmentHandler();
                                    IOC.register(
                                            IOC.resolve(IOC.getKeyForKeyStorage(), "test environment handler"),
                                            new SingletonStrategy(testHandler)
                                    );
                                } catch (InitializationException e) {
                                    throw new ActionExecuteException("Test environment handler plugin can't load: can't create new instance.", e);
                                } catch (ResolutionException e) {
                                    throw new ActionExecuteException("Test environment handler plugin can't load: can't get ioc key.", e);
                                } catch (InvalidArgumentException e) {
                                    throw new ActionExecuteException("Test environment handler plugin can't load: can't create strategy.", e);
                                } catch (RegistrationException e) {
                                    throw new ActionExecuteException("Test environment handler plugin can't load: can't register new strategy.", e);
                                }
                            }
                    );
            this.bootstrap.add(testHandlerItem);

            IBootstrapItem<String> runnerItem = new BootstrapItem("chain tests runner");
            runnerItem
                    .after("IOC")
                    .after("iobject")
                    .after("IFieldNamePlugin")
                    .after("test assertions")
                    .after("test environment handler")
                    .after("router")
                    .process(
                            () -> {
                                try {
                                    ITestRunner chainTestRunner = new ChainTestRunner();
                                    IOC.register(
                                            IOC.resolve(IOC.getKeyForKeyStorage(), ITestRunner.class.getCanonicalName() + "#chain"),
                                            new SingletonStrategy(chainTestRunner)
                                    );
                                    ITestRunner httpEndpointTestRunner = new HttpEndpointTestRunner();
                                    IOC.register(
                                            IOC.resolve(IOC.getKeyForKeyStorage(), ITestRunner.class.getCanonicalName() + "#httpEndpoint"),
                                            new SingletonStrategy(httpEndpointTestRunner));
                                } catch (ResolutionException e) {
                                    throw new ActionExecuteException("TestRunners plugin can't load: can't get ioc key.", e);
                                } catch (InvalidArgumentException e) {
                                    throw new ActionExecuteException("TestRunners plugin can't load: can't create strategy.", e);
                                } catch (RegistrationException e) {
                                    throw new ActionExecuteException("TestRunners plugin can't load: can't register new strategy.", e);
                                } catch (InitializationException e) {
                                    throw new ActionExecuteException("TestRunners plugin can't load: can't create instance of TestRunner.", e);
                                }
                            }
                    );
            this.bootstrap.add(runnerItem);

            IBootstrapItem<String> strategyItem = new BootstrapItem("config_section:tests");

            strategyItem
                    .after("config_section:executor")
                    .after("config_section:maps")
                    .after("config_section:objects")
                    .after("message_processor")
                    .after("message_processing_sequence")
                    .after("main_test_chain")
                    .after("chain tests runner")
                    .before("starter")
                    .process(() -> {
                        try {
                            IConfigurationManager configurationManager =
                                    IOC.resolve(Keys.getOrAdd(IConfigurationManager.class.getCanonicalName()));

                            configurationManager.addSectionStrategy(new TestsSectionStrategy());
                        } catch (ResolutionException | InvalidArgumentException e) {
                            throw new ActionExecuteException(e);
                        }
                    });

            bootstrap.add(strategyItem);

            IBootstrapItem<String> mainChainItem = new BootstrapItem("main_test_chain");

            mainChainItem
                    .after("IOC")
                    .process(() -> {
                        try {
                            IOC.register(Keys.getOrAdd(MainTestChain.class.getCanonicalName()),
                                    new CreateNewInstanceStrategy(args -> {
                                        try {
                                            return new MainTestChain((IReceiverChain) args[0], (IAction) args[1], (IObject) args[2]);
                                        } catch (InvalidArgumentException | InitializationException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }));
                        } catch (ResolutionException | RegistrationException | InvalidArgumentException e) {
                            throw new ActionExecuteException(e);
                        }
                    });

            bootstrap.add(mainChainItem);

        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
