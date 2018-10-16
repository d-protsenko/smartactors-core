package info.smart_tools.smartactors.version_management.chain_version_manager;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.class_management.module_manager.ModuleManager;
import info.smart_tools.smartactors.iobject.iobject.IObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChainIdFromMapNameStrategy {

    Map<Object, Map<Comparable, Object>> chainIds = new ConcurrentHashMap<>();
    private static Map<Object, ChainVersionStrategies> chainVersionStrategies = new ConcurrentHashMap<>();
    private IResolveDependencyStrategy resolve_by_message_strategy = new IResolveDependencyStrategy(){
        @Override
        public <T> T resolve(Object... args) throws ResolveDependencyStrategyException {
            return (T) resolve_by_message(args[0], (IObject) args[1]);
        }
    };

    private IResolveDependencyStrategy resolve_by_module_dependencies_strategy = new IResolveDependencyStrategy(){
        @Override
        public <T> T resolve(Object... args) throws ResolveDependencyStrategyException {
            return (T) resolve_by_module_dependencies(args[0]);
        }
    };

    private IResolveDependencyStrategy resolve_by_version_strategy = new IResolveDependencyStrategy(){
        @Override
        public <T> T resolve(Object... args) throws ResolveDependencyStrategyException {
            return (T) resolve_by_version(args[0], (Comparable)args[1]);
        }
    };

    private IResolveDependencyStrategy register_message_version_strategy = new IResolveDependencyStrategy(){
        @Override
        public <T> T resolve(Object... args) throws ResolveDependencyStrategyException {
            registerVersionResolutionStrategy(
                    args[0],                            // map name
                    (IResolveDependencyStrategy)args[1] // message version resolution strategy
            );
            return (T) null;
        }
    };

    public IResolveDependencyStrategy getRegisterMessageVersionStrategy() {
        return resolve_by_message_strategy;
    }

    public IResolveDependencyStrategy getResolveByMessageStrategy() {
        return resolve_by_message_strategy;
    }

    public IResolveDependencyStrategy getResolveByModuleDependenciesStrategy() {
        return resolve_by_module_dependencies_strategy;
    }

    public IResolveDependencyStrategy getResolveByVersionStrategy() {
        return resolve_by_version_strategy;
    }

    private void registerVersionResolutionStrategy(Object mapName, IResolveDependencyStrategy strategy)
            throws ResolveDependencyStrategyException {
        Comparable version = ModuleManager.getCurrentModule().getVersion();
        if (mapName == null || version == null) {
            throw new ResolveDependencyStrategyException("Chain name and version cannot be null.");
        }
        ChainVersionStrategies versionStrategies = chainVersionStrategies.get(mapName);
        if (versionStrategies == null) {
            versionStrategies = new ChainVersionStrategies(mapName);
            chainVersionStrategies.put(mapName, versionStrategies);
        }
        versionStrategies.registerVersionResolutionStrategy(version, strategy);
    }

    private Object registerChain(Object mapName, Comparable version)
            throws ResolveDependencyStrategyException {

        if (mapName == null || version == null) {
            throw new ResolveDependencyStrategyException("Map name or version cannot be null.");
        }

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
            throws ResolveDependencyStrategyException {
        if (chainName == null) {
            throw new ResolveDependencyStrategyException("Chain name cannot be null.");
        }
        if (message == null) {
            throw new ResolveDependencyStrategyException("Message for chain Id resolution cannot be null.");
        }

        Comparable version = chainVersionStrategies.get(chainName).resolveVersion(message);
        return resolve_by_version(chainName, version);
    }

    private Object resolve_by_module_dependencies(Object chainName)
            throws ResolveDependencyStrategyException {

        Comparable version = ModuleManager.getCurrentModule().getVersion();
        return resolve_by_version(chainName, version);
    }

    private Object resolve_by_version(Object chainName, Comparable version)
            throws ResolveDependencyStrategyException {

        if (chainName == null) {
            throw new ResolveDependencyStrategyException("Chain name cannot be null.");
        }

        if (version == null) {
            throw new ResolveDependencyStrategyException("Chain Id resolution failed for chain name '"+chainName+"'.");
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