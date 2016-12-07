package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature.IFeatureState;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.exception.FeatureManagementException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by sevenbits on 11/15/16.
 */
public class UnzipFeatureTask implements ITask {

    private IFeatureManager featureManager;
    private IFeature feature;

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
                System.out.println("[INFO] Start unzipping feature - '" + feature.getName() + "'.");
                File f = new File(feature.getFeatureLocation().toString());
                unzip(f);
                System.out.println("[OK] -------------- Feature '" + this.feature.getName() + "' has been unzipped successful.");
            }
            ((IFeatureState) this.feature.getStatus()).setLastSuccess(true);
        } catch (Throwable e) {
            ((IFeatureState) this.feature.getStatus()).setLastSuccess(false);
            System.out.println("[FAILED] ---------- Feature '" + this.feature.getName() + "' unzipping has been aborted with exception:");
            System.out.println(e);
        }
        try {
            this.featureManager.onCompleteFeatureOperation(this.feature);
        } catch (FeatureManagementException e) {
            System.out.println(e);
        }
    }

    private void unzip(final File f)
            throws Exception {
        String destination = f.getPath().substring(0, f.getPath().lastIndexOf('/'));
        try {
            ZipFile zipFile = new ZipFile(f);
            zipFile.extractAll(destination);
            List<FileHeader> headers = zipFile.getFileHeaders();
            FileHeader configFileHeader = headers.stream().filter(fh -> fh.getFileName().endsWith("config.json")).findFirst().get();
            File configFile = new File(destination + File.separator + configFileHeader.getFileName());
            updateFeature(configFile);

        } catch (ZipException e) {
            e.printStackTrace();
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
