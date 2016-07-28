package info.smart_tools.smartactors.plugin.actor_reveiver_creator;

import info.smart_tools.smartactors.core.actor_receiver_creator.ActorReceiverCreator;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.iroutable_object_creator.IRoutedObjectCreator;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of {@link IPlugin}.
 * Plugin creates new instance of {@link ActorReceiverCreator} and
 * register its into IOC.
 */
public class ActorReceiverCreatorPlugin  implements IPlugin {

    /** Local storage for instance of {@link IBootstrap}*/
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * @param bootstrap Target bootstrap for adding strategy
     * @throws InvalidArgumentException if any errors occurred
     */
    public ActorReceiverCreatorPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap)
            throws InvalidArgumentException {
        if (null == bootstrap) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.bootstrap = bootstrap;
    }

    @Override
    public void load()
            throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("ActorReceiverCreator");
            item
                    .after("IOC")
                    .before("configure")
                    .after("InitializeReceiverGenerator")
                    .after("InitializeWrapperGenerator")
                    .after("IFieldPlugin")
                    .process(
                            () -> {
                                try {
                                    IOC.register(
                                            Keys.getOrAdd("actor_receiver_queue"),
                                            new CreateNewInstanceStrategy(args -> new ConcurrentLinkedQueue()));

                                    IOC.register(
                                            Keys.getOrAdd("actor_receiver_busyness_flag"),
                                            new CreateNewInstanceStrategy(args -> new AtomicBoolean(false)));

                                    ActorReceiverCreator objectCreator = new ActorReceiverCreator();
                                    IOC.register(
                                            IOC.resolve(
                                                    IOC.getKeyForKeyStorage(),
                                                    IRoutedObjectCreator.class.getCanonicalName() + "#actor"
                                            ),
                                            new SingletonStrategy(objectCreator)
                                    );
                                } catch (Exception e) {
                                    throw new RuntimeException(
                                            "Could not create or register actor receiver creator.", e
                                    );
                                }
                            }
                    );
            this.bootstrap.add(item);
        } catch (Throwable e) {
            throw new PluginException("Could not load 'ActorReceiverCreator plugin'", e);
        }
    }
}
