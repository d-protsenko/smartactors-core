package info.smart_tools.smartactors.core.wrapper_generator;

import info.smart_tools.smartactors.core.wrapper_generator.Field;
import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.wrapper_generator.IWrapper;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.wrapper_generator.IObjectWrapper;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import java.lang.Integer;
import info.smart_tools.smartactors.core.iobject.IObject;
import java.util.Map;
import java.lang.Boolean;
import java.util.List;
import info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper;
import java.lang.String;
import info.smart_tools.smartactors.core.wrapper_generator.TestClass;

public class IWrapperImpl implements IObjectWrapper, IWrapper {
    private Field<java.lang.Integer> fieldFor_getIntValue;
    private Field<Integer> fieldFor_setIntValue;
    private Field<java.lang.String> fieldFor_getStringValue;
    private Field<java.lang.String> fieldFor_setStringValue;
    private Field<info.smart_tools.smartactors.core.wrapper_generator.TestClass> fieldFor_getTestClassValue;
    private Field<info.smart_tools.smartactors.core.wrapper_generator.TestClass> fieldFor_setTestClassValue;
    private Field<java.util.List> fieldFor_getListOfInt;
    private Field<java.util.List<java.lang.Integer>> fieldFor_setListOfInt;
    private Field<java.util.List> fieldFor_getListOfString;
    private Field<java.util.List<java.lang.String>> fieldFor_setListOfString;
    private Field<java.util.List> fieldFor_getListOfTestClasses;
    private Field<java.util.List<info.smart_tools.smartactors.core.wrapper_generator.TestClass>> fieldFor_setListOfTestClasses;
    private Field<java.lang.Boolean> fieldFor_getBoolValue;
    private Field<Boolean> fieldFor_setBoolValue;
    private Field<info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper> fieldFor_getWrappedIObject;
    private Field<info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper> fieldFor_setWrappedIObject;
    private Field<info.smart_tools.smartactors.core.iobject.IObject> fieldFor_getIObject;
    private Field<info.smart_tools.smartactors.core.iobject.IObject> fieldFor_setIObject;
    private Field<info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper> fieldFor_getInnerMapByName;
    private Field<java.util.Map<java.lang.String, info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper>> fieldFor_setStringIInnerMap;
    private IObject env;

    public IWrapperImpl() throws InvalidArgumentException  {
        try {
            this.fieldFor_getIntValue = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/getIntValue");
            this.fieldFor_setIntValue = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/setIntValue");
            this.fieldFor_getStringValue = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/getStringValue");
            this.fieldFor_setStringValue = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/setStringValue");
            this.fieldFor_getTestClassValue = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/getTestClassValue");
            this.fieldFor_setTestClassValue = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/setTestClassValue");
            this.fieldFor_getListOfInt = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/getListOfInt");
            this.fieldFor_setListOfInt = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/setListOfInt");
            this.fieldFor_getListOfString = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/getListOfString");
            this.fieldFor_setListOfString = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/setListOfString");
            this.fieldFor_getListOfTestClasses = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/getListOfTestClasses");
            this.fieldFor_setListOfTestClasses = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/setListOfTestClasses");
            this.fieldFor_getBoolValue = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/getBoolValue");
            this.fieldFor_setBoolValue = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/setBoolValue");
            this.fieldFor_getWrappedIObject = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/getWrappedIObject");
            this.fieldFor_setWrappedIObject = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/setWrappedIObject");
            this.fieldFor_getIObject = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/getIObject");
            this.fieldFor_setIObject = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/setIObject");
            this.fieldFor_getInnerMapByName = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/getInnerMapByName");
            this.fieldFor_setStringIInnerMap = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IWrapper/setStringIInnerMap");
        } catch (Exception e) {
            throw new InvalidArgumentException("", e);
        }

    }

    public void init(IObject environments)  {
        this.env = environments;

    }

    public IObject getEnvironmentIObject(IFieldName fieldName) throws InvalidArgumentException  {
        try {
            return (IObject) this.env.getValue(fieldName);
        } catch (Throwable e) {
            throw new InvalidArgumentException("Could not get IObject from environments.", e);
        }

    }

    public java.lang.Integer getIntValue() throws ReadValueException  {
        try {
            return fieldFor_getIntValue.out(this.env);
        } catch(Throwable e) {
            throw new ReadValueException("Could not get value from iobject.", e);
        }

    }

    public void setIntValue(int value) throws ChangeValueException  {
        try {
            this.fieldFor_setIntValue.in(this.env, value);
        } catch (Throwable e) {
            throw new ChangeValueException("Could not set value from iobject.", e);
        }

    }

    public java.lang.String getStringValue() throws ReadValueException  {
        try {
            return fieldFor_getStringValue.out(this.env);
        } catch(Throwable e) {
            throw new ReadValueException("Could not get value from iobject.", e);
        }

    }

