package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature;

/**
 * Created by sevenbits on 11/15/16.
 */
public interface IFeatureState {

    String getCurrent();

    boolean isLastSuccess();

    void setLastSuccess(boolean lastSuccess);

    boolean isExecuting();

    void setExecuting(boolean executing);

    void next();

    boolean completed();

    boolean isDeferred();

    void setDeferred(boolean deferred);
}
