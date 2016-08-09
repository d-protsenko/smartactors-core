package info.smart_tools.smartactors.plugin.in_memory_database;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.in_memory_database.DataBaseItem;
import info.smart_tools.smartactors.core.in_memory_database.IConditionVerifier;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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

                                    IOC.register(Keys.getOrAdd("CompareSimpleObjects"), new ApplyFunctionToArgumentsStrategy(
                                                    (args) -> {
                                                        Object entry = args[0];
                                                        Object reference = args[1];
                                                        if (reference instanceof Long) {
                                                            Long longEntry = (Long) entry;
                                                            Long longReference = (Long) reference;
                                                            return -longReference.compareTo(longEntry);
                                                        }
                                                        if (reference instanceof Double) {
                                                            Double doubleReference = (Double) reference;
                                                            Double doubleEntry = Double.parseDouble(entry.toString());
                                                            return -doubleReference.compareTo(doubleEntry);
                                                        }
                                                        if (reference instanceof String) {
                                                            String doubleEntry = (String) entry;
                                                            String doubleReference = (String) reference;
                                                            return -doubleReference.compareTo(doubleEntry);
                                                        }
                                                        return 0;
                                                    }
                                            )
                                    );

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
                                                                IFieldName bufFieldName =
                                                                        IOC.resolve(
                                                                                Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                                                                                fieldString
                                                                        );
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
                                                    Object reference = ((IObject) condition.getValue(fieldName))
                                                            .getValue(new FieldName("$eq"));
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
                                                    Object reference = ((IObject) condition.getValue(fieldName))
                                                            .getValue(new FieldName("$neq"));
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
                                                    Object reference = ((IObject) condition.getValue(fieldName))
                                                            .getValue(new FieldName("$gt"));
                                                    return ((Integer) IOC.resolve(
                                                            Keys.getOrAdd("CompareSimpleObjects"), entry, reference)
                                                    ) > 0;
                                                } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                                                }
                                                return false;
                                            }
                                    );
                                    verifierMap.put("$lt", (condition, document) -> {
                                                IFieldName fieldName = condition.iterator().next().getKey();
                                                try {
                                                    Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                                                    Object reference = ((IObject) condition.getValue(fieldName))
                                                            .getValue(new FieldName("$lt"));
                                                    return (Integer) IOC.resolve(
                                                            Keys.getOrAdd("CompareSimpleObjects"), entry, reference
                                                    ) < 0;
                                                } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                                                }
                                                return false;
                                            }
                                    );
                                    verifierMap.put("$gte", (condition, document) -> {
                                                IFieldName fieldName = condition.iterator().next().getKey();
                                                try {
                                                    Object entry = IOC.resolve(Keys.getOrAdd("NestedFieldName"), fieldName, document);
                                                    Object reference = ((IObject) condition.getValue(fieldName))
                                                            .getValue(new FieldName("$gte"));
                                                    return (Integer) IOC.resolve(
                                                            Keys.getOrAdd("CompareSimpleObjects"), entry, reference
                                                    ) >= 0;
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
                                                            ((IObject) condition.getValue(fieldName))
                                                                    .getValue(new FieldName("$date-from"))
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
                                                            ((IObject) condition.getValue(fieldName))
                                                                    .getValue(new FieldName("$date-to"))
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
                                                    Object entry = IOC.resolve(
                                                            Keys.getOrAdd("NestedFieldName"),
                                                            fieldName,
                                                            document
                                                    );
                                                    Object reference = ((IObject) condition.getValue(fieldName))
                                                            .getValue(new FieldName("$lte"));
                                                    return (Integer) IOC.resolve(
                                                            Keys.getOrAdd("CompareSimpleObjects"), entry, reference
                                                    ) <= 0;
                                                } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                                                }
                                                return false;
                                            }
                                    );
                                    verifierMap.put("$in", (condition, document) -> {
                                                IFieldName fieldName = condition.iterator().next().getKey();
                                                try {
                                                    Object entry = IOC.resolve(
                                                            Keys.getOrAdd("NestedFieldName"),
                                                            fieldName,
                                                            document
                                                    );
                                                    List<Object> references = (List<Object>)
                                                            ((IObject) condition.getValue(fieldName))
                                                                    .getValue(new FieldName("$in"));
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
                                                    Object entry = IOC.resolve(
                                                            Keys.getOrAdd("NestedFieldName"),
                                                            fieldName,
                                                            document
                                                    );
                                                    Boolean references = (Boolean)
                                                            ((IObject) condition.getValue(fieldName))
                                                                    .getValue(new FieldName("$isNull"));
                                                    return (null == entry) == references;
                                                } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                                                }
                                                return false;
                                            }
                                    );

                                    verifierMap.put("$and", (condition, document) -> {
                                                boolean result = true;
                                                try {
                                                    List<IObject> conditions = (List<IObject>) condition
                                                            .getValue(new FieldName("$and"));
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
                                                    List<IObject> conditions = (List<IObject>) condition
                                                            .getValue(new FieldName("$or"));
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
                                                    List<IObject> conditions = (List<IObject>) condition
                                                            .getValue(new FieldName("$not"));
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
                                                    Object entry = IOC.resolve(
                                                            Keys.getOrAdd("NestedFieldName"),
                                                            fieldName,
                                                            document
                                                    );
                                                    if (null == entry) {
                                                        return false;
                                                    }
                                                    Object reference = ((IObject) condition.getValue(fieldName))
                                                            .getValue(new FieldName("$hasTag"));
                                                    if (entry instanceof List) {
                                                        List<Object> entryList = (List<Object>) entry;
                                                        for (Object entryItem : entryList) {
                                                            if (entryItem.equals(reference)) {
                                                                return true;
                                                            }
                                                        }
                                                        return false;
                                                    }

                                                    IFieldName tagFieldName = IOC.resolve(
                                                            Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                                                            reference
                                                    );
                                                    return null != ((IObject) entry).getValue(tagFieldName);
                                                } catch (Exception e) {
                                                    return false;
                                                }
                                            }
                                    );

                                    IOC.register(
                                            Keys.getOrAdd("ResolveDataBaseCondition"),
                                            new ApplyFunctionToArgumentsStrategy(
                                                    (args) ->
                                                            verifierMap.get(args[0])
                                                                    .verify((IObject) args[1], (IObject) args[2])
                                            )
                                    );

                                    IOC.register(Keys.getOrAdd("ContainsResolveDataBaseCondition"),
                                            new ApplyFunctionToArgumentsStrategy(
                                                    (args) ->
                                                            verifierMap.containsKey(args[0])
                                            )
                                    );
                                    IOC.register(Keys.getOrAdd("PagingForDatabaseCollection"),
                                            new ApplyFunctionToArgumentsStrategy(
                                                    (args) -> {
                                                        IObject page = null;
                                                        Integer pageNumber = 0;
                                                        Integer pageSize = 0;
                                                        try {
                                                            IFieldName pageFieldName = IOC.resolve(
                                                                    Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                                                                    "page"
                                                            );
                                                            IFieldName pageNumberFieldName = IOC.resolve(
                                                                    Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                                                                    "number"
                                                            );
                                                            IFieldName pageSizeFieldName = IOC.resolve(
                                                                    Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                                                                    "size"
                                                            );
                                                            page = (IObject) ((IObject) args[0]).getValue(pageFieldName);
                                                            if (null == page) {
                                                                pageNumber = 1;
                                                                pageSize = 100;
                                                            } else {
                                                                pageNumber = (Integer) page.getValue(pageNumberFieldName);
                                                                pageSize = (Integer) page.getValue(pageSizeFieldName);
                                                            }
                                                        } catch (ResolutionException | ReadValueException | InvalidArgumentException e) {
                                                        }
                                                        Integer startNumber = (pageNumber - 1) * pageSize;
                                                        Integer finNumber = pageNumber * pageSize;
                                                        List<IObject> outputDocuments = new LinkedList<>();
                                                        List<IObject> documents = (List<IObject>) args[1];
                                                        for (int i = startNumber; i < finNumber && i < documents.size(); i++) {
                                                            outputDocuments.add(documents.get(i));
                                                        }
                                                        return outputDocuments;
                                                    }
                                            )
                                    );
                                    IOC.register(Keys.getOrAdd("SortIObjects"), new ApplyFunctionToArgumentsStrategy(
                                                    (args) -> {
                                                        IObject condition = (IObject) args[0];
                                                        List<IObject> documents = (List<IObject>) args[1];

                                                        IFieldName sortFieldName = null;
                                                        try {
                                                            sortFieldName = IOC.resolve(
                                                                    Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                                                                    "sort"
                                                            );
                                                        } catch (ResolutionException e) {
                                                            e.printStackTrace();
                                                        }
                                                        List<IObject> sortRules = null;
                                                        try {
                                                            sortRules = (List<IObject>) condition.getValue(sortFieldName);
                                                        } catch (ReadValueException | InvalidArgumentException e) {
                                                            e.printStackTrace();
                                                        }
                                                        if (null == sortRules) {
                                                            return documents;
                                                        }
                                                        List<IFieldName> sortingFields = new LinkedList<>();
                                                        List<Integer> sortingType = new LinkedList<>();
                                                        for (IObject sortRule : sortRules) {
                                                            sortingFields.add(sortRule.iterator().next().getKey());
                                                            sortingType.add(sortRule.iterator().next()
                                                                    .getValue().equals("asc") ? 1 : -1);
                                                        }
                                                        Comparator comparator = (o1, o2) -> {
                                                            try {
                                                                Integer compare = 0;
                                                                for (int i = 0; i < sortingFields.size(); i++) {
                                                                    Object object1 = IOC.resolve(
                                                                            Keys.getOrAdd("NestedFieldName"),
                                                                            sortingFields.get(i),
                                                                            o1
                                                                    );
                                                                    Object object2 = IOC.resolve(
                                                                            Keys.getOrAdd("NestedFieldName"),
                                                                            sortingFields.get(i),
                                                                            o2
                                                                    );
                                                                    compare = IOC.resolve(
                                                                            Keys.getOrAdd("CompareSimpleObjects"),
                                                                            object1,
                                                                            object2
                                                                    );
                                                                    if (compare != 0) {
                                                                        return compare * sortingType.get(i);
                                                                    }
                                                                }
                                                            } catch (ResolutionException e) {
                                                            }
                                                            return 0;
                                                        };
                                                        documents.sort(comparator);
                                                        return documents;

                                                    }
                                            )
                                    );
                                    IOC.register(Keys.getOrAdd(DataBaseItem.class.getCanonicalName()), new CreateNewInstanceStrategy(
                                                    (args) -> {
                                                        try {
                                                            return new DataBaseItem((IObject) args[0], (String) args[1]);
                                                        } catch (ResolutionException | ReadValueException | InvalidArgumentException e) {
                                                        }
                                                        return null;
                                                    }
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
