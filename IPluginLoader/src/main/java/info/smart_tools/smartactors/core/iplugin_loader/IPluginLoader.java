package info.smart_tools.smartactors.core.iplugin_loader;

/**
 * Interface IPluginLoader
 */
public interface IPluginLoader {

    /**
     * Load plugin to the system
     * @param plugin plugin package (for example, jar file)
     * @param <T> type of plugin package
     */
    <T> void loadPlugin(T plugin);
}
