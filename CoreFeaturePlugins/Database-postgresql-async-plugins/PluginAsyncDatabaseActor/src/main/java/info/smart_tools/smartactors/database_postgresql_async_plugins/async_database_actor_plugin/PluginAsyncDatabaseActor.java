package info.smart_tools.smartactors.database_postgresql_async_plugins.async_database_actor_plugin;

import com.github.pgasync.ConnectionPoolBuilder;
import com.github.pgasync.Db;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.database.postgresql_async.async_query_actor.AsyncQueryActor;
import info.smart_tools.smartactors.database.postgresql_async.async_query_actor.impl.JSONBDataConverter;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.AsynchronousOperationException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.MessageReceiveException;

import java.net.URI;

/**
 *
 */
public class PluginAsyncDatabaseActor extends BootstrapPlugin {
    public PluginAsyncDatabaseActor(final IBootstrap bootstrap) {
        super(bootstrap);
    }

    @Item("register_async_postgres_query_actor")
    public void registerActor()
            throws ResolutionException, InvalidArgumentException, RegistrationException {
        IFieldName poolSizeFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "poolSize");
        IFieldName uriFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "uri");

        IOC.register(
                Keys.getOrAdd("asynchronous postgresql query actor"),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    try {
                        IObject arg = (IObject) args[0];

                        // TODO: Use ConnectionOptions (?)
                        int poolSize = ((Number) arg.getValue(poolSizeFieldName)).intValue();
                        URI uri = URI.create((String) arg.getValue(uriFieldName));

                        String[] userInfo = uri.getUserInfo().split(":");

                        ConnectionPoolBuilder builder = new ConnectionPoolBuilder()
                                .hostname(uri.getHost())
                                .port(uri.getPort())
                                .database(uri.getPath().substring(1)) // skip "^/"
                                .username(userInfo[0])
                                .poolSize(poolSize);

                        if (userInfo.length > 1) {
                            builder = builder.password(userInfo[1]);
                        }

                        builder = builder.dataConverter(JSONBDataConverter.INSTANCE);

                        Db db = builder.build();

                        return new AsyncQueryActor(db);
                    } catch (Exception e) {
                        throw new FunctionExecutionException(e);
                    }
                })
        );
    }

    // TODO:: Remove
    // For testing purposes only
    @Item("print_exception_receiver")
    public void printExceptionReceiver()
            throws Exception {
        IFieldName excFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "exception");

        IOC.register(
                Keys.getOrAdd("print exception receiver"),
                new ApplyFunctionToArgumentsStrategy(args -> (IMessageReceiver) processor -> {
                    try {
                        ((Throwable) processor.getContext().getValue(excFieldName)).printStackTrace();
                    } catch (Exception e) {
                        throw new MessageReceiveException(e);
                    }
                })
        );
    }
}
