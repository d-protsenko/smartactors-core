package info.smart_tools.smartactors.version_management.version_manager;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.version_management.interfaces.imodule.IModule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class VersionedChainIdFromMapNameStrategy implements IResolveDependencyStrategy {
    Map<String, Map<String, String>> chainIds = new ConcurrentHashMap<>();
    private static Map<String, ChainVersionStrategies> chainVersionStrategies = new ConcurrentHashMap<>();

    public void registerVersionResolutionStrategy(String mapName, IResolveDependencyStrategy strategy)
            throws ResolveDependencyStrategyException {
        String version = VersionManager.getCurrentModule().getVersion();
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
                    "[WARNING] Replacing chain " + chainId + " by chain from feature " +
                    VersionManager.getCurrentModule().getName() + ":" + version
            );
        } else {
            registerVersionResolutionStrategy(mapName, null);
        }
        return versions;
    }

    @Override
    public <T> T resolve(Object... args) throws ResolveDependencyStrategyException {
        String mapName = String.valueOf(args[0]);
        if (mapName == null) {
            throw new ResolveDependencyStrategyException("Map name cannot be null.");
        }
        IModule currentModule = VersionManager.getCurrentModule();
        Map<String, String> versions = chainIds.get(mapName);
        if (versions == null) {
            versions = registerMap(mapName, currentModule.getVersion());

        }
        String version;
        IObject message = VersionManager.getCurrentMessage();
        if (message != null) {
            version = chainVersionStrategies.get(mapName).resolveVersion(message);
        } else {
            version = currentModule.getVersion();
        }
        if (version == null) {
            throw new ResolveDependencyStrategyException("Resolution failed for map name '"+mapName+"'.");
        }

        String chainId = versions.get(version);
        if (chainId == null) {
            registerMap(mapName, version);
        }

        return (T) chainId;
    }
}