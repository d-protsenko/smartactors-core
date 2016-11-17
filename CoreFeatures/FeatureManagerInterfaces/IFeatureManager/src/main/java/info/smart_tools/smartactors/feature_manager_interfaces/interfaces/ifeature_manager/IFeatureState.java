package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager;

/**
 * Created by sevenbits on 11/15/16.
 */
public interface IFeatureState<S> {

    S getCurrent();

    boolean getLastSuccess();

    void setLastSuccess(boolean lastSuccess);

    boolean isExecuting();

    void setExecuting(boolean executing);

    void next()
            throws Exception;

    boolean completed();
}
