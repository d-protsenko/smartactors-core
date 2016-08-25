package info.smart_tools.smartactors.plugin.test_checkers;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.initialization_exception.InitializationException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.test.iresult_checker.IResultChecker;
import info.smart_tools.smartactors.test.test_checkers.AssertionChecker;
import info.smart_tools.smartactors.test.test_checkers.ExceptionInterceptor;

import java.util.List;

/**
 * Plugin registers strategies that returns new instances of {@link IResultChecker}.
 */
public class TestCheckersPlugin implements IPlugin {

    /** Local storage for instance of {@link IBootstrap}*/
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor with single argument
     * @param bootstrap instance of {@link IBootstrap}
     * @throws InvalidArgumentException if any errors occurred
     */
    public TestCheckersPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap)
            throws InvalidArgumentException {
        if (null == bootstrap) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("TestCheckers");
            item
                    .after("IOC")
                    .after("iobject")
                    .after("IFieldNamePlugin")
                    .after("testing_assertions")
                    .after("router")
                    .process(
                            () -> {
                                try {
                                    IOC.register(
                                            IOC.resolve(IOC.getKeyForKeyStorage(), IResultChecker.class.getCanonicalName() + "#assert"),
                                            new ApplyFunctionToArgumentsStrategy((args) -> {
                                                try {
                                                    return new AssertionChecker((List<IObject>) args[0]);
                                                } catch (InitializationException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            })
                                    );
                                    IOC.register(
                                            IOC.resolve(IOC.getKeyForKeyStorage(), IResultChecker.class.getCanonicalName() + "#intercept"),
                                            new ApplyFunctionToArgumentsStrategy((args) -> {
                                                try {
                                                    return new ExceptionInterceptor((IObject) args[0]);
                                                } catch (InitializationException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            })
                                    );
                                } catch (ResolutionException e) {
                                    throw new ActionExecuteException("TestCheckers plugin can't load: can't get ioc key.", e);
                                } catch (InvalidArgumentException e) {
                                    throw new ActionExecuteException("TestCheckers plugin can't load: can't create strategy.", e);
                                } catch (RegistrationException e) {
                                    throw new ActionExecuteException("TestCheckers plugin can't load: can't register new strategy.", e);
                                }
                            }
                        );
            this.bootstrap.add(item);
        } catch (Throwable e) {
            throw new PluginException("Could not load 'TestCheckers' plugin'", e);
        }
    }
}
