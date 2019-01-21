package info.smart_tools.smartactors.ioc_strategy_pack_plugins.resolve_standard_types_strategies_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.i_registration_strategy.IRegistrationStrategy;
import info.smart_tools.smartactors.base.interfaces.i_registration_strategy.exception.RegistrationStrategyException;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunctionTwoArgs;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.base.strategy.strategy_storage_with_cache_strategy.StrategyStorageWithCacheStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.DoubleToBigDecimalResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.FloatToBigDecimalResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.IntegerToBigDecimalResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.StringToBigDecimalResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_boolean_strategies.BooleanToPrimitiveResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_character_strategies.StringToCharacterResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_date_strategies.StringToDateResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_integer_strategies.DoubleToIntResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_integer_strategies.StringToIntResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies.*;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_self.ClassToClassResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_string_strategies.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Plugin.
 * Implements {@link IPlugin}
 * Load strategies for resolving standard types
 */
public class ResolveStandardTypesStrategiesPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     */
    public ResolveStandardTypesStrategiesPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            IBootstrapItem<String> item = new BootstrapItem("ResolveStandardTypesStrategiesPlugin");
            item
                    .after("IOC")
                    .process(() -> {
                        try {
                            IFunction argToKey = arg -> arg.getClass();
                            IFunctionTwoArgs findValueByArgument = (map, arg) -> {
                                IResolveDependencyStrategy strategy = null;
                                for (Map.Entry<Class, IResolveDependencyStrategy> entry : ((Map<Class, IResolveDependencyStrategy>) map).entrySet()) {
                                    if (entry.getKey().isInstance(arg)) {
                                        strategy = entry.getValue();

                                        break;
                                    }
                                }
                                return strategy;
                            };

                            // to String strategies
                            IKey stringKey = Keys.resolveByName(String.class.getCanonicalName() + "convert");
                            IKey expandableStrategyStringKey = Keys.resolveByName("expandable_strategy#" + String.class.getCanonicalName());
                            IResolveDependencyStrategy stringStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IRegistrationStrategy)stringStrategy).register(
                                    String.class,
                                    new ClassToClassResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    Object.class,
                                    new ObjectToStringResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    int.class,
                                    new IntToStringResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    long.class,
                                    new LongToStringResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    float.class,
                                    new FloatToStringResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    double.class,
                                    new DoubleToStringResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    boolean.class,
                                    new BooleanToStringResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    byte.class,
                                    new ByteToStringResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    short.class,
                                    new ShortToStringResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    char.class,
                                    new CharToStringResolveDependencyStrategy()
                            );
                            IOC.register(stringKey, stringStrategy);
                            IOC.register(expandableStrategyStringKey, new SingletonStrategy(stringStrategy));

                            // to Character strategies
                            IKey characterKey = Keys.resolveByName(Character.class.getCanonicalName() + "convert");
                            IKey expandableStrategyCharacterKey = Keys.resolveByName("expandable_strategy#" + Character.class.getCanonicalName());
                            IResolveDependencyStrategy characterStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IRegistrationStrategy) characterStrategy).register(
                                    Character.class,
                                    new ClassToClassResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy) characterStrategy).register(
                                    String.class,
                                    new StringToCharacterResolveDependencyStrategy()
                            );
                            IOC.register(characterKey, characterStrategy);
                            IOC.register(expandableStrategyCharacterKey, new SingletonStrategy(characterStrategy));

                            // to boolean strategies
                            IKey booleanKey = Keys.resolveByName(boolean.class.getCanonicalName() + "convert");
                            IKey expandableStrategyBooleanKey = Keys.resolveByName("expandable_strategy#" + boolean.class.getCanonicalName());
                            IResolveDependencyStrategy booleanStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IRegistrationStrategy) booleanStrategy).register(
                                    boolean.class,
                                    new ClassToClassResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy) booleanStrategy).register(
                                    Boolean.class,
                                    new BooleanToPrimitiveResolveDependencyStrategy()
                            );
                            IOC.register(booleanKey, booleanStrategy);
                            IOC.register(expandableStrategyBooleanKey, new SingletonStrategy(booleanStrategy));

                            // to Integer strategies
                            IKey integerKey = Keys.resolveByName(Integer.class.getCanonicalName() + "convert");
                            IKey expandableStrategyIntegerKey = Keys.resolveByName("expandable_strategy#" + Integer.class.getCanonicalName());
                            IResolveDependencyStrategy integerStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IRegistrationStrategy) integerStrategy).register(
                                    Integer.class,
                                    new ClassToClassResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy) integerStrategy).register(
                                    String.class,
                                    new StringToIntResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy) integerStrategy).register(
                                    Double.class,
                                    new DoubleToIntResolveDependencyStrategy()
                            );
                            IOC.register(integerKey, integerStrategy);
                            IOC.register(expandableStrategyIntegerKey, new SingletonStrategy(integerStrategy));

                            // to BigDecimal strategies
                            IKey bigDecimalKey = Keys.resolveByName(BigDecimal.class.getCanonicalName() + "convert");
                            IKey expandableStrategyBigDecimalKey = Keys.resolveByName("expandable_strategy#" + BigDecimal.class.getCanonicalName());
                            IResolveDependencyStrategy bigDecimalStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IRegistrationStrategy) bigDecimalStrategy).register(
                                    BigDecimal.class,
                                    new ClassToClassResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy) bigDecimalStrategy).register(
                                    String.class,
                                    new StringToBigDecimalResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy) bigDecimalStrategy).register(
                                    Double.class,
                                    new DoubleToBigDecimalResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy) bigDecimalStrategy).register(
                                    Float.class,
                                    new FloatToBigDecimalResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy) bigDecimalStrategy).register(
                                    Integer.class,
                                    new IntegerToBigDecimalResolveDependencyStrategy()
                            );
                            IOC.register(bigDecimalKey, bigDecimalStrategy);
                            IOC.register(expandableStrategyBigDecimalKey, new SingletonStrategy(bigDecimalStrategy));

                            // to LocalDateTime strategies
                            IKey localDateTimeKey = Keys.resolveByName(LocalDateTime.class.getCanonicalName() + "convert");
                            IKey expandableStrategyLocalDateTimeKey = Keys.resolveByName("expandable_strategy#" + LocalDateTime.class.getCanonicalName());
                            IResolveDependencyStrategy localDateTimeStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IRegistrationStrategy) localDateTimeStrategy).register(
                                    LocalDateTime.class,
                                    new ClassToClassResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy) localDateTimeStrategy).register(
                                    String.class,
                                    new StringToDateResolveDependencyStrategy()
                            );
                            IOC.register(localDateTimeKey, localDateTimeStrategy);
                            IOC.register(expandableStrategyLocalDateTimeKey, new SingletonStrategy(localDateTimeStrategy));

                            // to list strategies
                            IKey listKey = Keys.resolveByName(List.class.getCanonicalName() + "convert");
                            IKey expandableStrategyListKey = Keys.resolveByName("expandable_strategy#" + List.class.getCanonicalName());
                            IResolveDependencyStrategy listStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IRegistrationStrategy) listStrategy).register(boolean[].class, new BooleanArrayToListResolveDependencyStrategy());
                            ((IRegistrationStrategy) listStrategy).register(byte[].class, new ByteArrayToListResolveDependencyStrategy());
                            ((IRegistrationStrategy) listStrategy).register(char[].class, new CharArrayToListResolveDependencyStrategy());
                            ((IRegistrationStrategy) listStrategy).register(double[].class, new DoubleArrayToListResolveDependencyStrategy());
                            ((IRegistrationStrategy) listStrategy).register(float[].class, new FloatArrayToListResolveDependencyStrategy());
                            ((IRegistrationStrategy) listStrategy).register(int[].class, new IntArrayToListResolveDependencyStrategy());
                            ((IRegistrationStrategy) listStrategy).register(long[].class, new LongArrayToListResolveDependencyStrategy());
                            ((IRegistrationStrategy) listStrategy).register(Object[].class, new ObjectArrayToListResolveDependencyStrategy());
                            ((IRegistrationStrategy) listStrategy).register(short[].class, new ShortArrayToListResolveDependencyStrategy());
                            IOC.register(listKey, listStrategy);
                            IOC.register(expandableStrategyListKey, new SingletonStrategy(listStrategy));

                            //to int strategies
                            IKey intKey = Keys.resolveByName(int.class.getCanonicalName() + "convert");
                            IKey expandableStrategyIntKey = Keys.resolveByName("expandable_strategy#" + int.class.getCanonicalName());
                            IResolveDependencyStrategy intStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IRegistrationStrategy) intStrategy).register(
                                    String.class,
                                    new StringToIntResolveDependencyStrategy()
                            );
                            ((IRegistrationStrategy) intStrategy).register(
                                    Double.class,
                                    new DoubleToIntResolveDependencyStrategy()
                            );
                            IOC.register(intKey, intStrategy);
                            IOC.register(expandableStrategyIntKey, new SingletonStrategy(intStrategy));
                        } catch (ResolutionException e) {
                            throw new ActionExecutionException("ResolveStandardTypesStrategies plugin can't load: can't get ResolveStandardTypesStrategies key", e);
                        } catch (RegistrationException | RegistrationStrategyException | InvalidArgumentException e) {
                            throw new ActionExecutionException("ResolveStandardTypesStrategies plugin can't load: can't register new strategy", e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "ResolveStandardTypesStrategiesPlugin";
                        String keyName = "";

                        try {
                            keyName = "expandable_strategy#" + int.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = int.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + List.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = List.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + LocalDateTime.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = LocalDateTime.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + BigDecimal.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = BigDecimal.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + Integer.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = Integer.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + boolean.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = boolean.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + Character.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = Character.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + String.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = String.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }
                    });

            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load ResolveStandardTypesStrategies plugin", e);
        }
    }
}
