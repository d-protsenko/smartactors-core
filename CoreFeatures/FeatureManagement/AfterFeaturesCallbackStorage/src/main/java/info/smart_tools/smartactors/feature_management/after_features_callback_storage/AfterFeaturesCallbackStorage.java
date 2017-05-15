package info.smart_tools.smartactors.feature_management.after_features_callback_storage;

import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;

/**
 * Thread storage of IQueue
 */
public class AfterFeaturesCallbackStorage {

    private static final ThreadLocal<IQueue> localCallbackQueue = new ThreadLocal<>();

    public final static IQueue getLocaleCallbackQueue() {
        return localCallbackQueue.get();
    }

    public final static void setLocalCallbackQueue(final IQueue queue) {
        localCallbackQueue.set(queue);
    }
}
