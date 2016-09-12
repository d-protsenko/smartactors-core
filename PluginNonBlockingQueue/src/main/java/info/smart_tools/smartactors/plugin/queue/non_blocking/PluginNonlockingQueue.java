package info.smart_tools.smartactors.plugin.queue.non_blocking;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.non_blocking_queue.NonBlockingQueue;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Plugin that registers non-blocking queue in IOC.
 */
public class PluginNonlockingQueue implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public PluginNonlockingQueue(final IBootstrap<IBootstrapItem<String>> bootstrap) {
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
                                    return new NonBlockingQueue<>(new ConcurrentLinkedQueue<>());
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
