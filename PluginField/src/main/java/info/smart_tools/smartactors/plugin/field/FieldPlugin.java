package info.smart_tools.smartactors.plugin.field;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.wrapper_generator.Field;

import java.util.HashMap;
import java.util.Map;

/**
 * Plugin for registration of IOC strategy for Field
 */
public class FieldPlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     */
    public FieldPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            Map<String, IField> fieldMap = new HashMap<>();
            IBootstrapItem<String> item = new BootstrapItem("FieldPlugin");
            item
                .after("IOC")
                .process(() -> {
                    try {
                        IKey fieldKey = Keys.getOrAdd(IField.class.toString());
                        IOC.register(fieldKey, new CreateNewInstanceStrategy(
                            (args) -> {
                                String fieldName = String.valueOf(args[0]);
                                IField field = fieldMap.get(fieldName);
                                if (field == null) {
                                    try {
                                        //TODO:: Remove IKey when old Field would be returned
                                        field = new Field(new FieldName(fieldName));
                                        fieldMap.put(fieldName, field);
                                    } catch (InvalidArgumentException e) {
                                        throw new RuntimeException("Can't resolve field: ", e);
                                    }
                                }
                                return field;
                            }));
                    } catch (RegistrationException | InvalidArgumentException | ResolutionException e) {
                        throw new RuntimeException(e);
                    }
                });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load field plugin", e);
        }
    }
}
