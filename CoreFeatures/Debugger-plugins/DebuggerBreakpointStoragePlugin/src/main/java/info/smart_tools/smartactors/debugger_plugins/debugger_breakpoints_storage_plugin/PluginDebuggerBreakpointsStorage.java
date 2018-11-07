package info.smart_tools.smartactors.debugger_plugins.debugger_breakpoints_storage_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.debugger.interfaces.IDebuggerBreakpointsStorage;
import info.smart_tools.smartactors.debugger.session_impl.DebuggerBreakpointsStorageImpl;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;

/**
 *
 */
public class PluginDebuggerBreakpointsStorage extends BootstrapPlugin {
    /**
     * The constructor.
     *
     * @param bootstrap the bootstrap
     */
    public PluginDebuggerBreakpointsStorage(final IBootstrap bootstrap) {
        super(bootstrap);
    }

    /**
     * Register strategy of creation of a breakpoints storage for debugger session.
     *
     * @throws ResolutionException if error occurs resolving a key
     * @throws RegistrationException if error occurs registering a strategy
     * @throws InvalidArgumentException if strategy does not accept the function
     */
    @Item("debugger:breakpoint_storage")
    public void registerBreakppointStorage()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(Keys.getOrAdd(IDebuggerBreakpointsStorage.class.getCanonicalName()), new ApplyFunctionToArgumentsStrategy(args -> {
            try {
                return new DebuggerBreakpointsStorageImpl();
            } catch (ResolutionException e) {
                throw new FunctionExecutionException(e);
            }
        }));
    }
}
