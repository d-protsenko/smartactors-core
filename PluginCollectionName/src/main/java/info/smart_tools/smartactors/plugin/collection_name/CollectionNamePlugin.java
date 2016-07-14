package info.smart_tools.smartactors.plugin.collection_name;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Plugin for load IOC strategy for collection name object
 */
public class CollectionNamePlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     */
    public CollectionNamePlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            Map<String, CollectionName> collectionNameMap = new HashMap<>();
            IBootstrapItem<String> item = new BootstrapItem("CollectionNamePlugin");
            item
                .after("IOC")
                .process(() -> {
                    try {
                        IKey collectionNameKey = Keys.getOrAdd(CollectionName.class.toString());
                        IOC.register(collectionNameKey, new CreateNewInstanceStrategy(
                            (args) -> {
                                String name = String.valueOf(args[0]);
                                CollectionName collectionName = collectionNameMap.get(name);
                                if (collectionName == null) {
                                    try {
                                        collectionName = CollectionName.fromString(name);
                                        collectionNameMap.put(name, collectionName);
                                    } catch (StorageException e) {
                                        throw new RuntimeException("Can't resolve collection name: ", e);
                                    }
                                }

                                return collectionName;
                            }));
                    } catch (RegistrationException | InvalidArgumentException | ResolutionException e) {
                        throw new RuntimeException(e);
                    }
                });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load collection name plugin", e);
        }
    }
}
