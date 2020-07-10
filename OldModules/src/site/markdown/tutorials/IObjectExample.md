# How to use IObject

[`IObject`](../apidocs/info/smart_tools/smartactors/core/iobject/IObject.html) is the base interface for semi-structured data.
It's widely used in SmartActors to represent complex configuration options, method parameters, messages, etc...
Think of IObject as of [JSON](https://en.wikipedia.org/wiki/JSON) object with named fields and some values for each field.

## IFieldName

The name of the field of IObject is presented as [`IFieldName`](../apidocs/info/smart_tools/smartactors/core/ifield_name/IFieldName.html) instance.

In tests you can construct IFieldName using trivial implementation and `new` operator.

    IFieldName fieldName = new FieldName("name");
    
However, the recommended way to get IFieldName is to resolve it from [IOC](IOCExample.html).

    IFieldName fieldName = IOC.resolve(
            Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
            "name");
        
You need the initialized IOC and a plugin which registers the appropriate strategy to resolve IFieldName.
For example, [IFieldNamePlugin](../apidocs/info/smart_tools/smartactors/plugin/ifieldname/IFieldNamePlugin.html).

Note, using of canonical name of the class or interface, which is returned by `getCanonicalName()` is the convenience key for strategies resolving the specified class or interface.

IFieldName can be converted to String as expected.

    assertEquals("name", fieldName.toString());
    
### Accessing fields of IObject

When you have IFieldName, you can put values to IObject.

    object.setValue(fieldName, value);
    
The `setValue()` method throws `ChangeValueException` if currently it's not possible to set the value.

The value cannot be `null`, `InvalidArgumentException` is thrown in this case.

You can take the value from IObject.

    object.getValue(fieldName);
    
The `ReadValueException` can be thrown here.

You can delete the previously set field.

    object.deleteField(fieldName);
    
This can throw `DeleteValueException`.

After the deletion reading of the same field will return `null`.

### Iteration

It's possible to iterate over IObject to inspect all it's fields.

    for (Map.Entry<IFieldName, Object> entry : object) {
        IFieldName fieldName = entry.getKey();
        Object value = entry.getValue();
        // do something with fieldName and value
    }
    
However, it's not recommended to use iteration, try to access the specified, known and fixed set of fields. Don't try to work with a field you don't know about.
 
## IField

Another way to access fields of IObject is [`IField`](../apidocs/info/smart_tools/smartactors/core/ifield/IField.html).

In tests you can create the trivial implementation of IField from IFieldName.

    IField field = new Field(fieldName);
    
However, it's recommended to resolve the IField from IOC.

    IField field = IOC.resolve(
            Keys.getKeyByName(IField.class.getCanonicalName()),
            "name");
    
You need the initialized IOC and a plugin which registers the appropriate strategy to resolve IFieldName.
For example, [IFieldPlugin](../apidocs/info/smart_tools/smartactors/plugin/ifield/IFieldPlugin.html).

### Accessing fields of IObject

The idea of IField is that IObject is a handler of some data external to this code.
So you can take data IN and put data OUT.
Depending on IField implementation some transformation rules may be applied to the data,
in case of [Wrapper](WrapperExample.html) such rules can be configured externally.
Also the value can be casted to necessary data type.

To set the IObject field put the data out.

    field.out(object, value);
    
To get the IObject field take the data in.

    field.in(object);

IField may cast the value to necessary type using [type conversion](TypeConversionExample.html) rules.

    String stringValue = "123";
    field.out(object, stringValue);
    Integer intValue = field.in(object, Integer.class);
        
You need the initialized IOC and a plugin which registers the appropriate strategies to convert data types.
For example, [ResolveStandardTypesStrategiesPlugin](../apidocs/info/smart_tools/smartactors/plugin/resolve_standard_types_strategies/ResolveStandardTypesStrategiesPlugin.html).

## Creation of IObject
      
Usually you don't need to create IObject instances, because typically they already came to you code outside.
However, when you need to construct some data to pass, for example, to [database tasks](DBCollectionExample.html), you need to create a new IObject.

In tests you can use trivial IObject implementation: [`DSObject`](../apidocs/info/smart_tools/smartactors/core/ds_object/DSObject.html).

You can create empty IObject, to add fields later.
    
    IObject object = new DSObject();
    
You can create IObject from JSON text.
Note it must be JSON object, in curly braces, not array or a single value, because IObject is the object.
Don't forget quotation marks around the field name, JSON standard requires them.

    IObject object = new DSObject("{ \"name\": \"value\" }");
    
Also you can create IObject from a [Map](http://docs.oracle.com/javase/8/docs/api/java/util/Map.html) of IFieldName and Object value.

    Map<IFieldName, Object> map = new HashMap<>();
    map.put(fieldName, "value");
    IObject object = new DSObject(map);

Note, the recommended way to create a new IObject is to resolve it from IOC.

You can resolve empty IObject.

    IObject object = IOC.resolve(
            Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"));
            
You can resolve IObject from JSON string.

    IObject object = IOC.resolve(
            Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
            "{ \"name\": \"value\" }");
            
You need the initialized IOC and a plugin which registers the appropriate strategies to create IObject from IOC.
For example, [PluginDSObject](../apidocs/info/smart_tools/smartactors/plugin/dsobject/PluginDSObject.html)            

## Serialization
            
DSObject implementation of IObject can be serialized to JSON String.
            
    IObject object = IOC.resolve(
            Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
            "{ \"name\": \"value\" }");
    assertEquals("{\"name\":\"value\"}", object.serialize());
    
The `serialize()` call may throw `SerializeException`.

The `<T> T serialize()` is the generic method, 
so other implementations may serialize to another kind of data.
            
## Data types

For IObject it's values are always just Java Objects.
It doesn't care about the actual data type.
It even allow to store any Java object, which doesn't have a good serializable JSON representation.

But it case of conversion from JSON you need to know the Java types of the objects.

### Objects

JSON objects in curly braces are always presented as IObject.

    IObject object = IOC.resolve(
            Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
            "{ \"name\": { \"nested\": \"object\" } }");
    IObject value = field.in(object);

### Strings    
    
Strings are represented as String.

    IObject object = IOC.resolve(
            Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
            "{ \"name\": \"value\" }");
    String value = field.in(object);

### Numbers    
    
JSON doesn't distinguish different kind of numbers,
so numeric values in JSON can be presented as Integer, Long, Double or even BigDecimal.
It's always safe to retrieve them as Number.

    IObject object = IOC.resolve(
            Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
            "{ \"int\": 42, \"float\": 42.42 }");
    Number intValue = intField.in(object);
    assertEquals(42, intValue.intValue());
    Number floatValue = floatField.in(object);
    assertEquals(42.42, floatValue.doubleValue(), 0.01);
    
Or you can use conversion abilities of Field to convert to necessary type.

    IObject object = IOC.resolve(
            Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
            "{ \"value\": 42.42 }");
    Integer intValue = field.in(object, Integer.class);
    Double doubleValue = field.in(object, Double.class);
    BigDecimal decimalValue = field.in(object, BigDecimal.class);
    
### Dates

JSON doesn't have any special type for dates.
You should use [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) String representation of dates,
and retrieve them as [LocalDateTime](http://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html).

    IObject object = IOC.resolve(
            Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
            "{ \"date\": \"2016-08-22T13:38:42\" }");
    LocalDateTime dateTime = field.in(object, LocalDateTime.class);
    
### Arrays
    
JSON arrays are presented as Lists of the specified type.
If it's array of objects, it'll be `List<IObject>`,
if it's array of strings, it'll be `List<String>`, etc...

    IObject object = IOC.resolve(
            Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
            "{ \"array\": [ \"a\", \"b\", \"c\" ] }");
    List<String> array = field.in(object);
    assertEquals("a", array.get(0));
    
Note, JSON allows to mix different data types in the same array.
In this case you have to work with `List<Object>` in Java and cast each element to necessary type individually.
      
## Code
      
* [Tests](../core.examples/xref-test/info/smart_tools/smartactors/core/examples/IObjectExample.html) showing examples to work with IObject.
