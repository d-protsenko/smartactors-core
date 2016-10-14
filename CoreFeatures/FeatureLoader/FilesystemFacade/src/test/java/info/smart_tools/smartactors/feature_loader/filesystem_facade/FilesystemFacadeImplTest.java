package info.smart_tools.smartactors.feature_loader.filesystem_facade;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.feature_loader.interfaces.ifilesystem_facade.IFilesystemFacade;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Test for {@link FilesystemFacadeImpl}.
 */
public class FilesystemFacadeImplTest {
    private final IFilesystemFacade filesystemFacade = new FilesystemFacadeImpl();

    @Test(expected = NotDirectoryException.class)
    public void Should_listSubdirectories_throwWhenFileIsNotADirectory()
            throws Exception {
        IPath path = new Path(Files.createTempFile("not-a-directory-", ""));
        filesystemFacade.listSubdirectories(path);
    }

    @Test
    public void Should_listSubdirectories_listSubdirectories()
            throws Exception {
        java.nio.file.Path dirPath = Files.createTempDirectory("directory-with-files-");
        Files.createDirectory(dirPath.resolve("a"));
        Files.createFile(dirPath.resolve("b"));
        Collection<IPath> paths = filesystemFacade.listSubdirectories(new Path(dirPath));

        assertNotNull(paths);
        assertEquals(1, paths.size());
        assertEquals(dirPath.resolve("a").toString(), paths.iterator().next().getPath());
    }

    @Test(expected = NotDirectoryException.class)
    public void Should_listFiles_throwWhenFileIsNotADirectory()
            throws Exception {
        IPath path = new Path(Files.createTempFile("not-a-directory-", ""));
        filesystemFacade.listFiles(path, p -> true);
    }

    @Test
    public void Should_listFiles_listFilesMatchingGivenFilter()
            throws Exception {
        java.nio.file.Path dirPath = Files.createTempDirectory("directory-with-files-");
        Files.createDirectory(dirPath.resolve("1a"));
        Files.createFile(dirPath.resolve("2a"));
        Files.createFile(dirPath.resolve("b"));
        Collection<IPath> paths = filesystemFacade.listFiles(
                new Path(dirPath),
                path -> path.getPath().endsWith("a"));

        assertNotNull(paths);
        assertEquals(1, paths.size());
        assertEquals(dirPath.resolve("2a").toString(), paths.iterator().next().getPath());
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_joinPaths_throwWhenAnyOfPathsIsNull()
            throws Exception {
        assertNotNull(filesystemFacade.joinPaths(new Path("/"), new Path("."), null));
    }

    @Test
    public void Should_joinPaths_joinPaths()
            throws Exception {
        IPath joined = filesystemFacade.joinPaths(new Path("foo"), new Path("bar"), new Path("baz"));
        assertNotNull(joined);
        assertEquals(String.format("foo%sbar%sbaz", File.separator, File.separator), joined.getPath());
    }

    @Test
    public void Should_readToString_readAFileToString()
            throws Exception {
        java.nio.file.Path tfp = Files.createTempFile("file-with-string-", "");
        Files.write(tfp, "The quick brown actor".getBytes());

        String read = filesystemFacade.readToString(new Path(tfp));

        assertEquals("The quick brown actor", read);
    }
}
