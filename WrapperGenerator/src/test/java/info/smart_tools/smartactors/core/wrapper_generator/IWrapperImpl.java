package info.smart_tools.smartactors.core.wrapper_generator;

import info.smart_tools.smartactors.core.wrapper_generator.Field;
import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.wrapper_generator.IWrapper;
import java.lang.Integer;
import java.util.Map;
import java.lang.Boolean;
import info.smart_tools.smartactors.core.wrapper_generator.TestClass;
import info.smart_tools.smartactors.core.iobject.IObject;
import java.util.List;
import java.lang.String;
import info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper;

public class IWrapperImpl implements IObjectWrapper, IWrapper {
    private Field<info.smart_tools.smartactors.core.wrapper_generator.TestClass> fieldFor_getTestClassValue;
    private Field<info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper> fieldFor_getWrappedIObject;
    private Field<java.lang.Integer> fieldFor_getIntValue;
    private Field<Integer> fieldFor_setIntValue;
    private Field<java.lang.String> fieldFor_getStringValue;
    private Field<java.lang.String> fieldFor_setStringValue;
    private Field<info.smart_tools.smartactors.core.wrapper_generator.TestClass> fieldFor_setTestClassValue;
    private Field<java.util.List> fieldFor_getListOfInt;
    private Field<java.util.List<java.lang.Integer>> fieldFor_setListOfInt;
    private Field<java.util.List> fieldFor_getListOfString;
    private Field<java.util.List<java.lang.String>> fieldFor_setListOfString;
    private Field<java.util.List> fieldFor_getListOfTestClasses;
    private Field<java.util.List<info.smart_tools.smartactors.core.wrapper_generator.TestClass>> fieldFor_setListOfTestClasses;
    private Field<java.lang.Boolean> fieldFor_getBoolValue;
    private Field<Boolean> fieldFor_setBoolValue;
    private Field<info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper> fieldFor_setWrappedIObject;
    private Field<info.smart_tools.smartactors.core.iobject.IObject> fieldFor_getIObject;
    private Field<info.smart_tools.smartactors.core.iobject.IObject> fieldFor_setIObject;
    private Field<java.util.Map> fieldFor_getStringIInnerMap;
    private Field<java.util.Map<java.lang.String, info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper>> fieldFor_setStringIInnerMap;
    private IObject message;
    private IObject context;
    private IObject response;

    public IWrapperImpl() throws InvalidArgumentException  {
        try {
            this.fieldFor_getTestClassValue = new Field<>(new FieldName("TestClassValue"));
            this.fieldFor_getWrappedIObject = new Field<>(new FieldName("WrappedIObject"));
            this.fieldFor_getIntValue = new Field<>(new FieldName("IntValue"));
            this.fieldFor_setIntValue = new Field<>(new FieldName("IntValue"));
            this.fieldFor_getStringValue = new Field<>(new FieldName("StringValue"));
            this.fieldFor_setStringValue = new Field<>(new FieldName("StringValue"));
            this.fieldFor_setTestClassValue = new Field<>(new FieldName("TestClassValue"));
            this.fieldFor_getListOfInt = new Field<>(new FieldName("ListOfInt"));
            this.fieldFor_setListOfInt = new Field<>(new FieldName("ListOfInt"));
            this.fieldFor_getListOfString = new Field<>(new FieldName("ListOfString"));
            this.fieldFor_setListOfString = new Field<>(new FieldName("ListOfString"));
            this.fieldFor_getListOfTestClasses = new Field<>(new FieldName("ListOfTestClasses"));
            this.fieldFor_setListOfTestClasses = new Field<>(new FieldName("ListOfTestClasses"));
            this.fieldFor_getBoolValue = new Field<>(new FieldName("BoolValue"));
            this.fieldFor_setBoolValue = new Field<>(new FieldName("BoolValue"));
            this.fieldFor_setWrappedIObject = new Field<>(new FieldName("WrappedIObject"));
            this.fieldFor_getIObject = new Field<>(new FieldName("IObject"));
            this.fieldFor_setIObject = new Field<>(new FieldName("IObject"));
            this.fieldFor_getStringIInnerMap = new Field<>(new FieldName("StringIInnerMap"));
            this.fieldFor_setStringIInnerMap = new Field<>(new FieldName("StringIInnerMap"));
        } catch (Exception e) {
            throw new InvalidArgumentException("", e);
        }

    }

    public IObject getMessage()  {
        return this.message;

    }

    public IObject getContext()  {
        return this.context;

    }

    public IObject getResponse()  {
        return this.response;

    }

    public void init(IObject message, IObject context, IObject response)  {
        this.message = message;
        this.context = context;
        this.response = response;

    }

    public info.smart_tools.smartactors.core.wrapper_generator.TestClass getTestClassValue()  {
        try {
            return fieldFor_getTestClassValue.from(message, "ToTestClass");
        } catch(Throwable e) {
            throw new RuntimeException("Could not get value from iobject.", e);
        }

    }

