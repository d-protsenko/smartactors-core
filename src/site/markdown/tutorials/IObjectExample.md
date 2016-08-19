# How to use IObject

[`IObject`](http://smarttools.github.io/smartactors-core/develop/apidocs/info/smart_tools/smartactors/core/iobject/IObject.html) is the base interface for semi-structured data.
It's widely used in SmartActors to represent complex configuration options, method parameters, messages, etc...
Think of IObject as of [JSON](https://en.wikipedia.org/wiki/JSON) object with named fields and some values for each field.

## IFieldName

The name of the field of IObject is presented as [`IFieldName`](http://smarttools.github.io/smartactors-core/apidocs/info/smart_tools/smartactors/core/ifield_name/IFieldName.html) instance.

In tests you can construct IFieldName using trivial implementation and `new` operator.

    IFieldName fieldName = new FieldName("name");
    
However, the recommended way to get IFieldName is to resolve it from [IOC](IOCExample.html).

    IFieldName fieldName = IOC.resolve(
        Keys.getOrAdd(IFieldName.class.getCanonicalName()), 
        "name");
        
You need the initialized IOC and a plugin which registers the appropriate strategy to resolve IFieldName.
For example, [IFieldNamePlugin](http://smarttools.github.io/smartactors-core/apidocs/info/smart_tools/smartactors/plugin/ifieldname/IFieldNamePlugin.html).

Note, using of canonical name of the class or interface, which is returned by `getCanonicalName()` is the convenience key for strategies resolving the specified class or interface.

IFieldName can be converted to String as expected.

    assertEquals("name", fieldName.toString());
    
## Accessing fields of IObject

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


 
## IField

Another way to access fields of IObject
