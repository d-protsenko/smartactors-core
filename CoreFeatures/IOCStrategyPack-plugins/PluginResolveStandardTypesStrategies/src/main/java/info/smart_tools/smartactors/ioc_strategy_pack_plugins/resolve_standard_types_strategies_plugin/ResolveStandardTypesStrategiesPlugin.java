package info.smart_tools.smartactors.ioc_strategy_pack_plugins.resolve_standard_types_strategies_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iregistration_strategy.IRegistrationStrategy;
import info.smart_tools.smartactors.base.interfaces.iregistration_strategy.exception.RegistrationStrategyException;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunctionTwoArgs;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
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
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.*;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.FloatToBigDecimalResolutionStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.IntegerToBigDecimalResolutionStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_boolean_strategies.BooleanToPrimitiveResolutionStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_character_strategies.StringToCharacterResolutionStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_date_strategies.StringToDateResolutionStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_integer_strategies.DoubleToIntResolutionStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_integer_strategies.StringToIntResolutionStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies.*;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_self.ClassToClassResolutionStrategy;
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
                                IResolutionStrategy strategy = null;
                                for (Map.Entry<Class, IResolutionStrategy> entry : ((Map<Class, IResolutionStrategy>) map).entrySet()) {
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
                            IResolutionStrategy stringStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IRegistrationStrategy)stringStrategy).register(
                                    String.class,
                                    new ClassToClassResolutionStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    Object.class,
                                    new ObjectToStringResolutionStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    int.class,
                                    new IntToStringResolutionStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    long.class,
                                    new LongToStringResolutionStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    float.class,
                                    new FloatToStringResolutionStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    double.class,
                                    new DoubleToStringResolutionStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    boolean.class,
                                    new BooleanToStringResolutionStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    byte.class,
                                    new ByteToStringResolutionStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    short.class,
                                    new ShortToStringResolutionStrategy()
                            );
                            ((IRegistrationStrategy)stringStrategy).register(
                                    char.class,
                                    new CharToStringResolutionStrategy()
                            );
                            IOC.register(stringKey, stringStrategy);
                            IOC.register(expandableStrategyStringKey, new SingletonStrategy(stringStrategy));

                            // to Character strategies
                            IKey characterKey = Keys.resolveByName(Character.class.getCanonicalName() + "convert");
                            IKey expandableStrategyCharacterKey = Keys.resolveByName("expandable_strategy#" + Character.class.getCanonicalName());
                            IResolutionStrategy characterStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IRegistrationStrategy) characterStrategy).register(
                                    Character.class,
                                    new ClassToClassResolutionStrategy()
                            );
                            ((IRegistrationStrategy) characterStrategy).register(
                                    String.class,
                                    new StringToCharacterResolutionStrategy()
                            );
                            IOC.register(characterKey, characterStrategy);
                            IOC.register(expandableStrategyCharacterKey, new SingletonStrategy(characterStrategy));

                            // to boolean strategies
                            IKey booleanKey = Keys.resolveByName(boolean.class.getCanonicalName() + "convert");
                            IKey expandableStrategyBooleanKey = Keys.resolveByName("expandable_strategy#" + boolean.class.getCanonicalName());
                            IResolutionStrategy booleanStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IRegistrationStrategy) booleanStrategy).register(
                                    boolean.class,
                                    new ClassToClassResolutionStrategy()
                            );
                            ((IRegistrationStrategy) booleanStrategy).register(
                                    Boolean.class,
                                    new BooleanToPrimitiveResolutionStrategy()
                            );
                            IOC.register(booleanKey, booleanStrategy);
                            IOC.register(expandableStrategyBooleanKey, new SingletonStrategy(booleanStrategy));

                            // to Integer strategies
                            IKey integerKey = Keys.resolveByName(Integer.class.getCanonicalName() + "convert");
                            IKey expandableStrategyIntegerKey = Keys.resolveByName("expandable_strategy#" + Integer.class.getCanonicalName());
                            IResolutionStrategy integerStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IRegistrationStrategy) integerStrategy).register(
                                    Integer.class,
                                    new ClassToClassResolutionStrategy()
                            );
                            ((IRegistrationStrategy) integerStrategy).register(
                                    String.class,
                                    new StringToIntResolutionStrategy()
                            );
                            ((IRegistrationStrategy) integerStrategy).register(
                                    Double.class,
                                    new DoubleToIntResolutionStrategy()
                            );
                            IOC.register(integerKey, integerStrategy);
                            IOC.register(expandableStrategyIntegerKey, new SingletonStrategy(integerStrategy));

                            // to BigDecimal strategies
                            IKey bigDecimalKey = Keys.resolveByName(BigDecimal.class.getCanonicalName() + "convert");
                            IKey expandableStrategyBigDecimalKey = Keys.resolveByName("expandable_strategy#" + BigDecimal.class.getCanonicalName());
                            IResolutionStrategy bigDecimalStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IRegistrationStrategy) bigDecimalStrategy).register(
                                    BigDecimal.class,
                                    new ClassToClassResolutionStrategy()
                            );
                            ((IRegistrationStrategy) bigDecimalStrategy).register(
                                    String.class,
                                    new StringToBigDecimalResolutionStrategy()
                            );
                            ((IRegistrationStrategy) bigDecimalStrategy).register(
                                    Double.class,
                                    new DoubleToBigDecimalResolutionStrategy()
                            );
                            ((IRegistrationStrategy) bigDecimalStrategy).register(
                                    Float.class,
                                    new FloatToBigDecimalResolutionStrategy()
                            );
                            ((IRegistrationStrategy) bigDecimalStrategy).register(
                                    Integer.class,
                                    new IntegerToBigDecimalResolutionStrategy()
                            );
                            IOC.register(bigDecimalKey, bigDecimalStrategy);
                            IOC.register(expandableStrategyBigDecimalKey, new SingletonStrategy(bigDecimalStrategy));

                            // to LocalDateTime strategies
                            IKey localDateTimeKey = Keys.resolveByName(LocalDateTime.class.getCanonicalName() + "convert");
                            IKey expandableStrategyLocalDateTimeKey = Keys.resolveByName("expandable_strategy#" + LocalDateTime.class.getCanonicalName());
                            IResolutionStrategy localDateTimeStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IRegistrationStrategy) localDateTimeStrategy).register(
                                    LocalDateTime.class,
                                    new ClassToClassResolutionStrategy()
                            );
                            ((IRegistrationStrategy) localDateTimeStrategy).register(
                                    String.class,
                                    new StringToDateResolutionStrategy()
                            );
                            IOC.register(localDateTimeKey, localDateTimeStrategy);
                            IOC.register(expandableStrategyLocalDateTimeKey, new SingletonStrategy(localDateTimeStrategy));

                            // to list strategies
                            IKey listKey = Keys.resolveByName(List.class.getCanonicalName() + "convert");
                            IKey expandableStrategyListKey = Keys.resolveByName("expandable_strategy#" + List.class.getCanonicalName());
                            IResolutionStrategy listStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IRegistrationStrategy) listStrategy).register(boolean[].class, new BooleanArrayToListResolutionStrategy());
                            ((IRegistrationStrategy) listStrategy).register(byte[].class, new ByteArrayToListResolutionStrategy());
                            ((IRegistrationStrategy) listStrategy).register(char[].class, new CharArrayToListResolutionStrategy());
                            ((IRegistrationStrategy) listStrategy).register(double[].class, new DoubleArrayToListResolutionStrategy());
                            ((IRegistrationStrategy) listStrategy).register(float[].class, new FloatArrayToListResolutionStrategy());
                            ((IRegistrationStrategy) listStrategy).register(int[].class, new IntArrayToListResolutionStrategy());
                            ((IRegistrationStrategy) listStrategy).register(long[].class, new LongArrayToListResolutionStrategy());
                            ((IRegistrationStrategy) listStrategy).register(Object[].class, new ObjectArrayToListResolutionStrategy());
                            ((IRegistrationStrategy) listStrategy).register(short[].class, new ShortArrayToListResolutionStrategy());
                            IOC.register(listKey, listStrategy);
                            IOC.register(expandableStrategyListKey, new SingletonStrategy(listStrategy));

                            //to int strategies
                            IKey intKey = Keys.resolveByName(int.class.getCanonicalName() + "convert");
                            IKey expandableStrategyIntKey = Keys.resolveByName("expandable_strategy#" + int.class.getCanonicalName());
                            IResolutionStrategy intStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IRegistrationStrategy) intStrategy).register(
                                    String.class,
                                    new StringToIntResolutionStrategy()
                            );
                            ((IRegistrationStrategy) intStrategy).register(
                                    Double.class,
                                    new DoubleToIntResolutionStrategy()
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
