package info.smart_tools.smartactors.sandbox.server_with_configuration;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
import info.smart_tools.smartactors.core.feature_manager.FeatureManager;
import info.smart_tools.smartactors.core.filesystem_tracker.FilesystemTracker;
import info.smart_tools.smartactors.core.filesystem_tracker.ListenerTask;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ifeature_manager.IFeature;
import info.smart_tools.smartactors.core.ifeature_manager.IFeatureManager;
import info.smart_tools.smartactors.core.ifilesystem_tracker.IFilesystemTracker;
import info.smart_tools.smartactors.core.ipath.IPath;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin_creator.IPluginCreator;
import info.smart_tools.smartactors.core.iplugin_loader.IPluginLoader;
import info.smart_tools.smartactors.core.iplugin_loader_visitor.IPluginLoaderVisitor;
import info.smart_tools.smartactors.core.iserver.IServer;
import info.smart_tools.smartactors.core.iserver.exception.ServerExecutionException;
import info.smart_tools.smartactors.core.iserver.exception.ServerInitializeException;
import info.smart_tools.smartactors.core.path.Path;
import info.smart_tools.smartactors.core.plugin_creator.PluginCreator;
import info.smart_tools.smartactors.core.plugin_loader_from_jar.ExpansibleURLClassLoader;
import info.smart_tools.smartactors.core.plugin_loader_from_jar.PluginLoader;
import info.smart_tools.smartactors.core.plugin_loader_visitor_empty_implementation.PluginLoaderVisitor;

import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collection;

/**
 *
 */
