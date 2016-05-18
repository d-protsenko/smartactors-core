package info.smart_tools.smartactors.core.filesystem_tracker;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

class ListenerTask implements Runnable {
    private WatchService watchService;
    private IAction<File> newFileAction;
    private File directory;

    public ListenerTask(File directory, IAction<File> newFileAction)
            throws IOException {
        this.directory = directory;
        this.newFileAction = newFileAction;

        Path directoryPath = directory.toPath();

        watchService = directoryPath.getFileSystem().newWatchService();
        directoryPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
    }

    @Override
    public void run() {
        try (WatchService watcher = watchService) {
            scanExistFiles();

            for (;;) {
                WatchKey key = watcher.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        File file = ((Path) event.context()).toFile();
                        newFileAction.execute(file);
                    }
                }

                if (!key.reset())
                    break;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | ActionExecuteException e) {
            throw new RuntimeException(e);
        }
    }

    private void scanExistFiles()
            throws ActionExecuteException {
        for (File file : directory.listFiles())
            newFileAction.execute(file);
    }
}
