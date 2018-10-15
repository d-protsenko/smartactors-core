package info.smart_tools.smartactors.version_management.chain_version_manager;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.iobject.iobject.IObject;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class ChainVersionStrategies {
    private String mapName;
    private List<IResolveDependencyStrategy> versionResolutionStrategies;
    private List<String> versions;

    ChainVersionStrategies(String mapName) {
        this.mapName = mapName;
        this.versionResolutionStrategies = Collections.synchronizedList(new LinkedList<>());
        this.versions = Collections.synchronizedList(new LinkedList<>());
    }

    void registerVersionResolutionStrategy(String version, IResolveDependencyStrategy strategy) {
        int idx, order;
        for (idx = 0; idx < versions.size(); idx++) {
            order = versions.get(idx).compareTo(version);
            if (order == 0) {
                if (strategy != null) {
                    versionResolutionStrategies.remove(idx);
                    versionResolutionStrategies.add(idx, strategy);
                }
                return;
            } else if (order < 0) {
                break;
            }
        }
        versions.add(idx, version);
        versionResolutionStrategies.add(idx, strategy);
    }

    String resolveVersion(IObject message)
            throws ResolveDependencyStrategyException {

        String version = null;
        for(IResolveDependencyStrategy strategy : versionResolutionStrategies) {
            if (strategy != null) {
                try {
                    version = strategy.resolve(message);
                    if (version != null) {
                        break;
                    }
                } catch (Throwable e) {
                    System.out.println("[WARNING] Chain '"+ mapName +"' version resolution strategy thrown exception: "+ e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        if (version == null) {
            version = versions.get(0);
        }
        return version;
    }
}