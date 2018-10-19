package info.smart_tools.smartactors.feature_loading_system.feature_manager;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ifeature_manager.IFeature;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ifeature_manager.IFeatureManager;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ifeature_manager.exception.FeatureManagementException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ifilesystem_tracker.IFilesystemTracker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implementation of {@link IFeatureManager}.
 */
public class FeatureManager implements IFeatureManager {
    private final BlockingQueue<ExecutionPair> queue = new LinkedBlockingQueue<>(10);

    /**
     * The constructor.
     *
     * @throws InvalidArgumentException if {@code filesystemTracker} is {@code null}
     */
    public FeatureManager()
            throws InvalidArgumentException {

        Runnable queueWorker = new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        queue.take().execute();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    throw new RuntimeException("FeatureManager queue processing has been failed.", e);
                }
            }
        };
        new Thread(queueWorker, "FeatureManagerThread").start();
    }

    @Override
    public IFeature newFeature(final String name, final IFilesystemTracker tracker)
            throws FeatureManagementException, InvalidArgumentException {
        if (null == tracker) {
            throw new InvalidArgumentException("Tracke should not be null.");
        }
        try {
            return new Feature(name, tracker, this.queue);
        } catch (InvalidArgumentException e) {
            throw new FeatureManagementException("Error creating new feature.", e);
        }
    }
}
