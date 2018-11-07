package info.smart_tools.smartactors.field_plugins.ifield_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.field.field.Field;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;

/**
 * Plugin for registration of IOC strategy for Field
 */
public class IFieldPlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     */
    public IFieldPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            BootstrapItem item = new BootstrapItem("IFieldPlugin");
            item
                .after("IOC")
                .process(() -> {
                    try {
                        IKey fieldKey = Keys.resolveByName(IField.class.getCanonicalName());
                        IOC.register(fieldKey, new ResolveByNameIocStrategy(
                            (args) -> {
                                String fieldName = String.valueOf(args[0]);
                                try {
                                    return new Field(IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), fieldName));
                                } catch (InvalidArgumentException | ResolutionException e) {
                                    throw new RuntimeException("Can't resolve IField: ", e);
                                }
                            }));
                    } catch (ResolutionException e) {
                        throw new ActionExecuteException("IField plugin can't load: can't get IField key", e);
                    } catch (InvalidArgumentException e) {
                        throw new ActionExecuteException("IField plugin can't load: can't create strategy", e);
                    } catch (RegistrationException e) {
                        throw new ActionExecuteException("IField plugin can't load: can't register new strategy", e);
                    }
                })
                .revertProcess(() -> {
                    String itemName = "IFieldPlugin";
                    String keyName = IField.class.getCanonicalName();
                    try {
                        IOC.remove(Keys.resolveByName(keyName));
                    } catch(DeletionException e) {
                        System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                    } catch (ResolutionException e) { }
                });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load IField plugin", e);
        }
    }
}
