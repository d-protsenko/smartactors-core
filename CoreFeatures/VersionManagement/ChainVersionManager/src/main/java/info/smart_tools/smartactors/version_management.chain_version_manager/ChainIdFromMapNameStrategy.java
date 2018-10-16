package info.smart_tools.smartactors.version_management.chain_version_manager;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.class_management.module_manager.ModuleManager;
import info.smart_tools.smartactors.iobject.iobject.IObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChainIdFromMapNameStrategy implements IResolveDependencyStrategy {

    Map<String, Map<String, String>> chainIds = new ConcurrentHashMap<>();
    private static Map<String, ChainVersionStrategies> chainVersionStrategies = new ConcurrentHashMap<>();

    public void registerVersionResolutionStrategy(String mapName, IResolveDependencyStrategy strategy)
            throws ResolveDependencyStrategyException {
        String version = ModuleManager.getCurrentModule().getVersion();
        if (mapName == null || version == null) {
            throw new ResolveDependencyStrategyException("Map name and version cannot be null.");
        }
        ChainVersionStrategies versionStrategies = chainVersionStrategies.get(mapName);
        if (versionStrategies == null) {
            versionStrategies = new ChainVersionStrategies(mapName);
            chainVersionStrategies.put(mapName, versionStrategies);
        }
        versionStrategies.registerVersionResolutionStrategy(version, strategy);
    }

    private Map<String, String> registerMap(String mapName, String version)
            throws ResolveDependencyStrategyException {

        if (mapName == null || version == null) {
            throw new ResolveDependencyStrategyException("Map name or version cannot be null.");
        }

        Map<String, String> versions = chainIds.get(mapName);
        if (versions == null) {
            versions = new ConcurrentHashMap<>();
            chainIds.put(mapName, versions);
        }

        String chainId = mapName + ":" + version;
        String previous = versions.put(version, chainId);
        if (previous != null) {
            System.out.println(
                    "[WARNING] Replacing chain '" + chainId + "' by chain from feature " +
                    ModuleManager.getCurrentModule().getName() + ":" + version
            );
        } else {
            registerVersionResolutionStrategy(mapName, null);
        }
        return versions;
    }

    @Override
    public <T> T resolve(Object... args) throws ResolveDependencyStrategyException {

        String mapName = (String)args[0];
        if (mapName == null) {
            throw new ResolveDependencyStrategyException("Map name cannot be null.");
        }

        IObject message = null;
        String version = null;
        if (args.length > 2) {
            version = (String)args[2];
        }
        if (args.length > 1) {
            message = (IObject)args[1];
        }

        if (version == null) {
            if (message == null) {
                version = ModuleManager.getCurrentModule().getVersion();
            } else {
                version = chainVersionStrategies.get(mapName).resolveVersion(message);
            }
        }

        if (version == null) {
            throw new ResolveDependencyStrategyException("Resolution failed for map name '"+mapName+"'.");
        }

        Map<String, String> versions = chainIds.get(mapName);
        if (versions == null) {
            versions = registerMap(mapName, version);
        }

        String chainId = versions.get(version);
        if (chainId == null) {
            registerMap(mapName, version);
        }

        return (T) chainId;
    }
}