    public void setStringValue(java.lang.String value) throws ChangeValueException  {
        try {
            this.fieldFor_setStringValue.in(this.env, value);
        } catch (Throwable e) {
            throw new ChangeValueException("Could not set value from iobject.", e);
        }

    }

    public info.smart_tools.smartactors.core.wrapper_generator.TestClass getTestClassValue() throws ReadValueException  {
        try {
            return fieldFor_getTestClassValue.out(this.env);
        } catch(Throwable e) {
            throw new ReadValueException("Could not get value from iobject.", e);
        }

    }

    public void setTestClassValue(info.smart_tools.smartactors.core.wrapper_generator.TestClass value) throws ChangeValueException  {
        try {
            this.fieldFor_setTestClassValue.in(this.env, value);
        } catch (Throwable e) {
            throw new ChangeValueException("Could not set value from iobject.", e);
        }

    }

    public java.util.List<java.lang.Integer> getListOfInt() throws ReadValueException  {
        try {
            return fieldFor_getListOfInt.out(this.env);
        } catch(Throwable e) {
            throw new ReadValueException("Could not get value from iobject.", e);
        }

    }

    public void setListOfInt(java.util.List<java.lang.Integer> value) throws ChangeValueException  {
        try {
            this.fieldFor_setListOfInt.in(this.env, value);
        } catch (Throwable e) {
            throw new ChangeValueException("Could not set value from iobject.", e);
        }

    }

    public java.util.List<java.lang.String> getListOfString() throws ReadValueException  {
        try {
            return fieldFor_getListOfString.out(this.env);
        } catch(Throwable e) {
            throw new ReadValueException("Could not get value from iobject.", e);
        }

    }

    public void setListOfString(java.util.List<java.lang.String> value) throws ChangeValueException  {
        try {
            this.fieldFor_setListOfString.in(this.env, value);
        } catch (Throwable e) {
            throw new ChangeValueException("Could not set value from iobject.", e);
        }

    }

    public java.util.List<info.smart_tools.smartactors.core.wrapper_generator.TestClass> getListOfTestClasses() throws ReadValueException  {
        try {
            return fieldFor_getListOfTestClasses.out(this.env);
        } catch(Throwable e) {
            throw new ReadValueException("Could not get value from iobject.", e);
        }

    }

    public void setListOfTestClasses(java.util.List<info.smart_tools.smartactors.core.wrapper_generator.TestClass> value) throws ChangeValueException  {
        try {
            this.fieldFor_setListOfTestClasses.in(this.env, value);
        } catch (Throwable e) {
            throw new ChangeValueException("Could not set value from iobject.", e);
        }

    }

    public java.lang.Boolean getBoolValue() throws ReadValueException  {
        try {
            return fieldFor_getBoolValue.out(this.env);
        } catch(Throwable e) {
            throw new ReadValueException("Could not get value from iobject.", e);
        }

    }

    public void setBoolValue(boolean value) throws ChangeValueException  {
        try {
            this.fieldFor_setBoolValue.in(this.env, value);
        } catch (Throwable e) {
            throw new ChangeValueException("Could not set value from iobject.", e);
        }

    }

    public info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper getWrappedIObject() throws ReadValueException  {
        try {
            return fieldFor_getWrappedIObject.out(this.env);
        } catch(Throwable e) {
            throw new ReadValueException("Could not get value from iobject.", e);
        }

    }

    public void setWrappedIObject(info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper value) throws ChangeValueException  {
        try {
            this.fieldFor_setWrappedIObject.in(this.env, value);
        } catch (Throwable e) {
            throw new ChangeValueException("Could not set value from iobject.", e);
        }

    }

    public info.smart_tools.smartactors.core.iobject.IObject getIObject() throws ReadValueException  {
        try {
            return fieldFor_getIObject.out(this.env);
        } catch(Throwable e) {
            throw new ReadValueException("Could not get value from iobject.", e);
        }

    }

    public void setIObject(info.smart_tools.smartactors.core.iobject.IObject value) throws ChangeValueException  {
        try {
            this.fieldFor_setIObject.in(this.env, value);
        } catch (Throwable e) {
            throw new ChangeValueException("Could not set value from iobject.", e);
        }

    }

    public info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper getInnerMapByName() throws ReadValueException  {
        try {
            return fieldFor_getInnerMapByName.out(this.env);
        } catch(Throwable e) {
            throw new ReadValueException("Could not get value from iobject.", e);
        }

    }

    public void setStringIInnerMap(java.util.Map<java.lang.String, info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper> value) throws ChangeValueException  {
        try {
            this.fieldFor_setStringIInnerMap.in(this.env, value);
        } catch (Throwable e) {
            throw new ChangeValueException("Could not set value from iobject.", e);
        }

    }

}