    public info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper getWrappedIObject()  {
        try {
            return fieldFor_getWrappedIObject.from(message, IInnerWrapper.class);
        } catch(Throwable e) {
            throw new RuntimeException("Could not get value from iobject.", e);
        }

    }

    public java.lang.Integer getIntValue()  {
        try {
            return fieldFor_getIntValue.from(message, Integer.class);
        } catch(Throwable e) {
            throw new RuntimeException("Could not get value from iobject.", e);
        }

    }

    public void setIntValue(int value)  {
        try {
            this.fieldFor_setIntValue.inject(response, value);
        } catch (Throwable e) {
            throw new RuntimeException("Could not set value from iobject.", e);
        }

    }

    public java.lang.String getStringValue()  {
        try {
            return fieldFor_getStringValue.from(message, String.class);
        } catch(Throwable e) {
            throw new RuntimeException("Could not get value from iobject.", e);
        }

    }

    public void setStringValue(java.lang.String value)  {
        try {
            this.fieldFor_setStringValue.inject(response, value);
        } catch (Throwable e) {
            throw new RuntimeException("Could not set value from iobject.", e);
        }

    }

    public void setTestClassValue(info.smart_tools.smartactors.core.wrapper_generator.TestClass value)  {
        try {
            this.fieldFor_setTestClassValue.inject(response, value);
        } catch (Throwable e) {
            throw new RuntimeException("Could not set value from iobject.", e);
        }

    }

    public java.util.List<java.lang.Integer> getListOfInt()  {
        try {
            return fieldFor_getListOfInt.from(message, "ToListOfInt");
        } catch(Throwable e) {
            throw new RuntimeException("Could not get value from iobject.", e);
        }

    }

    public void setListOfInt(java.util.List<java.lang.Integer> value)  {
        try {
            this.fieldFor_setListOfInt.inject(response, value);
        } catch (Throwable e) {
            throw new RuntimeException("Could not set value from iobject.", e);
        }

    }

    public java.util.List<java.lang.String> getListOfString()  {
        try {
            return fieldFor_getListOfString.from(message, List.class);
        } catch(Throwable e) {
            throw new RuntimeException("Could not get value from iobject.", e);
        }

    }

    public void setListOfString(java.util.List<java.lang.String> value)  {
        try {
            this.fieldFor_setListOfString.inject(response, value);
        } catch (Throwable e) {
            throw new RuntimeException("Could not set value from iobject.", e);
        }

    }

    public java.util.List<info.smart_tools.smartactors.core.wrapper_generator.TestClass> getListOfTestClasses()  {
        try {
            return fieldFor_getListOfTestClasses.from(message, "ToListOfTestClasses");
        } catch(Throwable e) {
            throw new RuntimeException("Could not get value from iobject.", e);
        }

    }

    public void setListOfTestClasses(java.util.List<info.smart_tools.smartactors.core.wrapper_generator.TestClass> value)  {
        try {
            this.fieldFor_setListOfTestClasses.inject(response, value);
        } catch (Throwable e) {
            throw new RuntimeException("Could not set value from iobject.", e);
        }

    }

    public java.lang.Boolean getBoolValue()  {
        try {
            return fieldFor_getBoolValue.from(message, Boolean.class);
        } catch(Throwable e) {
            throw new RuntimeException("Could not get value from iobject.", e);
        }

    }

    public void setBoolValue(boolean value)  {
        try {
            this.fieldFor_setBoolValue.inject(response, value);
        } catch (Throwable e) {
            throw new RuntimeException("Could not set value from iobject.", e);
        }

    }

    public void setWrappedIObject(info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper value)  {
        try {
            this.fieldFor_setWrappedIObject.inject(response, value);
        } catch (Throwable e) {
            throw new RuntimeException("Could not set value from iobject.", e);
        }

    }

    public info.smart_tools.smartactors.core.iobject.IObject getIObject()  {
        try {
            return fieldFor_getIObject.from(message, IObject.class);
        } catch(Throwable e) {
            throw new RuntimeException("Could not get value from iobject.", e);
        }

    }

    public void setIObject(info.smart_tools.smartactors.core.iobject.IObject value)  {
        try {
            this.fieldFor_setIObject.inject(response, value);
        } catch (Throwable e) {
            throw new RuntimeException("Could not set value from iobject.", e);
        }

    }

    public java.util.Map<java.lang.String, info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper> getStringIInnerMap()  {
        try {
            return fieldFor_getStringIInnerMap.from(context, Map.class);
        } catch(Throwable e) {
            throw new RuntimeException("Could not get value from iobject.", e);
        }

    }

    public void setStringIInnerMap(java.util.Map<java.lang.String, info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper> value)  {
        try {
            this.fieldFor_setStringIInnerMap.inject(context, value);
        } catch (Throwable e) {
            throw new RuntimeException("Could not set value from iobject.", e);
        }

    }

}