package info.smart_tools.smartactors.message_processing.wrapper_generator;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.field.field.Field;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.DeleteValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;
import info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class IWrapperImpl implements IObjectWrapper, IObject, IWrapper {
    private IField fieldFor_in_getIntValue;
    private IField fieldFor_out_setListOfTestClasses;
    private IField fieldFor_out_setIntValue;
    private IField fieldFor_out_transform;
    private IField fieldFor_in_getTestClassValue;
    private IField fieldFor_out_setTestClassValue;
    private IField fieldFor_in_getListOfTestClasses;
    private IField fieldFor_out_wrappedIObject;
    private IField fieldFor_in_wrappedIObject;
    private Map<IFieldName,Field> fields;
    private IObject env;

    public IWrapperImpl() throws InvalidArgumentException  {
        try {
            this.fieldFor_in_getIntValue = new Field(new FieldName("in_getIntValue"));
            this.fieldFor_out_setListOfTestClasses = new Field(new FieldName("out_setListOfTestClasses"));
            this.fieldFor_out_setIntValue = new Field(new FieldName("out_setIntValue"));
            this.fieldFor_out_transform = new Field(new FieldName("out_transform"));
            this.fieldFor_in_getTestClassValue = new Field(new FieldName("in_getTestClassValue"));
            this.fieldFor_out_setTestClassValue = new Field(new FieldName("out_setTestClassValue"));
            this.fieldFor_in_getListOfTestClasses = new Field(new FieldName("in_getListOfTestClasses"));
            this.fieldFor_out_wrappedIObject = new Field(new FieldName("out_wrappedIObject"));
            this.fieldFor_in_wrappedIObject = new Field(new FieldName("in_wrappedIObject"));
            this.fields = new HashMap<>();
        } catch (Exception e) {
            throw new InvalidArgumentException("", e);
        }

    }

    public void init(IObject environments)  {
        this.env = environments;

    }

    public IObject getEnvironmentIObject(IFieldName fieldName) throws InvalidArgumentException  {
        try {
            if (IObjectWrapper.class.isAssignableFrom(this.env.getClass())) {
                return ((IObjectWrapper) this.env).getEnvironmentIObject(fieldName);
            }
            return (IObject) this.env.getValue(fieldName);
        } catch (Throwable e) {
            throw new InvalidArgumentException("Could not get IObject from environments.", e);
        }

    }

    public java.lang.Integer getIntValue() throws ReadValueException  {
        try {
            return fieldFor_in_getIntValue.in(this.env, Integer.class);
        } catch(Throwable e) {
            throw new ReadValueException("Could not get value from iobject.", e);
        }

    }

    public void setListOfTestClasses(java.util.List<TestClass> value) throws ChangeValueException  {
        try {
            this.fieldFor_out_setListOfTestClasses.out(this.env, value);
        } catch (Throwable e) {
            throw new ChangeValueException("Could not set value from iobject.", e);
        }

    }

    public void setIntValue(int value) throws ChangeValueException  {
        try {
            this.fieldFor_out_setIntValue.out(this.env, value);
        } catch (Throwable e) {
            throw new ChangeValueException("Could not set value from iobject.", e);
        }

    }

    public void transform(java.lang.Integer value) throws ChangeValueException  {
        try {
            this.fieldFor_out_transform.out(this.env, value);
        } catch (Throwable e) {
            throw new ChangeValueException("Could not set value from iobject.", e);
        }

    }

    public TestClass getTestClassValue() throws ReadValueException  {
        try {
            return fieldFor_in_getTestClassValue.in(this.env, TestClass.class);
        } catch(Throwable e) {
            throw new ReadValueException("Could not get value from iobject.", e);
        }

    }

    public void setTestClassValue(TestClass value) throws ChangeValueException  {
        try {
            this.fieldFor_out_setTestClassValue.out(this.env, value);
        } catch (Throwable e) {
            throw new ChangeValueException("Could not set value from iobject.", e);
        }

    }

    public java.util.List<TestClass> getListOfTestClasses() throws ReadValueException  {
        try {
            return fieldFor_in_getListOfTestClasses.in(this.env, List.class);
        } catch(Throwable e) {
            throw new ReadValueException("Could not get value from iobject.", e);
        }

    }

    public void wrappedIObject(IInnerWrapper value) throws ChangeValueException  {
        try {
            this.fieldFor_out_wrappedIObject.out(this.env, value);
        } catch (Throwable e) {
            throw new ChangeValueException("Could not set value from iobject.", e);
        }

    }

    public IInnerWrapper wrappedIObject() throws ReadValueException  {
        try {
            return fieldFor_in_wrappedIObject.in(this.env, IInnerWrapper.class);
        } catch(Throwable e) {
            throw new ReadValueException("Could not get value from iobject.", e);
        }

    }

    public Object getValue(IFieldName name) throws ReadValueException, InvalidArgumentException  {
        Field field = fields.get(name);
        if (null == field) {
            field = new Field(name);
            fields.put(name, field);
        }
        return new Field(name).in(this.env);

    }

    public void setValue(IFieldName name, Object value) throws ChangeValueException, InvalidArgumentException  {
        Field field = fields.get(name);
        if (null == field) {
            field = new Field(name);
            fields.put(name, field);
        }
        new Field(name).out(env, value);

    }

    public void deleteField(IFieldName name) throws DeleteValueException, InvalidArgumentException  {
        throw new DeleteValueException("Method not implemented.");

    }

    public <T> T serialize() throws SerializeException  {
        throw new SerializeException("Method not implemented.");

    }

    public Iterator<Map.Entry<IFieldName, Object>> iterator()  {
        return null;

    }

}