package info.smart_tools.smartactors.feature_loader.interfaces.ifilesystem_facade;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.base.interfaces.ipath.IPathFilter;

import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.Collection;

/**
 * Facade for filesystem.
 */
public interface IFilesystemFacade {
    /**
     * List subdirectories of given directory.
     *
     * @param directory    path to directory to list subdirectories in
     * @return collection of paths to subdirectories
     * @throws IOException if I/O error occurs
     * @throws NotDirectoryException if given file is not a directory
     */
    Collection<IPath> listSubdirectories(final IPath directory) throws IOException;

    /**
     * List all files acceptable by a filter.
     *
     * @param directory    path to directory to list files in
     * @param filter       path filter
     * @return collection of paths to files
     * @throws IOException if I/O error occurs
     * @throws NotDirectoryException if given file is not a directory
     */
    Collection<IPath> listFiles(final IPath directory, final IPathFilter filter) throws IOException;

    /**
     * Join paths.
     *
     * @param first    first path
     * @param rest     other paths
     * @return result of concatenation of given paths
     * @throws InvalidArgumentException if any of given paths is null
     */
    IPath joinPaths(final IPath first, final IPath... rest) throws InvalidArgumentException;

    /**
     * Read content of file to a string
     *
     * @param path    path to ile to read
     * @return contents of file represented as string
     * @throws IOException if any I/O error occurs
     */
    String readToString(final IPath path) throws IOException;
}
