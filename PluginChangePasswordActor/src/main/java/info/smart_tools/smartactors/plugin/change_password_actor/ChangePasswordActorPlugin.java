package info.smart_tools.smartactors.plugin.change_password_actor;

import info.smart_tools.smartactors.actor.change_password.ChangePasswordActor;
import info.smart_tools.smartactors.actor.change_password.wrapper.ChangePasswordConfig;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

/**
 * Plugin for strategy for {@link ChangePasswordActor}
 */
public class ChangePasswordActorPlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     */
    public ChangePasswordActorPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }


    @Override
    public void load() throws PluginException {

        try {
            IBootstrapItem<String> item = new BootstrapItem("ChangePasswordActorPlugin");
            item
                .after("IOC")
                .before("starter")
                .process(() -> {
                    try {
                        IField collectionNameField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
                        IField charsetField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "charset");
                        IField algorithmField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "algorithm");
                        IField encoderField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "encoder");

                        IOC.register(
                            Keys.getOrAdd(ChangePasswordConfig.class.getCanonicalName()), new ApplyFunctionToArgumentsStrategy(
                                (args) -> {
                                    IObject params = (IObject) args[0];
                                    return new ChangePasswordConfig() {

                                        @Override
                                        public String getCollectionName() throws ReadValueException {
                                            try {
                                                return collectionNameField.in(params);
                                            } catch (InvalidArgumentException e) {
                                                throw new ReadValueException(e);
                                            }
                                        }

                                        @Override
                                        public IPool getConnectionPool() throws ReadValueException {
                                            try {
                                                ConnectionOptions connectionOptions = IOC.resolve(
                                                    Keys.getOrAdd("PostgresConnectionOptions")
                                                );
                                                return IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"), connectionOptions);
                                            } catch (Exception e) {
                                                throw new ReadValueException(e);
                                            }
                                        }

                                        @Override
                                        public String getAlgorithm() throws ReadValueException {
                                            try {
                                                return algorithmField.in(params);
                                            } catch (InvalidArgumentException e) {
                                                throw new ReadValueException(e);
                                            }
                                        }

                                        @Override
                                        public String getCharset() throws ReadValueException {
                                            try {
                                                return charsetField.in(params);
                                            } catch (InvalidArgumentException e) {
                                                throw new ReadValueException(e);
                                            }
                                        }

                                        @Override
                                        public String getEncoder() throws ReadValueException {
                                            try {
                                                return encoderField.in(params);
                                            } catch (InvalidArgumentException e) {
                                                throw new ReadValueException(e);
                                            }
                                        }
                                    };
                                }
                            )
                        );
                        IOC.register(Keys.getOrAdd(ChangePasswordActor.class.getCanonicalName()), new ApplyFunctionToArgumentsStrategy(
                                (args) -> {
                                    try {
                                        IObject config = (IObject) args[0];
                                        return new ChangePasswordActor(
                                            IOC.resolve(Keys.getOrAdd(ChangePasswordConfig.class.getCanonicalName()), config)
                                        );
                                    } catch (Exception e) {
                                        throw new RuntimeException("Error during resolving ChangePasswordActor", e);
                                    }
                                }
                            )
                        );
                    } catch (ResolutionException e) {
                        throw new ActionExecuteException("ChangePasswordActor plugin can't load: can't get key");
                    } catch (InvalidArgumentException e) {
                        throw new ActionExecuteException("ChangePasswordActor plugin can't load: can't create strategy");
                    } catch (RegistrationException e) {
                        throw new ActionExecuteException("ChangePasswordActor plugin can't load: can't register new strategy");
                    }
                });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't get BootstrapItem", e);
        }
    }
}
