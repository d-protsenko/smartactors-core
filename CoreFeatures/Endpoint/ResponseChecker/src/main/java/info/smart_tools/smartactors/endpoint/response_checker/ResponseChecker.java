package info.smart_tools.smartactors.endpoint.response_checker;

import info.smart_tools.smartactors.endpoint.interfaces.iresponse_checker.IResponseChecker;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse_handler.IResponseHandler;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;

/**
 * Implementation of {@link IResponseChecker}
 */
public class ResponseChecker implements IResponseChecker {
    IResponseHandler responseHandler;
    ITask taskOnPass, taskOnFail;

    /**
     * Constructor
     *
     * @param responseHandler responseHandler, that checker should check
     */
    ResponseChecker(final IResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    @Override
    public void check() {
        try {
            IQueue<ITask> mainQueue = IOC.resolve(Keys.getOrAdd("task_queue"));
            if (responseHandler.isReceived()) {
                if (taskOnPass != null) {
                    mainQueue.put(taskOnPass);
                }
            } else {
                if (taskOnFail != null) {
                    mainQueue.put(taskOnFail);
                }
            }
        } catch (ResolutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setTaskOnFail(ITask task) {
        this.taskOnFail = task;
    }

    @Override
    public void setTaskOnPass(ITask task) {
        this.taskOnPass = task;
    }
}
