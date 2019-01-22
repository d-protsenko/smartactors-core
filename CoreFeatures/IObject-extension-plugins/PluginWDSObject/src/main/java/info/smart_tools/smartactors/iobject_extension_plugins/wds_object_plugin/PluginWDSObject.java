package info.smart_tools.smartactors.iobject_extension_plugins.wds_object_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.FunctionExecutionException;
import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_extension.wds_object.WDSObject;
import info.smart_tools.smartactors.iobject_extension.wds_object.WDSObjectFieldSet;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Plugin registers into IOC strategy for creation new instance of
 * {@link WDSObject} and strategy for storing other strategies that will be using as WDS transformation rules.
 */
public class PluginWDSObject extends BootstrapPlugin {

    /**
     * The constructor.
     *
     * @param bootstrap the bootstrap
     */
    public PluginWDSObject(IBootstrap bootstrap) {
        super(bootstrap);
    }

    @Item("wds_object_field_set_map_strategies")
    public void registerFieldSetDependencies()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.resolveByName("WDSObject field set thread safe map"),
                // TODO:: Use some lock-free hash map
                new ApplyFunctionToArgumentsStrategy(args -> new ConcurrentSkipListMap())
        );

        IOC.register(
                Keys.resolveByName("WDSObject field set non thread safe map"),
                new ApplyFunctionToArgumentsStrategy(args -> new HashMap())
        );
    }

    @ItemRevert("wds_object_field_set_map_strategies")
    public void unregisterFieldSetDependencies() {
        String itemName = "wds_object_field_set_map_strategies";
        String keyName;

        keyName = "WDSObject field set thread safe map";
        try {
            IOC.unregister(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }

        keyName = "WDSObject field set non thread safe map";
        try {
            IOC.unregister(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }
    }

    @Item("wds_object_rules_strategy")
    public void registerRulesStrategy()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.resolveByName(IStrategy.class.getCanonicalName()),
                new ResolveByNameIocStrategy(
                        (a) -> a[1]
                )
        );
    }

    @ItemRevert("wds_object_rules_strategy")
    public void unregisterRulesStrategy() {
        String itemName = "wds_object_rules_strategy";
        String keyName = "";

        try {
            keyName = IStrategy.class.getCanonicalName();
            IOC.unregister(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }
    }

    @Item("wds_object_field_set_strategy")
    @After({
        "wds_object_field_set_map_strategies"
    })
    public void registerFieldSetCreationStrategy()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.resolveByName("thread safe wrapper configuration"),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    try {
                        Map<IFieldName, IField> inMap = IOC.resolve(Keys.resolveByName("WDSObject field set thread safe map"));
                        Map<IFieldName, IField[]> outMap = IOC.resolve(Keys.resolveByName("WDSObject field set thread safe map"));
                        return new WDSObjectFieldSet((IObject) args[0], inMap, outMap);
                    } catch (Exception e) {
                        throw new FunctionExecutionException(e);
                    }
                })
        );

        IOC.register(
                Keys.resolveByName("non thread safe wrapper configuration"),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    try {
                        Map<IFieldName, IField> inMap = IOC.resolve(Keys.resolveByName("WDSObject field set non thread safe map"));
                        Map<IFieldName, IField[]> outMap = IOC.resolve(Keys.resolveByName("WDSObject field set non thread safe map"));
                        return new WDSObjectFieldSet((IObject) args[0], inMap, outMap);
                    } catch (Exception e) {
                        throw new FunctionExecutionException(e);
                    }
                })
        );
    }

    @ItemRevert("wds_object_field_set_strategy")
    public void unregisterFieldSetCreationStrategy() {
        String itemName = "wds_object_field_set_strategy";
        String keyName;

        keyName = "thread safe wrapper configuration";
        try {
            IOC.unregister(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }

        keyName = "non thread safe wrapper configuration";
        try {
            IOC.unregister(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }
    }

    @Item("wds_object")
    @After({
        "wds_object_field_set_strategy",
        "wds_object_rules_strategy",
        "FieldNamePlugin",
    })
    public void registerWDSObjectCreationStrategy()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.resolveByName(WDSObject.class.getCanonicalName()),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    try {
                        if (args[0] instanceof IObject) {
                            return new WDSObject((IObject) args[0]);
                        }

                        if (args[0] instanceof WDSObjectFieldSet) {
                            return new WDSObject((WDSObjectFieldSet) args[0]);
                        }
                    } catch (InvalidArgumentException e) {
                        throw new RuntimeException(e);
                    }

                    throw new FunctionExecutionException("Unexpected argument types for WDSObject creation strategy.");
                }));
    }

    @ItemRevert("wds_object")
    public void unregisterWDSObjectCreationStrategy() {
        String itemName = "wds_object";
        String keyName = "";

        try {
            keyName = WDSObject.class.getCanonicalName();
            IOC.unregister(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }

        keyName = "non thread safe wrapper configuration";
        try {
            IOC.unregister(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }
    }
}
