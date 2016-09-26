package info.smart_tools.smartactors.plugin.chain_testing.assertions;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.test.test_assertions.EqualAssertion;
import info.smart_tools.smartactors.test.test_assertions.NotEqualAssertion;

/**
 * Plugin registering some assertions for chain tests.
 */
public class PluginChainTestingAssertions implements IPlugin {

    /** Local storage for instance of {@link IBootstrap}*/
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor with single argument
     * @param bootstrap instance of {@link IBootstrap}
     * @throws InvalidArgumentException if any errors occurred
     */
    public PluginChainTestingAssertions(final IBootstrap<IBootstrapItem<String>> bootstrap)
            throws InvalidArgumentException {
        if (null == bootstrap) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> assertionsItem = new BootstrapItem("test assertions");

            assertionsItem
                    .after("IOC")
                    .after("IFieldNamePlugin")
                    .before("configure")
                    .process(() -> {
                        try {
                            IOC.register(Keys.getOrAdd("assertion of type equal"), new SingletonStrategy(new EqualAssertion()));
                            IOC.register(Keys.getOrAdd("assertion of type not equal"), new SingletonStrategy(new NotEqualAssertion()));
                        } catch (ResolutionException | RegistrationException | InvalidArgumentException e) {
                            throw new ActionExecuteException(e);
                        }
                    });

            bootstrap.add(assertionsItem);
        } catch (Throwable e) {
            throw new PluginException(e);
        }
    }
}
