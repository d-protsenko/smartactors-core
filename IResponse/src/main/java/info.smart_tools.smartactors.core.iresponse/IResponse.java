package info.smart_tools.smartactors.core.iresponse;

import info.smart_tools.smartactors.core.iobject.IObject;

public interface IResponse {

    void setEnvironment(IObject environment);

    void setContext(IObject environment);

    String getBody();

    Object getEnvironment(String key);
}