public class ServerWithConfiguration implements IServer {
    @Override
    public void initialize() throws ServerInitializeException {
        try {
            IBootstrap bootstrap = new Bootstrap();
            IPluginCreator creator = new PluginCreator();
            IPluginLoaderVisitor<String> visitor = new PluginLoaderVisitor<>();
            ExpansibleURLClassLoader urlClassLoader = new ExpansibleURLClassLoader(new URL[]{}, ClassLoader.getSystemClassLoader());
            IPluginLoader<Collection<IPath>> pluginLoader = new PluginLoader(
                    urlClassLoader,
                    (t) -> {
                        try {
                            IPlugin plugin = creator.create(t, bootstrap);
                            plugin.load();
                        } catch (Exception e) {
                            throw new RuntimeException("Could not create instance of IPlugin");
                        }
                    },
                    visitor
            );

            // FS listener creation
            // TODO: Get from configuration
            IPath coreJarsDir = new Path("plugins");
            Files.createDirectories(FileSystems.getDefault().getPath(coreJarsDir.getPath()));

            IFilesystemTracker jarFilesTracker = new FilesystemTracker(
                    (path) -> path.getPath().endsWith(".jar"),
                    ListenerTask::new);

            jarFilesTracker.start(coreJarsDir);
            jarFilesTracker.addErrorHandler((e) -> {
                System.out.println("Server initialization failed!");
                e.printStackTrace();
            });

            // FeatureManager & Feature creation
            IFeatureManager featureManager = new FeatureManager(jarFilesTracker);


            IFeature coreFeature = featureManager.newFeature("smartactors.core");
            coreFeature.whenPresent(files -> {
                try {
                    pluginLoader.loadPlugin(files);
                    bootstrap.start();
                } catch (Throwable e) {
                    throw new RuntimeException("Plugin loading failed.", e);
                }
            });

            String[] coreJars = {
                    "core.bootstrap-0.2.0-SNAPSHOT.jar",
                    "core.bootstrap_item-0.2.0-SNAPSHOT.jar",
                    "core.chain_storage-0.2.0-SNAPSHOT.jar",
                    "core.configuration_manager-0.2.0-SNAPSHOT.jar",
                    "core.create_new_instance_strategy-0.2.0-SNAPSHOT.jar",
                    "core.db_storage-0.2.0-SNAPSHOT.jar",
                    "core.ds_object-0.2.0-SNAPSHOT.jar",
                    "core.feature_manager-0.2.0-SNAPSHOT.jar",
                    "core.field_name-0.2.0-SNAPSHOT.jar",
                    "core.filesystem_tracker-0.2.0-SNAPSHOT.jar",
                    "core.iaction-0.2.0-SNAPSHOT.jar",
                    "core.ibootstrap-0.2.0-SNAPSHOT.jar",
                    "core.ibootstrap_item-0.2.0-SNAPSHOT.jar",
                    "core.ichain_storage-0.2.0-SNAPSHOT.jar",
                    "core.iconfiguration_manager-0.2.0-SNAPSHOT.jar",
                    "core.idatabase_task-0.2.0-SNAPSHOT.jar",
                    "core.ifeature_manager-0.2.0-SNAPSHOT.jar",
                    "core.ifield_name-0.2.0-SNAPSHOT.jar",
                    "core.ifilesystem_tracker-0.2.0-SNAPSHOT.jar",
                    "core.iioccontainer-0.2.0-SNAPSHOT.jar",
                    "core.ikey-0.2.0-SNAPSHOT.jar",
                    "core.immutable_receiver_chain-0.2.0-SNAPSHOT.jar",
                    "core.invalid_argument_exception-0.2.0-SNAPSHOT.jar",
                    "core.invalid_state_exception-0.2.0-SNAPSHOT.jar",
                    "core.iobject-0.2.0-SNAPSHOT.jar",
                    "core.ioc-0.2.0-SNAPSHOT.jar",
                    "core.ioc_container-0.2.0-SNAPSHOT.jar",
                    "core.ipath-0.2.0-SNAPSHOT.jar",
                    "core.iplugin-0.2.0-SNAPSHOT.jar",
                    "core.iplugin_creator-0.2.0-SNAPSHOT.jar",
                    "core.iplugin_loader-0.2.0-SNAPSHOT.jar",
                    "core.iplugin_loader_visitor-0.2.0-SNAPSHOT.jar",
                    "core.ipool-0.2.0-SNAPSHOT.jar",
                    "core.iresolve_dependency_strategy-0.2.0-SNAPSHOT.jar",
                    "core.irouted_object_creator-0.2.0-SNAPSHOT.jar",
                    "core.irouter-0.2.0-SNAPSHOT.jar",
                    "core.iscope-0.2.0-SNAPSHOT.jar",
                    "core.iscope_provider_container-0.2.0-SNAPSHOT.jar",
                    "core.iserver-0.2.0-SNAPSHOT.jar",
                    "core.istrategy_container-0.2.0-SNAPSHOT.jar",
                    "core.itask-0.2.0-SNAPSHOT.jar",
                    "core.map_router-0.2.0-SNAPSHOT.jar",
                    "core.message_processing-0.2.0-SNAPSHOT.jar",
                    "core.named_keys_storage-0.2.0-SNAPSHOT.jar",
                    "core.path-0.2.0-SNAPSHOT.jar",
                    "core.plugin_creator-0.2.0-SNAPSHOT.jar",
                    "core.plugin_loader_from_jar-0.2.0-SNAPSHOT.jar",
                    "core.plugin_loader_visitor_empty_implementation-0.2.0-SNAPSHOT.jar",
                    "core.pool-0.2.0-SNAPSHOT.jar",
                    "core.pool_guard-0.2.0-SNAPSHOT.jar",
                    "core.recursive_scope-0.2.0-SNAPSHOT.jar",
                    "core.resolve_by_name_ioc_strategy-0.2.0-SNAPSHOT.jar",
                    "core.resolve_by_name_ioc_with_lambda_strategy-0.2.0-SNAPSHOT.jar",
                    "core.scope_provider-0.2.0-SNAPSHOT.jar",
                    "core.scope_provider_container-0.2.0-SNAPSHOT.jar",
                    "core.singleton_strategy-0.2.0-SNAPSHOT.jar",
                    "core.sql_commons-0.2.0-SNAPSHOT.jar",
                    "core.strategy_container-0.2.0-SNAPSHOT.jar",
                    "core.string_ioc_key-0.2.0-SNAPSHOT.jar",
                    "hamcrest-core-1.3.jar",
                    "InMemoryJavaCompiler-1.2.jar",
                    "jackson-annotations-2.5.2.jar",
                    "jackson-core-2.5.2.jar",
                    "jackson-databind-2.5.2.jar",
                    "junit-4.12.jar",
                    "plugin.configuration_manager-0.2.0-SNAPSHOT.jar",
                    "plugin.dsobject-0.2.0-SNAPSHOT.jar",
                    "plugin.ifieldname-0.2.0-SNAPSHOT.jar",
                    "plugin.fieldname-0.2.0-SNAPSHOT.jar",
                    "plugin.immutable_receiver_chain-0.2.0-SNAPSHOT.jar",
                    "plugin.ioc_keys-0.2.0-SNAPSHOT.jar",
                    "plugin.load_scope_provider-0.2.0-SNAPSHOT.jar",
                    "plugin.map_router-0.2.0-SNAPSHOT.jar",
                    "plugin.messaging_identifiers-0.2.0-SNAPSHOT.jar",
                    "plugin.read_config_file-0.2.0-SNAPSHOT.jar",
                    "plugin.receiver_chains_storage-0.2.0-SNAPSHOT.jar",
                    "plugin.scoped_ioc-0.2.0-SNAPSHOT.jar",
                    "plugin.scope_provider-0.2.0-SNAPSHOT.jar",
                    "plugin.standard_object_creators-0.2.0-SNAPSHOT.jar",
                    "plugin.http_endpoint-0.2.0-SNAPSHOT.jar"
            };

            for (String jarName : coreJars) {
                coreFeature.requireFile(jarName);
            }

            coreFeature.listen();

            while (!Thread.interrupted()) {
                Thread.sleep(1L);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new ServerInitializeException(e);
        }
    }

    @Override
    public void start() throws ServerExecutionException {

    }
}
