package info.smart_tools.smartactors.feature_management.unzip_feature_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.feature_management.unzip_feature_actor.exception.UnzipFeatureException;
import info.smart_tools.smartactors.feature_management.unzip_feature_actor.wrapper.UnzipFeatureWrapper;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
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
 * Created by sevenbits on 12/6/16.
 */
public class UnzipFeatureActor {

    private final IFieldName dependenciesFieldName;

    public UnzipFeatureActor()
            throws ResolutionException {
        this.dependenciesFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "afterFeatures");
    }

    public void unzip(final UnzipFeatureWrapper wrapper)
            throws UnzipFeatureException {
        IFeature feature;
        try {
            feature = wrapper.getFeature();
        } catch (ReadValueException e) {
            throw new UnzipFeatureException("Feature should not be null.");
        }
        try {
            if (null == feature.getDependencies() && feature.getFeatureLocation().toString().endsWith(".zip")) {
                System.out.println("[INFO] Start unzipping feature - '" + feature.getName() + "'.");
                File f = new File(feature.getFeatureLocation().toString());
                File configFile = unzip0(f);
                updateFeature(configFile, feature);
                if (null != feature.getDependencies()) {
                    IMessageProcessor mp = wrapper.getMessageProcessor();
                    mp.pauseProcess();
                    wrapper.getDeferredFeatures().add(feature);
                    wrapper.getFeatureProcess().put(mp, feature);
                }
                System.out.println("[OK] -------------- Feature '" + feature.getName() + "' has been unzipped successful.");
            }
        } catch (Throwable e) {
            feature.setFailed(true);
            System.out.println("[FAILED] ---------- Feature '" + feature.getName() + "' unzipping has been aborted with exception:");
            System.out.println(e);
        }
    }

    private File unzip0(final File f) throws Exception {
        String destination = f.getPath().substring(0, f.getPath().lastIndexOf('/'));
        try {
            ZipFile zipFile = new ZipFile(f);
            zipFile.extractAll(destination);
            List<FileHeader> headers = zipFile.getFileHeaders();
            FileHeader configFileHeader = headers.stream().filter(fh -> fh.getFileName().endsWith("config.json")).findFirst().get();
            File configFile = new File(destination + File.separator + configFileHeader.getFileName());

            return configFile;
        } catch (ZipException e) {
            throw new Exception(e);
        }
    }

    private void updateFeature(final File f, final IFeature feature)
            throws IOException {
        try {
            String content = new Scanner(f).useDelimiter("\\Z").next();
            IObject config = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), content);
            List<String> dependencies = (List<String>) config.getValue(this.dependenciesFieldName);
            Set<String> dependenciesSet = new HashSet<>(dependencies);
            feature.setDependencies(dependenciesSet);
            feature.setFeatureLocation(new Path(f.getParent()));
        } catch (ResolutionException | ReadValueException | InvalidArgumentException e) {

        }
    }
}
