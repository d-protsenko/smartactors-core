package info.smart_tools.smartactors.plugin.cached_collection;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.cached_collection.CachedCollection;
import info.smart_tools.smartactors.core.cached_collection.ICachedCollection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;


/**
 * Plugin for registration strategy of create cached collection with IOC.
 * IOC resolve method waits collectionName as a first parameter and keyName as a second parameter.
 */
public class CreateCachedCollectionPlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap
     */
    public CreateCachedCollectionPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            IBootstrapItem<String> item = new BootstrapItem("CreateCachedCollectionPlugin");

            item
                .after("IOC")
                .after("CollectionNamePlugin")
                .process(() -> {
                    try {
                        IKey cachedCollectionKey = Keys.getOrAdd(ICachedCollection.class.toString());
                        IField connectionPoolField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "connectionPool");
                        IField collectionNameField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "collectionName");
                        IField keyNameField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "keyName");
                        //TODO:: change ResolveByNameIocStrategy to ResolveByCompositeNameIocStrategy
                        IOC.register(cachedCollectionKey, new ResolveByNameIocStrategy(
                            (args) -> {
                                try {
                                    CollectionName collectionName = IOC.resolve(Keys.getOrAdd(CollectionName.class.toString()), args[0]);
                                    String keyName = String.valueOf(args[1]);
                                    if (collectionName == null) {
                                        throw new RuntimeException("Can't resolve cached collection: key parameter is null");
                                    }
                                    IObject config = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));
                                    IPool connectionPool = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"));
                                    connectionPoolField.out(config, connectionPool);
                                    collectionNameField.out(config, collectionName);
                                    keyNameField.out(config, keyName);

                                    return new CachedCollection(config);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }));
                } catch (RegistrationException | InvalidArgumentException | ResolutionException e) {
                    throw new RuntimeException(e);
                }
            });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load CreateCollectionActor plugin", e);
        }
    }
}
