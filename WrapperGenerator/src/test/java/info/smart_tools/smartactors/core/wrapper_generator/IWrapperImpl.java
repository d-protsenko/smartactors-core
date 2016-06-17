package info.smart_tools.smartactors.core.wrapper_generator;
import info.smart_tools.smartactors.core.wrapper_generator.IWrapper;
import java.lang.Integer;
import info.smart_tools.smartactors.core.wrapper_generator.TestClass;
import java.lang.Boolean;
import java.util.List;
import java.lang.String;
import info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper;
import info.smart_tools.smartactors.core.iobject.IObject;
public class IWrapperImpl implements IObjectWrapper {
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
}