package info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;

/**
 * Plugin registers strategy for resolving field name by field name interface
 */
public class IFieldNamePlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     */
    public IFieldNamePlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("IFieldNamePlugin");

            item
                    .after("IOC")
                    .process(() -> {
                        try {
                            IKey iFieldNameKey = Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName");
                            IOC.register(iFieldNameKey,
                                    new ResolveByNameIocStrategy(
                                            (args) -> {
                                                try {
                                                    String nameOfFieldName = (String) args[0];
                                                    IFieldName result = new FieldName(nameOfFieldName);

                                                    return result;
                                                } catch (ClassCastException e) {
                                                    throw new RuntimeException("Can't cast object to String: " + args[0],
                                                            e);
                                                } catch (ArrayIndexOutOfBoundsException e) {
                                                    throw new RuntimeException(
                                                            "Can't get args: args must contain one or more elements " +
                                                                    "and first element must be String",
                                                            e);
                                                } catch (InvalidArgumentException e) {
                                                    throw new RuntimeException(
                                                            "Can't create new field name with this name: " + args[0],
                                                            e);
                                                }
                                        }
                                    )
                            );
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("IFieldName plugin can't load: can't get IFieldName key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("IFieldName plugin can't load: can't create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("IFieldName plugin can't load: can't register new strategy", e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "IFieldNamePlugin";
                        String keyName = "";

                        try {
                            keyName = "info.smart_tools.smartactors.iobject.ifield_name.IFieldName";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }
                    });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't get BootstrapItem from one of reason", e);
        }
    }
}
