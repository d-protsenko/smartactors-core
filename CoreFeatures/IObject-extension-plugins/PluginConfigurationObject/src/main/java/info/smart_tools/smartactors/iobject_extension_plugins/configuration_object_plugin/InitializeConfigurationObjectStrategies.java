package info.smart_tools.smartactors.iobject_extension_plugins.configuration_object_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.IAdditionDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.iobject_extension.configuration_object.CObjectStrategy;
import info.smart_tools.smartactors.iobject_extension.configuration_object.ConfigurationObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;

/**
 * creates and register some strategies for correct work of
 * {@link ConfigurationObject}.
 */
public class InitializeConfigurationObjectStrategies implements IPlugin {

    /** Local storage for instance of {@link IBootstrap}*/
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor with single argument
     * @param bootstrap instance of {@link IBootstrap}
     * @throws InvalidArgumentException if any errors occurred
     */
    public InitializeConfigurationObjectStrategies(final IBootstrap<IBootstrapItem<String>> bootstrap)
            throws InvalidArgumentException {
        if (null == bootstrap) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("ConfigurationObject");
            item
                    .after("IOC")
                    .before("configuration_manager")
                    .process( () -> {
                            try {
                                IOC.register(
                                        IOC.resolve(
                                                IOC.getKeyForKeyByNameResolutionStrategy(), "configuration object"
                                        ),
                                        new ApplyFunctionToArgumentsStrategy(
                                                (a) -> {
                                                    if (a.length == 0) {
                                                        return new ConfigurationObject();
                                                    } else if (a.length == 1 && a[0] instanceof String) {
                                                        try {
                                                            return new ConfigurationObject((String) a[0]);
                                                        } catch (InvalidArgumentException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    } else {
                                                        throw new RuntimeException("Could not create new instance of Configuration Object.");
                                                    }
                                                }
                                        )
                                );
                                IResolveDependencyStrategy defaultStrategy = new ApplyFunctionToArgumentsStrategy(
                                        (a) -> {
                                            try {
                                                return a[1];
                                            } catch (Throwable e) {
                                                throw new RuntimeException(
                                                        "Error in configuration 'default' rule.", e
                                                );
                                            }
                                        }
                                );
                                IResolveDependencyStrategy strategy = new CObjectStrategy();
                                ((IAdditionDependencyStrategy) strategy).register("default", defaultStrategy);
                                IOC.register(
                                        IOC.resolve(
                                                IOC.getKeyForKeyByNameResolutionStrategy(), "resolve key for configuration object"
                                        ),
                                        strategy
                                );
                                IOC.register(
                                        IOC.resolve(
                                                IOC.getKeyForKeyByNameResolutionStrategy(), "expandable_strategy#resolve key for configuration object"
                                        ),
                                        new SingletonStrategy(strategy)
                                );
                            } catch (Exception e) {
                                throw new ActionExecuteException(
                                        "Could not create or register some strategies for ConfigurationObject.",
                                        e);
                            }
                    })
                    .revertProcess( () -> {
                            String itemName = "ConfigurationObject";
                            String keyName = "";

                            try {
                                keyName = "expandable_strategy#resolve key for configuration object";
                                IOC.remove(IOC.resolve(IOC.getKeyForKeyByNameResolutionStrategy(), keyName));
                            } catch(DeletionException e) {
                                System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                            } catch (ResolutionException e) { }

                            try {
                                keyName = "resolve key for configuration object";
                                IOC.remove(IOC.resolve(IOC.getKeyForKeyByNameResolutionStrategy(), keyName));
                            } catch(DeletionException e) {
                                System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                            } catch (ResolutionException e) { }

                            try {
                                keyName = "configuration object";
                                IOC.remove(IOC.resolve(IOC.getKeyForKeyByNameResolutionStrategy(), keyName));
                            } catch(DeletionException e) {
                                System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                            } catch (ResolutionException e) { }
                    });
            this.bootstrap.add(item);
        } catch (Throwable e) {
            throw new PluginException("Could not load 'ConfigurationObject plugin'", e);
        }
    }
}
