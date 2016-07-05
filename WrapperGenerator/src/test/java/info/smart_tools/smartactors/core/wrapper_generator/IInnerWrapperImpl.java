package info.smart_tools.smartactors.core.wrapper_generator;

import info.smart_tools.smartactors.core.wrapper_generator.Field;
import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.wrapper_generator.IObjectWrapper;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import java.lang.Double;

public class IInnerWrapperImpl implements IObjectWrapper, IInnerWrapper {
    private Field<java.lang.Double> fieldFor_getDoubleValue;
    private Field<java.lang.Double> fieldFor_setDoubleValue;
    private IObject env;

    public IInnerWrapperImpl() throws InvalidArgumentException  {
        try {
            this.fieldFor_getDoubleValue = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper/getDoubleValue");
            this.fieldFor_setDoubleValue = new Field<>("binding/info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper/setDoubleValue");
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

    public java.lang.Double getDoubleValue() throws ReadValueException  {
        try {
            return fieldFor_getDoubleValue.out(this.env);
        } catch(Throwable e) {
            throw new ReadValueException("Could not get value from iobject.", e);
        }

    }

    public void setDoubleValue(java.lang.Double value) throws ChangeValueException  {
        try {
            this.fieldFor_setDoubleValue.in(this.env, value);
        } catch (Throwable e) {
            throw new ChangeValueException("Could not set value from iobject.", e);
        }

    }

}