package info.smart_tools.smartactors.das.utilities.interfaces;


import info.smart_tools.smartactors.das.models.Feature;
import info.smart_tools.smartactors.das.models.Project;
import info.smart_tools.smartactors.das.utilities.exception.ProjectCreationException;
import info.smart_tools.smartactors.das.utilities.exception.ProjectResolutionException;

import java.nio.file.Path;

public interface IProjectResolver {

    Project resolveProject()
            throws ProjectResolutionException;

    Project createProject(final String name, final String groupId, final String version, final Path path)
            throws ProjectCreationException;

    Feature getCurrentFeature();
}
