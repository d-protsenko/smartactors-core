package info.smart_tools.smartactors.endpoint.interfaces.iresponse_checker;

import info.smart_tools.smartactors.task.interfaces.itask.ITask;

/**
 * Checker if response already came
 */
public interface IResponseChecker {

    void check();

    void setTaskOnFail(final ITask task);

    void setTaskOnPass(final ITask task);


}
