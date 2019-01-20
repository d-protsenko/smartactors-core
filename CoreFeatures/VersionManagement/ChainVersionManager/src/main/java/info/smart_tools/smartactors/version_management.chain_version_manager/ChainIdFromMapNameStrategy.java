package info.smart_tools.smartactors.version_management.chain_version_manager;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import info.smart_tools.smartactors.class_management.module_manager.ModuleManager;
import info.smart_tools.smartactors.iobject.iobject.IObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChainIdFromMapNameStrategy {

    Map<Object, Map<Comparable, Object>> chainIds = new ConcurrentHashMap<>();
    private static Map<Object, ChainVersionStrategies> chainVersionStrategies = new ConcurrentHashMap<>();
    private IResolutionStrategy resolve_by_message_strategy = new IResolutionStrategy(){
        @Override
        public <T> T resolve(Object... args) throws ResolutionStrategyException {
            return (T) resolve_by_message(args[0], (IObject) args[1]);
        }
    };

    private IResolutionStrategy resolve_by_module_dependencies_strategy = new IResolutionStrategy(){
        @Override
        public <T> T resolve(Object... args) throws ResolutionStrategyException {
            return (T) resolve_by_module_dependencies(args[0]);
        }
    };

    private IResolutionStrategy register_message_version_strategy = new IResolutionStrategy(){
        @Override
        public <T> T resolve(Object... args) throws ResolutionStrategyException {
            registerVersionResolutionStrategy(
                    args[0],                            // map name
                    (IResolutionStrategy)args[1] // message version resolution strategy
            );
            return (T) null;
        }
    };

    public IResolutionStrategy getRegisterMessageVersionStrategy() {
        return register_message_version_strategy;
    }

    public IResolutionStrategy getResolveByMessageStrategy() {
        return resolve_by_message_strategy;
    }

    public IResolutionStrategy getResolveByModuleDependenciesStrategy() {
        return resolve_by_module_dependencies_strategy;
    }

    private void registerVersionResolutionStrategy(Object mapName, IResolutionStrategy strategy)
            throws ResolutionStrategyException {
        Comparable version = ModuleManager.getCurrentModule().getVersion();
        if (mapName == null || version == null) {
            throw new ResolutionStrategyException("Chain name and version cannot be null.");
        }
        ChainVersionStrategies versionStrategies = chainVersionStrategies.get(mapName);
        if (versionStrategies == null) {
            versionStrategies = new ChainVersionStrategies(mapName);
            chainVersionStrategies.put(mapName, versionStrategies);
        }
        versionStrategies.registerVersionResolutionStrategy(version, strategy);
    }

    private Object registerChain(Object mapName, Comparable version)
            throws ResolutionStrategyException {

        //if (mapName == null || version == null) {
        //    throw new ResolutionStrategyException("Map name or version cannot be null.");
        //}

        Map<Comparable, Object> versions = chainIds.get(mapName);
        if (versions == null) {
            versions = new ConcurrentHashMap<>();
            chainIds.put(mapName, versions);
        }

        String chainId = mapName.toString() + ":" + version.toString();
        Object previous = versions.put(version, chainId);
        if (previous != null) {
            System.out.println(
                    "[WARNING] Replacing chain '" + chainId + "' by chain from feature " +
                    ModuleManager.getCurrentModule().getName() + ":" + version.toString()
            );
        } else {
            registerVersionResolutionStrategy(mapName, null);
        }
        return chainId;
    }

    private Object resolve_by_message(Object chainName, IObject message)
            throws ResolutionStrategyException {
        if (chainName == null) {
            throw new ResolutionStrategyException("Chain name cannot be null.");
        }
        if (chainName.toString().indexOf(":") > -1) {
            return chainName;
        }
        if (message == null) {
            throw new ResolutionStrategyException("Message for chain Id resolution cannot be null.");
        }

        Comparable version = chainVersionStrategies.get(chainName).resolveVersion(message);
        return resolve_by_version(chainName, version);
    }

    private Object resolve_by_module_dependencies(Object chainName)
            throws ResolutionStrategyException {
        if (chainName.toString().indexOf(":") > -1) {
            return chainName;
        }
        Comparable version = ModuleManager.getCurrentModule().getVersion();
        return resolve_by_version(chainName, version);
    }

    private Object resolve_by_version(Object chainName, Comparable version)
            throws ResolutionStrategyException {

        //if (chainName == null) {
        //    throw new ResolutionStrategyException("Chain name cannot be null.");
        //}

        if (version == null) {
            throw new ResolutionStrategyException("Chain Id resolution failed for chain name '"+chainName+"'.");
        }

        Map<Comparable, Object> versions = chainIds.get(chainName);
        if (versions == null) {
            registerChain(chainName, version);
            versions = chainIds.get(chainName);
        }

        Object chainId = versions.get(version);
        if (chainId == null) {
            chainId = registerChain(chainName, version);
        }

        return chainId;
    }

}