package info.smart_tools.smartactors.plugin.ifieldname;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
            Map<String, IFieldName> fieldNamesMap = new ConcurrentHashMap<>();

            IBootstrapItem<String> item = new BootstrapItem("IFieldNamePlugin");

            item
                    .after("IOC")
                    .process(() -> {
                        try {
                            IKey iFieldNameKey = Keys.getOrAdd(IFieldName.class.toString());
                            IOC.register(iFieldNameKey,
                                    new CreateNewInstanceStrategy(
                                            (args) -> {
                                                try {
                                                    String nameOfFieldName = (String) args[0];
                                                    IFieldName result = fieldNamesMap.get(nameOfFieldName);

                                                    if (result == null) {
                                                        try {
                                                            result = new FieldName(nameOfFieldName);
                                                        } catch (InvalidArgumentException e) {
                                                            throw new RuntimeException(
                                                                    "Can't create FieldName with this name: "
                                                                            + nameOfFieldName, e);
                                                        }
                                                        fieldNamesMap.put(nameOfFieldName, result);
                                                    }

                                                    return result;
                                                } catch (ClassCastException e) {
                                                    throw new RuntimeException("Can't cast object to String: " + args[0], e);
                                                }
                                        }
                                    )
                            );
                        } catch (ResolutionException e) {
                            throw new RuntimeException("Can't get IFieldName key");
                        } catch (InvalidArgumentException e) {
                            throw new RuntimeException("Can't get create strategy");
                        } catch (RegistrationException e) {
                            throw new RuntimeException("Can't get register new strategy");
                        }
                    });
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't get BootstrapItem from one of reason", e);
        }
    }
}
