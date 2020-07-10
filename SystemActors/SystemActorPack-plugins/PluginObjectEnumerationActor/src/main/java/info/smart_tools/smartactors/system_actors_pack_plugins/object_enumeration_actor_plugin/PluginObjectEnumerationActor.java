package info.smart_tools.smartactors.system_actors_pack_plugins.object_enumeration_actor_plugin;

import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.system_actors_pack.object_enumeration_actor.ObjectEnumerationActor;

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
    //@After({"IOC"})
    public void objectEnumerationActor()
            throws Exception {
        IOC.register(
                Keys.getKeyByName("ObjectEnumerationActor"),
                new SingletonStrategy(new ObjectEnumerationActor()));
    }
}
