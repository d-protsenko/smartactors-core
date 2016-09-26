package info.smart_tools.smartactors.plugin.queue.blocking;

import info.smart_tools.smartactors.core.blocking_queue.BlockingQueue;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Plugin that registers a blocking queue creation strategy in IOC.
 */
public class PluginBlockingQueue implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public PluginBlockingQueue(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("queue");

            item
                    .after("IOC")
                    .after("IFieldNamePlugin")
                    .process(() -> {
                        try {
                            IOC.register(Keys.getOrAdd(IQueue.class.getCanonicalName()), new ApplyFunctionToArgumentsStrategy(args -> {
                                try {
                                    IFieldName queueSizeFieldName = IOC.resolve(
                                            Keys.getOrAdd(IFieldName.class.getCanonicalName()), "queueSize");
                                    IObject conf = (IObject) args[0];
                                    int queueSize = (int) conf.getValue(queueSizeFieldName);

                                    return new BlockingQueue<>(new ArrayBlockingQueue<>(queueSize));
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }));
                        } catch (ResolutionException | RegistrationException | InvalidArgumentException e) {
                            throw new ActionExecuteException(e);
                        }
                    });

            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
