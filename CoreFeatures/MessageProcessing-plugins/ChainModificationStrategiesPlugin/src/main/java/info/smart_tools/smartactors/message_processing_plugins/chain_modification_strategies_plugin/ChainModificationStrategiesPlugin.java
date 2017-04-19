package info.smart_tools.smartactors.message_processing_plugins.chain_modification_strategies_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;

public class ChainModificationStrategiesPlugin extends BootstrapPlugin {

     /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public ChainModificationStrategiesPlugin(final IBootstrap bootstrap) {
            super(bootstrap);
    }

//    @Item("")
//    @After("")
//    @Before("")
//    public void doSomeThing()
//            throws ResolutionException, RegistrationException, InvalidArgumentException {
//
//    }
}
