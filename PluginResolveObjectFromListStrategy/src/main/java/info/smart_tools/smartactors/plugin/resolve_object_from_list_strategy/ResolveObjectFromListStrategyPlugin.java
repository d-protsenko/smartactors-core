package info.smart_tools.smartactors.plugin.resolve_object_from_list_strategy;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_type_strategy.ResolveByTypeStrategy;
import info.smart_tools.smartactors.core.resolve_object_from_list_strategy.ResolveObjectFromListDependencyStrategy;

import java.util.List;

/**
 * Plugin for register {@link ResolveObjectFromListDependencyStrategy}
 */
public class ResolveObjectFromListStrategyPlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     * @throws InvalidArgumentException if bootstrap is null
     */
    public ResolveObjectFromListStrategyPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) throws InvalidArgumentException {
        if (null == bootstrap) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            IBootstrapItem<String> item = new BootstrapItem("ResolveObjectFromListStrategyPlugin");
            item
                .after("IOC")
                .process(() -> {
                    try {
                        IKey typeStrategyKey = Keys.getOrAdd(Object.class.getCanonicalName() + "convertFromList");
                        ResolveByTypeStrategy resolveStrategy = new ResolveByTypeStrategy();
                        resolveStrategy.register(List.class, new ResolveObjectFromListDependencyStrategy());
                        IOC.register(typeStrategyKey, resolveStrategy);
                    } catch (ResolutionException | RegistrationException e) {
                        throw new RuntimeException(e);
                    }
                });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load resolve object from list strategy plugin", e);
        }
    }
}
