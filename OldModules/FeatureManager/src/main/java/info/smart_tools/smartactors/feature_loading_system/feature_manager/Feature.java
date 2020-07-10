package info.smart_tools.smartactors.feature_loading_system.feature_manager;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ifeature_manager.IFeature;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ifeature_manager.IFeatureManager;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ifeature_manager.exception.FeatureManagementException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ifilesystem_tracker.IFilesystemTracker;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.concurrent.BlockingQueue;

/**
 * Implementation of {@link IFeature}.
 */
class Feature implements IFeature {
    private Set<String> expectedFileNames;
    private List<IPath> presentFiles;
    private List<IAction<Collection<IPath>>> actions;
    private boolean isListening;
    private boolean isPresent;
    private final Object lock;
    private String name;
    private IAction<IPath> fileHandleAction;
    private IFilesystemTracker filesystemTracker;
    private FileSystem fileSystem = FileSystems.getDefault();
    private BlockingQueue<ExecutionPair> queue;


    /**
     * Action to execute when filesystem tracker notifies feature about new files.
     */
    private class FileHandlerAction implements IAction<IPath> {
        @Override
        public void execute(final IPath file)
                throws ActionExecutionException {
            synchronized (Feature.this.lock) {
                String fileName = fileSystem.getPath(file.getPath()).getFileName().toString();

                if (expectedFileNames.contains(fileName)) {
                    presentFiles.add(file);
                }

                if (presentFiles.size() != expectedFileNames.size()) {
                    return;
                }

                Feature.this.isPresent = true;
                filesystemTracker.removeFileHandler(FileHandlerAction.this);
            }

            List<ActionExecutionException> exceptions = new LinkedList<>();

            for (IAction<Collection<IPath>> action : actions) {
                try {
                    queue.put(new ExecutionPair(action, presentFiles));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (0 != exceptions.size()) {
                ActionExecutionException exception = new ActionExecutionException(
                        "Error(s) occurred while executing actions specified for feature",
                        exceptions.remove(0));

                for (ActionExecutionException e : exceptions) {
                    exception.addSuppressed(e);
                }

                throw exception;
            }
        }
    }

    /**
     * The constructor.
     *
     * @param name                  name of the feature
     * @param filesystemTracker     {@link IFilesystemTracker} instance to use
     * @param queue the queue of parent {@link IFeatureManager}
     * @throws InvalidArgumentException if {@code name} is {@code null}
     * @throws InvalidArgumentException if {@code filesystemTracker} is {@code null}
     */
    Feature(final String name, final IFilesystemTracker filesystemTracker, final BlockingQueue<ExecutionPair> queue)
            throws InvalidArgumentException {
        if (null == name) {
            throw new InvalidArgumentException("Feature name should not be null.");
        }

        if (null == filesystemTracker) {
            throw new InvalidArgumentException("File system tracker should not be null.");
        }

        if (null == queue) {
            throw new InvalidArgumentException("Queue should not be null.");
        }

        this.queue = queue;
        this.expectedFileNames = new HashSet<>();
        this.presentFiles = new LinkedList<>();
        this.actions = new LinkedList<>();
        this.isListening = false;
        this.isPresent = false;
        this.lock = new Object();
        this.name = name;
        this.fileHandleAction = new FileHandlerAction();
        this.filesystemTracker = filesystemTracker;
    }

    @Override
    public void requireFile(final String fileName)
            throws FeatureManagementException {
        synchronized (this.lock) {
            if (isListening || isPresent) {
                throw new FeatureManagementException("requireFile() called after listening started.");
            }

            expectedFileNames.add(fileName);
        }
    }

    @Override
    public void whenPresent(final IAction<Collection<IPath>> action)
            throws FeatureManagementException {
        synchronized (this.lock) {
            if (isPresent) {
                try {
                    queue.put(new ExecutionPair(action, presentFiles));
                } catch (InterruptedException e) {
                    throw new FeatureManagementException("Was interrupted when wait.", e);
                }
            } else {
                actions.add(action);
            }
        }
    }

    @Override
    public void listen()
            throws FeatureManagementException {
        if (0 == expectedFileNames.size()) {
            throw new FeatureManagementException("No files defined for this feature.");
        }

        synchronized (this.lock) {
            if (this.isListening) {
                throw new FeatureManagementException("listen() called twice.");
            }

            this.filesystemTracker.addFileHandler(this.fileHandleAction);

            this.isListening = true;
        }
    }

    @Override
    public String getName() {
        return this.name;
    }
}


