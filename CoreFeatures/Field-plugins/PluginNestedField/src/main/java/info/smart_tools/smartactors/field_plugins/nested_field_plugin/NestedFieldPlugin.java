package info.smart_tools.smartactors.field_plugins.nested_field_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.field.nested_field.NestedField;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;

/**
 * Plugin for {@link NestedFieldPlugin}
 */
public class NestedFieldPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     */
    public NestedFieldPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            BootstrapItem item = new BootstrapItem("NestedFieldPlugin");
            item
                    .after("IOC")
                    .process(() -> {
                        try {
                            IKey fieldKey = Keys.resolveByName(NestedField.class.getCanonicalName());
                            IOC.register(fieldKey, new ResolveByNameIocStrategy(
                                    (args) -> {
                                        try {
                                            String fieldName = String.valueOf(args[0]);
                                            return new NestedField(fieldName);
                                        } catch (InvalidArgumentException e) {
                                            throw new RuntimeException("Can't resolve NestedField: ", e);
                                        }
                                    }));
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("NestedField plugin can't load: can't get NestedField key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("NestedField plugin can't load: can't create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("NestedField plugin can't load: can't register new strategy", e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "NestedFieldPlugin";
                        String keyName = NestedField.class.getCanonicalName();

                        try {
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }
                    });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load NestedField plugin", e);
        }
    }
}
