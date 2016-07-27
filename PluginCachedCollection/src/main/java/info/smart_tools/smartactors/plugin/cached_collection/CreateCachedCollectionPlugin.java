package info.smart_tools.smartactors.plugin.cached_collection;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.cached_collection.CachedCollection;
import info.smart_tools.smartactors.core.cached_collection.ICachedCollection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject_wrapper.IObjectWrapper;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.iwrapper_generator.IWrapperGenerator;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.core.resolve_by_composite_name_ioc_with_lambda_strategy.ResolveByCompositeNameIOCStrategy;
import info.smart_tools.smartactors.core.wds_object.WDSObject;
import info.smart_tools.smartactors.core.wrapper_generator.WrapperGenerator;

import java.util.ArrayList;
import java.util.List;


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
                .after("IFieldPlugin")
                .process(() -> {
                    try {
                        IKey cachedCollectionKey = Keys.getOrAdd(ICachedCollection.class.toString());
                        IField connectionPoolField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "connectionPool");
                        IField collectionNameField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "collectionName");
                        IField keyNameField = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "keyName");
                        IOC.register(cachedCollectionKey, new ResolveByCompositeNameIOCStrategy(
                            (args) -> {
                                try {
                                    CollectionName collectionName = IOC.resolve(Keys.getOrAdd(CollectionName.class.toString()), args[0]);
                                    if (collectionName == null) {
                                        throw new RuntimeException("Can't resolve cached collection: collectionName is null");
                                    }
                                    String keyName = String.valueOf(args[1]);
                                    //TODO:: clarify about generators
                                    //TODO:: wrapperGenerator should be resolved by IOC
                                    IObject configWDS = new DSObject();

                                    //TODO:: remove this hardcode, read from config
                                    //Setter configuration
                                    List<IObject> internalListUrl = new ArrayList<>();
                                    List<List<IObject>> ruleListUrl = new ArrayList<>();
                                    internalListUrl.add(new DSObject("{\n" +
                                        "\t\t\"name\": \"wds_target_strategy\",\n" +
                                        "\t\t\"args\": [\"local/value\", \"message/url\"]\n" +
                                        "}"));
                                    ruleListUrl.add(internalListUrl);
                                    configWDS.setValue(new FieldName("out_setUrl"), ruleListUrl);

                                    List<IObject> internalListUsername = new ArrayList<>();
                                    List<List<IObject>> ruleListUsername = new ArrayList<>();
                                    internalListUsername.add(new DSObject("{\n" +
                                        "\t\t\"name\": \"wds_target_strategy\",\n" +
                                        "\t\t\"args\": [\"local/value\", \"message/username\"]\n" +
                                        "}"));
                                    ruleListUsername.add(internalListUsername);
                                    configWDS.setValue(new FieldName("out_setUsername"), ruleListUsername);


                                    List<IObject> internalListPassword = new ArrayList<>();
                                    List<List<IObject>> ruleListPassword = new ArrayList<>();
                                    internalListPassword.add(new DSObject("{\n" +
                                        "\t\t\"name\": \"wds_target_strategy\",\n" +
                                        "\t\t\"args\": [\"local/value\", \"message/password\"]\n" +
                                        "}"));
                                    ruleListPassword.add(internalListPassword);
                                    configWDS.setValue(new FieldName("out_setPassword"), ruleListPassword);

                                    List<IObject> internalListMaxConnections = new ArrayList<>();
                                    List<List<IObject>> ruleListMaxConnections = new ArrayList<>();
                                    internalListMaxConnections.add(new DSObject("{\n" +
                                        "\t\t\"name\": \"wds_target_strategy\",\n" +
                                        "\t\t\"args\": [\"local/value\", \"message/maxConnections\"]\n" +
                                        "}"));
                                    ruleListMaxConnections.add(internalListMaxConnections);
                                    configWDS.setValue(new FieldName("out_setMaxConnections"), ruleListMaxConnections);


                                    //Getter configurations
                                    List<IObject> ruleListUrlGetter = new ArrayList<>();
                                    ruleListUrlGetter.add(new DSObject("{\n" +
                                        "\t\t\"name\": \"wds_getter_strategy\",\n" +
                                        "\t\t\"args\": [\"message/url\"]\n" +
                                        "}"));
                                    configWDS.setValue(new FieldName("in_getUrl"), ruleListUrlGetter);

                                    List<IObject> ruleListUsernameGetter = new ArrayList<>();
                                    ruleListUsernameGetter.add(new DSObject("{\n" +
                                        "\t\t\"name\": \"wds_getter_strategy\",\n" +
                                        "\t\t\"args\": [\"message/username\"]\n" +
                                        "}"));
                                    configWDS.setValue(new FieldName("in_getUsername"), ruleListUsernameGetter);

                                    List<IObject> ruleListPasswordGetter = new ArrayList<>();
                                    ruleListPasswordGetter.add(new DSObject("{\n" +
                                        "\t\t\"name\": \"wds_getter_strategy\",\n" +
                                        "\t\t\"args\": [\"message/password\"]\n" +
                                        "}"));
                                    configWDS.setValue(new FieldName("in_getPassword"), ruleListPasswordGetter);

                                    List<IObject> ruleListMaxConnectionsGetter = new ArrayList<>();
                                    ruleListMaxConnectionsGetter.add(new DSObject("{\n" +
                                        "\t\t\"name\": \"wds_getter_strategy\",\n" +
                                        "\t\t\"args\": [\"message/maxConnections\"]\n" +
                                        "}"));
                                    configWDS.setValue(new FieldName("in_getMaxConnections"), ruleListMaxConnectionsGetter);


                                    IObject wdsObject = new WDSObject(configWDS);
                                    IObject environment = new DSObject();
                                    environment.setValue(new FieldName("message"), new DSObject());

                                    IWrapperGenerator wrapperGenerator = new WrapperGenerator(this.getClass().getClassLoader());
                                    ConnectionOptions connectionOptionsWrapper = wrapperGenerator.generate(ConnectionOptions.class);
                                    ((IObjectWrapper) wdsObject).init(environment);
                                    ((IObjectWrapper) connectionOptionsWrapper).init(wdsObject);
                                    connectionOptionsWrapper.setUrl("jdbc:postgresql://localhost:5432/vp");
                                    connectionOptionsWrapper.setUsername("test_user");
                                    connectionOptionsWrapper.setPassword("qwerty");
                                    connectionOptionsWrapper.setMaxConnections(10);
                                    IPool connectionPool = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"), connectionOptionsWrapper);
                                    IObject config = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));
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
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
            });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load CreateCollectionActor plugin", e);
        }
    }
}
