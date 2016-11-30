package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.exception.FeatureManagementException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by sevenbits on 11/15/16.
 */
public class UnzipFeatureTask implements ITask {

    private IFeatureManager featureManager;
    private IFeature<String, IFeatureState<String>> feature;

    static final byte[] BUFFER = new byte[2048];

    private final IFieldName dependenciesFieldName;

    public UnzipFeatureTask(final IFeatureManager manager, final IFeature feature)
            throws ResolutionException {

        this.featureManager = manager;
        this.feature = feature;
        this.dependenciesFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "afterFeatures");
    }

    public void execute()
            throws TaskExecutionException {
        try {
            if (null == this.feature.getDependencies() && this.feature.getFeatureLocation().toString().endsWith(".zip")) {
                System.out.println("Start unzipping feature - '" + feature.getName() + "'.");
                File f = new File(feature.getFeatureLocation().toString());
                unzip(f);
                System.out.println("OK -------------- Feature '" + this.feature.getName() + "' has been unzipped successful.");
            }

            ((IFeatureState) this.feature.getStatus()).next();
            ((IFeatureState) this.feature.getStatus()).setLastSuccess(true);
            ((IFeatureState) this.feature.getStatus()).setExecuting(false);
        } catch (Throwable e) {
            ((IFeatureState) this.feature.getStatus()).setLastSuccess(false);
            System.out.println("FAILED ---------- Feature '" + this.feature.getName() + "' unzipping has been aborted with exception:");
            System.err.println(e);
        }
        try {
            this.featureManager.onCompleteFeatureOperation(this.feature);
        } catch (FeatureManagementException e) {
            System.err.println(e);
        }
    }

    private void unzip(final File f)
            throws Exception {
        String destination = f.getPath().substring(0, f.getPath().lastIndexOf('/'));
        FileInputStream fInput = null;
        ZipInputStream zipInput = null;
        ZipEntry entry;
        try {
            fInput = new FileInputStream(f);
            zipInput = new ZipInputStream(fInput);
            entry = zipInput.getNextEntry();
            while (entry != null) {
                String entryName = entry.getName();
                File file = new File(destination + File.separator + entryName);
                if (entry.isDirectory()) {
                    File newDir = new File(file.getAbsolutePath());
                    if (!newDir.exists()) {
                        boolean success = newDir.mkdirs();
                        if (!success) {
                            System.out.println("Problem with creating Folder");
                        }
                    }
                } else {
                    FileOutputStream fOutput = new FileOutputStream(file);
                    int count = 0;
                    while ((count = zipInput.read(BUFFER)) > 0) {
                        fOutput.write(BUFFER, 0, count);
                    }
                    if (file.getName().equals("config.json")) {
                        updateFeature(file);
                    }
                    fOutput.close();
                }
                zipInput.closeEntry();
                entry = zipInput.getNextEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != zipInput) {
                    zipInput.closeEntry();
                    zipInput.close();
                }
                if (null != fInput) {
                    fInput.close();
                }
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        }
    }

    private void updateFeature(final File f) throws IOException {
        try {
            String content = new Scanner(f).useDelimiter("\\Z").next();
            IObject config = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), content);
            List<String> dependencies = (List<String>) config.getValue(this.dependenciesFieldName);
            Set<String> dependenciesSet = new HashSet<>(dependencies);
            this.feature.setDependencies(dependenciesSet);
            this.feature.setFeatureLocation(new Path(f.getParent()));
        } catch (ResolutionException | ReadValueException | InvalidArgumentException e) {

        }
    }
}
