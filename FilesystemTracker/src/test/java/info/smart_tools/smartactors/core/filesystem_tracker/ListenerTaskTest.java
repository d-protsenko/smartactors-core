package info.smart_tools.smartactors.core.filesystem_tracker;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ListenerTaskTest {
    private IAction<File> actionMock;
    private File directoryMock;
    private WatchService watchServiceMock;
    private WatchKey watchKeyMock;
    private WatchEvent event1Mock, event2Mock;

    @Before
    public void setUp()
            throws Exception {
        actionMock = (IAction<File>)mock(IAction.class);
        directoryMock = spy(new File("dir"));
        watchServiceMock = mock(WatchService.class);
        watchKeyMock = mock(WatchKey.class);
        event1Mock = mock(WatchEvent.class);
        event2Mock = mock(WatchEvent.class);

//        when(directoryMock.getName()).thenReturn("dir");
//        when(directoryMock.getPath()).thenReturn("dir");
        when(directoryMock.toPath()).thenReturn(mock(Path.class));
        when(directoryMock.toPath().getFileSystem()).thenReturn(mock(FileSystem.class));
        when(directoryMock.toPath().getFileSystem().newWatchService()).thenReturn(watchServiceMock);
    }

    @Test
    public void Should_setupWatchServiceOnCreation()
            throws Exception {
        ListenerTask task = new ListenerTask(directoryMock, actionMock);

        verify(directoryMock.toPath()).register(same(watchServiceMock), same(StandardWatchEventKinds.ENTRY_CREATE));
    }

    @Test
    public void Should_queryListOfExistFilesAndPollWatchServiceEvents()
            throws Exception {
        File[] filesMock = new File[] {mock(File.class), mock(File.class)};
        File newFileMock = spy(new File(directoryMock, "new"));
        Path newFilePathMock = Paths.get("new");

        when(directoryMock.listFiles()).thenReturn(filesMock);

        when(event1Mock.kind()).thenReturn((WatchEvent.Kind)StandardWatchEventKinds.ENTRY_CREATE);
        when(event1Mock.context()).thenReturn(newFilePathMock);
        when(event2Mock.kind()).thenReturn((WatchEvent.Kind)StandardWatchEventKinds.ENTRY_DELETE);
        when(watchKeyMock.pollEvents())
                .thenReturn(Arrays.asList(event1Mock, event2Mock))
                .thenReturn(Collections.emptyList());

        when(watchServiceMock.take()).thenReturn(watchKeyMock);
        when(watchKeyMock.reset()).thenReturn(true).thenReturn(false);

        ListenerTask task = new ListenerTask(directoryMock, actionMock);

        task.run();

        verify(actionMock, times(1)).execute(same(filesMock[0]));
        verify(actionMock, times(1)).execute(same(filesMock[1]));
        verify(actionMock, times(1)).execute(eq(newFileMock));
        verify(actionMock, times(3)).execute(any());
    }

    @Test
    public void Should_handleThreadInterruption()
            throws Exception {
        when(directoryMock.listFiles()).thenReturn(new File[0]);
        when(watchServiceMock.take()).thenThrow(new InterruptedException());

        ListenerTask task = new ListenerTask(directoryMock, actionMock);

        task.run();

        assertTrue(Thread.interrupted());
    }

    @Test
    public void Should_wrapActionExceptionsIntoRuntimeExceptions()
            throws Exception {
        File[] filesMock = new File[] {mock(File.class)};
        when(directoryMock.listFiles()).thenReturn(filesMock);

        ListenerTask task = new ListenerTask(directoryMock, actionMock);

        doThrow(new ActionExecuteException("")).when(actionMock).execute(same(filesMock[0]));

        try {
            task.run();
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof ActionExecuteException);
        }
    }

    @Test
    public void Should_existFilesAndPolledFilesBeInTheSameDir()
            throws Exception {
        File newFileMock = new File(directoryMock.getName(), "file");
        Path newFilePathMock = Paths.get("file");

        when(directoryMock.listFiles()).thenReturn(new File[] {});

        when(event1Mock.kind()).thenReturn((WatchEvent.Kind)StandardWatchEventKinds.ENTRY_CREATE);
        when(event1Mock.context()).thenReturn(newFilePathMock);
        when(watchKeyMock.pollEvents())
                .thenReturn(Arrays.asList(event1Mock))
                .thenReturn(Collections.emptyList());

        when(watchServiceMock.take()).thenReturn(watchKeyMock);
        when(watchKeyMock.reset()).thenReturn(true).thenReturn(false);

        ListenerTask task = new ListenerTask(directoryMock, actionMock);
        task.run();

        verify(actionMock, times(1)).execute(eq(newFileMock));
    }

    @Test
    public void Should_addDirectoryToTrackedFiles() throws IOException {
        ListenerTask task = new ListenerTask(directoryMock, actionMock);
        assertEquals(new File("dir/file"), task.addFileToDirectory(new File("file")));
        assertEquals(new File("dir/subdir/file"), task.addFileToDirectory(new File("subdir/file")));
    }

}
