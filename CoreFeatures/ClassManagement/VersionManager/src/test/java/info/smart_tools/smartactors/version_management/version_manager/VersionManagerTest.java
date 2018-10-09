package info.smart_tools.smartactors.version_management.version_manager;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for SmartactorsClassLoader
 */
public class VersionManagerTest {

    @Test
    public void checkModuleCreation()
            throws Exception {
        VersionManager.addModule(VersionManager.coreId, VersionManager.coreName, VersionManager.coreVersion);
        VersionManager.addModule(VersionManager.coreId, VersionManager.coreName, VersionManager.coreVersion);
        VersionManager.setCurrentModule(VersionManager.coreId);
        assertSame(VersionManager.getCurrentModule(), VersionManager.coreId);
        VersionManager.addModule("cl1", "cl1", null);
        assertNotNull(VersionManager.getModuleClassLoader("cl1"));
        VersionManager.addModule("cl2", "cl2", null);
        assertNotNull(VersionManager.getModuleClassLoader("cl2"));
        assertSame(((ClassLoader) VersionManager.getModuleClassLoader("cl2")).getParent(), ClassLoader.getSystemClassLoader());
        VersionManager.addModuleDependency("cl1", "cl2");
        VersionManager.finalizeModuleDependencies("cl1");
        VersionManager.finalizeModuleDependencies("cl2");

        try {
            VersionManager.addModule(VersionManager.coreId, null, VersionManager.coreVersion);
            fail();
        } catch(InvalidArgumentException e) { }

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