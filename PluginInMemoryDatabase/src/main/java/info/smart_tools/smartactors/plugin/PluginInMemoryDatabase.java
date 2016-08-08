package info.smart_tools.smartactors.plugin;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.idatabase.exception.IDataBaseException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.in_memory_database.IConditionVerifier;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import sun.plugin.navig.motif.Plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Plugin for in memory database
 */
public class PluginInMemoryDatabase implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     *
     * @param bootstrap bootstrap element
     */
    public PluginInMemoryDatabase(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        IBootstrapItem<String> item = null;
        Map<String, IConditionVerifier> verifierMap = new HashMap<>();
        try {
            item = new BootstrapItem("IObjectSimpleImplPlugin");
            item
                    .after("IOC")
                    .after("FieldNamePlugin")
                    .process(() -> {
                                try {
                                    IOC.register(Keys.getOrAdd("NestedFieldName"), new ApplyFunctionToArgumentsStrategy(
                                                    (args) -> {
                                                        IFieldName fieldName = (IFieldName) args[0];
                                                        IObject iObject = (IObject) args[1];
                                                        String string = fieldName.toString();
                                                        String[] strings = string.split("\\.");
                                                        Object bufObject = iObject;
                                                        try {
                                                            for (String fieldString : strings) {
                                                                if (!(bufObject instanceof IObject)) {
                                                                    return null;
                                                                }
                                                                IFieldName bufFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), fieldString);
                                                                bufObject = ((IObject) bufObject).getValue(bufFieldName);
                                                                if (null == bufObject) {
                                                                    return null;
                                                                }
                                                            }
                                                            return bufObject;
                                                        } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                                                        }
                                                        return null;
                                                    }
                                            )
                                    );


                                    verifierMap.put("$general", (condition, document) -> {
                                                Iterator<Map.Entry<IFieldName, Object>> iterator = condition.iterator();
                                                String key = null;
                                                do {
                                                    Map.Entry<IFieldName, Object> entry = iterator.next();
                                                    key = entry.getKey().toString();
                                                    if (entry.getValue() instanceof IObject) {
                                                        iterator = ((IObject) entry.getValue()).iterator();
                                                    }
                                                } while (iterator.hasNext() && !verifierMap.containsKey(key));
                                                return verifierMap.get(key).verify(condition, document);
                                            }
                                    );

                                    verifierMap.put("$eq", (condition, document) -> {
                                                IFieldName fieldName = condition.iterator().next().getKey();
                                                try {
                                                    Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                                                    ;
                                                    Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$eq"));
                                                    return reference.equals(entry);
                                                } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                                                }
                                                return false;
                                            }
                                    );
                                    verifierMap.put("$neq", (condition, document) -> {
                                                IFieldName fieldName = condition.iterator().next().getKey();
                                                try {
                                                    Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                                                    ;
                                                    Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$neq"));
                                                    return !reference.equals(entry);
                                                } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                                                }
                                                return false;
                                            }
                                    );
                                    verifierMap.put("$gt", (condition, document) -> {
                                                IFieldName fieldName = condition.iterator().next().getKey();
                                                try {
                                                    Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                                                    ;
                                                    Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$gt"));
                                                    if (reference instanceof Long) {
                                                        Long longEntry = (Long) entry;
                                                        Long longReference = (Long) reference;
                                                        return longReference.compareTo(longEntry) == -1;
                                                    }
                                                    if (reference instanceof Double) {
                                                        Double doubleReference = (Double) reference;
                                                        Double doubleEntry = Double.parseDouble(entry.toString());
                                                        return doubleReference.compareTo(doubleEntry) == -1;
                                                    }
                                                    if (reference instanceof String) {
                                                        String doubleEntry = (String) entry;
                                                        String doubleReference = (String) reference;
                                                        return doubleReference.compareTo(doubleEntry) == -1;
                                                    }
                                                } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                                                }
                                                return false;
                                            }
                                    );
                                    verifierMap.put("$lt", (condition, document) -> {
                                                IFieldName fieldName = condition.iterator().next().getKey();
                                                try {
                                                    Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                                                    ;
                                                    Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$lt"));
                                                    if (reference instanceof Long) {
                                                        Long longEntry = (Long) entry;
                                                        Long longReference = (Long) reference;
                                                        return longReference.compareTo(longEntry) == 1;
                                                    }
                                                    if (reference instanceof Double) {
                                                        Double doubleReference = (Double) reference;
                                                        Double doubleEntry = Double.parseDouble(entry.toString());
                                                        return doubleReference.compareTo(doubleEntry) == 1;
                                                    }
                                                    if (reference instanceof String) {
                                                        String doubleEntry = (String) entry;
                                                        String doubleReference = (String) reference;
                                                        return doubleReference.compareTo(doubleEntry) == 1;
                                                    }
                                                } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                                                }
                                                return false;
                                            }
                                    );
                                    verifierMap.put("$gte", (condition, document) -> {
                                                IFieldName fieldName = condition.iterator().next().getKey();
                                                try {
                                                    Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                                                    ;
                                                    Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$gte"));
                                                    if (reference instanceof Long) {
                                                        Long longEntry = (Long) entry;
                                                        Long longReference = (Long) reference;
                                                        return longReference.compareTo(longEntry) == -1 || longReference.equals(longEntry);
                                                    }
                                                    if (reference instanceof Double) {
                                                        Double doubleReference = (Double) reference;
                                                        Double doubleEntry = Double.parseDouble(entry.toString());
                                                        return doubleReference.compareTo(doubleEntry) == -1 || doubleReference.equals(doubleEntry);
                                                    }
                                                    if (reference instanceof String) {
                                                        String doubleEntry = (String) entry;
                                                        String doubleReference = (String) reference;
                                                        return doubleReference.compareTo(doubleEntry) == -1 || doubleReference.equals(doubleEntry);
                                                    }
                                                } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                                                }
                                                return false;
                                            }
                                    );
                                    verifierMap.put("$date-from", (condition, document) -> {
                                                IFieldName fieldName = condition.iterator().next().getKey();
                                                try {
                                                    String entry = (String) document.getValue(fieldName);
                                                    String reference = String.valueOf(
                                                            ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$date-from"))
                                                    );
                                                    return reference.compareTo(entry) == -1 || reference.equals(entry);
                                                } catch (ReadValueException | InvalidArgumentException e) {
                                                }
                                                return false;
                                            }
                                    );
                                    verifierMap.put("$date-to", (condition, document) -> {
                                                IFieldName fieldName = condition.iterator().next().getKey();
                                                try {
                                                    String entry = (String) document.getValue(fieldName);
                                                    String reference = String.valueOf(
                                                            ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$date-to"))
                                                    );
                                                    return reference.compareTo(entry) == 1 || reference.equals(entry);
                                                } catch (ReadValueException | InvalidArgumentException e) {
                                                }
                                                return false;
                                            }
                                    );
                                    verifierMap.put("$lte", (condition, document) -> {
                                                IFieldName fieldName = condition.iterator().next().getKey();
                                                try {
                                                    Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                                                    ;
                                                    Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$lte"));
                                                    if (reference instanceof Long) {
                                                        Long longEntry = (Long) entry;
                                                        Long longReference = (Long) reference;
                                                        return longReference.compareTo(longEntry) == 1 || longReference.equals(longEntry);
                                                    }
                                                    if (reference instanceof Double) {
                                                        Double doubleReference = (Double) reference;
                                                        Double doubleEntry = Double.parseDouble(entry.toString());
                                                        return doubleReference.compareTo(doubleEntry) == 1 || doubleReference.equals(doubleEntry);
                                                    }
                                                    if (reference instanceof String) {
                                                        String doubleEntry = (String) entry;
                                                        String doubleReference = (String) reference;
                                                        return doubleReference.compareTo(doubleEntry) == 1 || doubleReference.equals(doubleEntry);
                                                    }
                                                } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                                                }
                                                return false;
                                            }
                                    );
                                    verifierMap.put("$in", (condition, document) -> {
                                                IFieldName fieldName = condition.iterator().next().getKey();
                                                try {
                                                    Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                                                    ;
                                                    List<Object> references = (List<Object>)
                                                            ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$in"));
                                                    for (Object reference : references) {
                                                        if (reference.equals(entry)) {
                                                            return true;
                                                        }
                                                    }
                                                } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                                                }
                                                return false;
                                            }
                                    );

                                    verifierMap.put("$isNull", (condition, document) -> {
                                                IFieldName fieldName = condition.iterator().next().getKey();
                                                try {
                                                    Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                                                    ;
                                                    Boolean references = (Boolean)
                                                            ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$isNull"));
                                                    return (null == entry) == references;
                                                } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                                                }
                                                return false;
                                            }
                                    );

                                    verifierMap.put("$and", (condition, document) -> {
                                                boolean result = true;
                                                try {
                                                    List<IObject> conditions = (List<IObject>) condition.getValue(new FieldName("$and"));
                                                    for (IObject conditionItem : conditions) {
                                                        result &= verifierMap.get("$general")
                                                                .verify(conditionItem, document);
                                                    }
                                                } catch (ReadValueException | InvalidArgumentException e) {
                                                }
                                                return result;
                                            }
                                    );
                                    verifierMap.put("$or", (condition, document) -> {
                                                boolean result = false;
                                                try {
                                                    List<IObject> conditions = (List<IObject>) condition.getValue(new FieldName("$or"));
                                                    for (IObject conditionItem : conditions) {
                                                        result |= verifierMap.get("$general")
                                                                .verify(conditionItem, document);
                                                    }
                                                } catch (ReadValueException | InvalidArgumentException e) {
                                                }
                                                return result;
                                            }
                                    );
                                    verifierMap.put("$not", (condition, document) -> {
                                                boolean result = true;
                                                try {
                                                    List<IObject> conditions = (List<IObject>) condition.getValue(new FieldName("$not"));
                                                    for (IObject conditionItem : conditions) {
                                                        result &= !verifierMap.get("$general")
                                                                .verify(conditionItem, document);
                                                    }
                                                } catch (ReadValueException | InvalidArgumentException e) {
                                                }
                                                return result;
                                            }
                                    );

                                    verifierMap.put("$hasTag", (condition, document) -> {
                                                IFieldName fieldName = condition.iterator().next().getKey();
                                                try {
                                                    Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                                                    if (null == entry) {
                                                        return false;
                                                    }
                                                    Object reference = ((IObject) condition.getValue(fieldName)).getValue(new FieldName("$hasTag"));
                                                    if (entry instanceof List) {
                                                        List<Object> entryList = (List<Object>) entry;
                                                        for (Object entryItem : entryList) {
                                                            if (entryItem.equals(reference)) {
                                                                return true;
                                                            }
                                                        }
                                                        return false;
                                                    }

                                                    IFieldName tagFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), reference);
                                                    return null != ((IObject) entry).getValue(tagFieldName);
                                                } catch (Exception e) {
                                                    return false;
                                                }
                                            }
                                    );

                                    IOC.register(Keys.getOrAdd("ResolveDataBaseCondition"), new ApplyFunctionToArgumentsStrategy(
                                                    (args) ->
                                                            verifierMap.get(args[0]).verify((IObject) args[1], (IObject) args[2])
                                            )
                                    );

                                    IOC.register(Keys.getOrAdd("ContainsResolveDataBaseCondition"),
                                            new ApplyFunctionToArgumentsStrategy(
                                                    (args) ->
                                                            verifierMap.containsKey(args[0])
                                            )
                                    );

                                } catch (Exception e) {
                                    throw new ActionExecuteException("Failed to load plugin \"NestedFieldName\"", e);
                                }
                            }
                    );
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }
}
