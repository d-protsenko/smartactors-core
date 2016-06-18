package info.smart_tools.smartactors.core.wrapper_generator;
import info.smart_tools.smartactors.core.wrapper_generator.Field;
import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.wrapper_generator.IWrapper;
import java.lang.Integer;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.wrapper_generator.TestClass;
import java.lang.Boolean;
import java.util.List;
import info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper;
import java.lang.String;
public class IWrapperImpl implements IObjectWrapper, IWrapper {
    private Field<java.util.List<java.util.Map<java.lang.String, java.util.List<info.smart_tools.smartactors.core.wrapper_generator.TestClass>>>> fieldFor_getIntValue;
    private Field<Integer> fieldFor_setIntValue;
    private Field<java.lang.String> fieldFor_getStringValue;
    private Field<java.lang.String> fieldFor_setStringValue;
    private Field<info.smart_tools.smartactors.core.wrapper_generator.TestClass> fieldFor_getTestClassValue;
    private Field<info.smart_tools.smartactors.core.wrapper_generator.TestClass> fieldFor_setTestClassValue;
    private Field<java.util.List<java.lang.Integer>> fieldFor_getListOfInt;
    private Field<java.util.List<java.lang.Integer>> fieldFor_setListOfInt;
    private Field<java.util.List<java.lang.String>> fieldFor_getListOfString;
    private Field<java.util.List<java.lang.String>> fieldFor_setListOfString;
    private Field<java.util.List<info.smart_tools.smartactors.core.wrapper_generator.TestClass>> fieldFor_getListOfTestClasses;
    private Field<java.util.List<info.smart_tools.smartactors.core.wrapper_generator.TestClass>> fieldFor_setListOfTestClasses;
    private Field<java.lang.Boolean> fieldFor_isBoolValue;
    private Field<Boolean> fieldFor_setBoolValue;
    private Field<java.lang.Boolean> fieldFor_hasValue;
    private Field<info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper> fieldFor_getWrappedIObject;
    private Field<info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper> fieldFor_setWrappedIObject;
    private Field<info.smart_tools.smartactors.core.iobject.IObject> fieldFor_getIObject;
    private Field<info.smart_tools.smartactors.core.iobject.IObject> fieldFor_setIObject;
    private Field<java.lang.Integer> fieldFor_countCValue;
    public IWrapperImpl() throws InvalidArgumentException {
        this.fieldFor_getIntValue = new Field<>(new FieldName("IntValue"));
        this.fieldFor_setIntValue = new Field<>(new FieldName("IntValue"));
        this.fieldFor_getStringValue = new Field<>(new FieldName("StringValue"));
        this.fieldFor_setStringValue = new Field<>(new FieldName("StringValue"));
        this.fieldFor_getTestClassValue = new Field<>(new FieldName("TestClassValue"));
        this.fieldFor_setTestClassValue = new Field<>(new FieldName("TestClassValue"));
        this.fieldFor_getListOfInt = new Field<>(new FieldName("ListOfInt"));
        this.fieldFor_setListOfInt = new Field<>(new FieldName("ListOfInt"));
        this.fieldFor_getListOfString = new Field<>(new FieldName("ListOfString"));
        this.fieldFor_setListOfString = new Field<>(new FieldName("ListOfString"));
        this.fieldFor_getListOfTestClasses = new Field<>(new FieldName("ListOfTestClasses"));
        this.fieldFor_setListOfTestClasses = new Field<>(new FieldName("ListOfTestClasses"));
        this.fieldFor_isBoolValue = new Field<>(new FieldName("BoolValue"));
        this.fieldFor_setBoolValue = new Field<>(new FieldName("BoolValue"));
        this.fieldFor_hasValue = new Field<>(new FieldName("Value"));
        this.fieldFor_getWrappedIObject = new Field<>(new FieldName("WrappedIObject"));
        this.fieldFor_setWrappedIObject = new Field<>(new FieldName("WrappedIObject"));
        this.fieldFor_getIObject = new Field<>(new FieldName("IObject"));
        this.fieldFor_setIObject = new Field<>(new FieldName("IObject"));
        this.fieldFor_countCValue = new Field<>(new FieldName("CValue"));
    }
    private IObject message;
    public IObject getMessage() {
        return message;
    }
    private IObject context;
    public IObject getContext() {
        return context;
    }
    private IObject response;
    public IObject getResponse() {
        return response;
    }
    public void init(IObject message, IObject context, IObject response) {
        this.message = message;
        this.context = context;
        this.response = response;
    }
    public java.util.List<java.util.Map<java.lang.String, java.util.List<info.smart_tools.smartactors.core.wrapper_generator.TestClass>>> getIntValue() {
        return null;
    }
    public void setIntValue(int value) {
    }
    public java.lang.String getStringValue() {
        return null;
    }
    public void setStringValue(java.lang.String value) {
    }
    public info.smart_tools.smartactors.core.wrapper_generator.TestClass getTestClassValue() {
        return null;
    }
    public void setTestClassValue(info.smart_tools.smartactors.core.wrapper_generator.TestClass value) {
    }
    public java.util.List<java.lang.Integer> getListOfInt() {
        return null;
    }
    public void setListOfInt(java.util.List<java.lang.Integer> value) {
    }
    public java.util.List<java.lang.String> getListOfString() {
        return null;
    }
    public void setListOfString(java.util.List<java.lang.String> value) {
    }
    public java.util.List<info.smart_tools.smartactors.core.wrapper_generator.TestClass> getListOfTestClasses() {
        return null;
    }
    public void setListOfTestClasses(java.util.List<info.smart_tools.smartactors.core.wrapper_generator.TestClass> value) {
    }
    public java.lang.Boolean isBoolValue() {
        return null;
    }
    public void setBoolValue(boolean value) {
    }
    public java.lang.Boolean hasValue() {
        return null;
    }
    public info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper getWrappedIObject() {
        return null;
    }
    public void setWrappedIObject(info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper value) {
    }
    public info.smart_tools.smartactors.core.iobject.IObject getIObject() {
        return null;
    }
    public void setIObject(info.smart_tools.smartactors.core.iobject.IObject value) {
    }
    public java.lang.Integer countCValue() {
        return null;
    }
}