package info.smart_tools.smartactors.feature_loader.filesystem_facade;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.base.interfaces.ipath.IPathFilter;
import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.feature_loader.interfaces.ifilesystem_facade.IFilesystemFacade;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

/**
 * Implementation of filesystem facade.
 */
public class FilesystemFacadeImpl implements IFilesystemFacade {
    @Override
    public Collection<IPath> listSubdirectories(final IPath directory) throws IOException {
        File dir = new File(directory.getPath());

        if (!dir.isDirectory()) {
            throw new NotDirectoryException(directory.getPath());
        }

        File[] files = dir.listFiles(File::isDirectory);
        IPath[] paths = new IPath[files.length];

        for (int i = 0; i < files.length; i++) {
            paths[i] = new Path(files[i]);
        }

        return Arrays.asList(paths);
    }

    @Override
    public Collection<IPath> listFiles(final IPath directory, final IPathFilter filter) throws IOException {
        File dir = new File(directory.getPath());

        if (!dir.isDirectory()) {
            throw new NotDirectoryException(directory.getPath());
        }

        File[] files = dir.listFiles(file -> file.isFile() && filter.accept(new Path(file)));
        IPath[] paths = new IPath[files.length];

        for (int i = 0; i < files.length; i++) {
            paths[i] = new Path(files[i]);
        }

        return Arrays.asList(paths);
    }

    @Override
    public IPath joinPaths(final IPath first, final IPath... rest) throws InvalidArgumentException {
        if (first == null || Arrays.asList(rest).indexOf(null) >= 0) {
            throw new InvalidArgumentException("One of arguments is null.");
        }

        java.nio.file.Path path = Paths.get(first.getPath());

        for (IPath p : rest) {
            path = path.resolve(p.getPath());
        }

        return new Path(path.toString());
    }

    @Override
    public String readToString(final IPath path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path.getPath())));
    }
}
