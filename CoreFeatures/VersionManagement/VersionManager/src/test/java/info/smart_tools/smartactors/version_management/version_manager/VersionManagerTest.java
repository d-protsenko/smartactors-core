package info.smart_tools.smartactors.version_management.version_manager;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.version_management.version_manager.exception.VersionManagerException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for SmartactorsClassLoader
 */
public class VersionManagerTest {

    @Test
    public void checkModuleCreation()
            throws Exception {
        VersionManager.setCurrentModule(VersionManager.getModuleById(VersionManager.coreId));
        assertNotNull(VersionManager.getCurrentModule());
        VersionManager.addModule("cl1", "cl1", null);
        assertNotNull(VersionManager.getModuleById("cl1").getClassLoader());
        VersionManager.addModule("cl2", "cl2", null);
        assertNotNull(VersionManager.getModuleById("cl2").getClassLoader());
        assertSame(((ClassLoader) VersionManager.getModuleById("cl2").getClassLoader()).getParent(), ClassLoader.getSystemClassLoader());
        VersionManager.addModuleDependency("cl1", "cl2");
        VersionManager.finalizeModuleDependencies("cl1");
        VersionManager.finalizeModuleDependencies("cl2");

        try {
            VersionManager.addModule(VersionManager.coreId, null, null);
            fail();
        } catch(InvalidArgumentException e) { }

        try {
            VersionManager.addModule(VersionManager.coreId, "core", "version");
            fail();
        } catch(VersionManagerException e) { }

        IObject iobj = new DSObject();
        VersionManager.setCurrentMessage(iobj);
        assertSame(VersionManager.getCurrentMessage(), iobj);

    }

    @Test
    public void checkAdditionNewURL()
            throws Exception {
    }

    @Test
    public void checkGettersSetters()
            throws Exception {
    }
}