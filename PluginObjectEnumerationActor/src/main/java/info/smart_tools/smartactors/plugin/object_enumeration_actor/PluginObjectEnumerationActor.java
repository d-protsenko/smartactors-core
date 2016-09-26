package info.smart_tools.smartactors.plugin.object_enumeration_actor;

import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.object_enumeration_actor.ObjectEnumerationActor;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.plugin.base.bootstrap_plugin.BootstrapPlugin;

/**
 * Plugin for {@link ObjectEnumerationActor}.
 */
public class PluginObjectEnumerationActor extends BootstrapPlugin {
    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public PluginObjectEnumerationActor(final IBootstrap bootstrap) {
        super(bootstrap);
    }

    /**
     * Registers ObjectEnumerationActor actor type.
     *
     * @throws Exception if any error occurs registering the actor
     */
    @Item("actor:object_enumerator")
    @After({"IOC"})
    public void objectEnumerationActor()
            throws Exception {
        IOC.register(
                Keys.getOrAdd("ObjectEnumerationActor"),
                new SingletonStrategy(new ObjectEnumerationActor()));
    }
}
