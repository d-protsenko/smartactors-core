package info.smart_tools.smartactors.iobject_plugins.fieldname_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;

/**
 * Plugin registers strategy for resolving field name by field name class
 */
public class FieldNamePlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     */
    public FieldNamePlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {

            IBootstrapItem<String> item = new BootstrapItem("FieldNamePlugin");

            item
                    .after("IOC")
                    .process(() -> {
                        try {
                            IKey fieldNameKey = Keys.getKeyByName(FieldName.class.getCanonicalName());
                            IOC.register(fieldNameKey,
                                    new ResolveByNameIocStrategy(
                                            (args) -> {
                                                try {
                                                    String nameOfFieldName = (String) args[0];
                                                    return new FieldName(nameOfFieldName);
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
                            throw new ActionExecutionException("FieldName plugin can't load: can't get FieldName key");
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecutionException("FieldName plugin can't load: can't create strategy");
                        } catch (RegistrationException e) {
                            throw new ActionExecutionException("FieldName plugin can't load: can't register new strategy");
                        }
                    })
                    .revertProcess(() -> {
                        String[] keyNames = {
                                FieldName.class.getCanonicalName(),
                                "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"
                        };
                        Keys.unregisterByNames(keyNames);
                    });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't get BootstrapItem from one of reason", e);
        }
    }

}
