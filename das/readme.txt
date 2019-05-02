Installation:

Download and install debian package from:
https://repository.smart-tools.info/artifactory/#artifact~smartactors_development_tools/das
    > sudo dpkg -i das-${version}.deb

Convention:
- Project/feature/actor/plugin name:
  must be in camel case format


  Examples:
    - name - 'MyNewActor' will be transformed to:
        - 'MyNewActor' for directory name,
        - 'my-new-actor' for artifact id,
        - 'my_new_actor' for package name.
    - name - 'IInterface' will be transformed to :
        - 'IInterface' for directory name,
        - 'iinterface' for artifact id and package name

    - name - 'My.NewActor' will be transformed to:
        - 'NewActor' - for directory name, and directory structure will like a 'NewActor/src/main/java/.../My/NewActor/NewActor.java' for actors and plugins
        - 'my.new-actor' for artifact id,
        - 'my.new_actor' for package name.

Usage:

help:
    > das -h


project import:
    > das import -sl CurrentProjectDirectory -path NewProjectLocation -pn NewProjectName -g projectGroupId -v ProjectVersion


new project creation:
    > das cp -pn NewProjectName -g my.project.groupid -v 0.2.0-SNAPSHOT


new feature creation:
    in the project directory:
    > das cf -fn NewFeatureName -g my.feature.groupid -v 0.2.0-SNAPSHOT
        (by default group id and version will be like a project)


new actor creation:
    in the project directory:
    > das ca -fn NewFeatureName -an NewActor


new plugin creation:
    in the project directory:
    > das cpl -fn NewFeatureName -pln NewActor
        (command watching for plugin name postfix 'Plugin'. if it doesn't exist it will added postfix 'Plugin':
            NewActor       -> NewActorPlugin
            NewActorPlugin -> NewActorPlugin
        )


add or update upload repository to the feature:
    in the project directory:
    > das afr -fn FeatureName -rid my-feature-upload-repository -rurl https://repository.smart-tools.info/artifactory/my-feature-upload-repository/
add or update upload repository to all features:
    > das afr -fn all -rid my-feature-upload-repository -rurl https://repository.smart-tools.info/artifactory/my-feature-upload-repository/


add or update upload repository to the actor:
    > das aar -an ActorName -fn FeatureName -rid my-actor-upload-repository -rurl https://repository.smart-tools.info/artifactory/my-actor-upload-repository/
add or update upload repository to all actors of specific feature:
    > das aar -an all -fn FeatureName -rid my-actor-upload-repository -rurl https://repository.smart-tools.info/artifactory/my-actor-upload-repository/
add or update upload repository to all actors of all features:
    > das aar -an all -fn all -rid my-actor-upload-repository -rurl https://repository.smart-tools.info/artifactory/my-actor-upload-repository/

add or update upload repository to the plugin:
    > das aplr -pln PluginName -fn FeatureName -rid my-plugin-upload-repository -rurl https://repository.smart-tools.info/artifactory/my-plugin-upload-repository/
add or update upload repository to all plugins of specific feature:
    > das aar -pln all -fn FeatureName -rid my-plugin-upload-repository -rurl https://repository.smart-tools.info/artifactory/my-plugin-upload-repository/
add or update upload repository to all plugins of all features:
    > das aar -pln all -fn all -rid my-plugin-upload-repository -rurl https://repository.smart-tools.info/artifactory/my-plugin-upload-repository/


add or update upload repository to feature on feature creation:
    > das aofcur -rid my-feature-upload-repository -rurl https://repository.smart-tools.info/artifactory/my-feature-upload-repository/


add or update upload repository to actor on actor creation:
    > das aoacur -rid my-actor-upload-repository -rurl https://repository.smart-tools.info/artifactory/my-actor-upload-repository/


add or update upload repository to plugin on plugin creation:
    > das aoplcur -rid my-plugin-upload-repository -rurl https://repository.smart-tools.info/artifactory/my-plugin-upload-repository/


update feature version:
    > das ufv -fn FeatureName -v 0.2.1
update version of all features:
    > das ufv -fn all -v 0.2.1

update actor version:
    > das uav -fn FeatureName -an ActorName -v 0.2.1
update version of all actors for specific feature:
    > das uav -fn FeatureName -an all -v 0.2.1
update version of all actors for all features:
    > das uav -fn all -an all -v 0.2.1


update plugin version:
    > das uplv -fn FeatureName -pln PluginName -v 0.2.1
update version of all plugins for specific feature:
    > das uplv -fn FeatureName -pln all -v 0.2.1
update version of all plugins for all features:
    > das uplv -fn all -pln all -v 0.2.1


create server:
    > das cs -aid artifactId -g groupId -v version -path serverDestination -sn ServerName -rid smartactors_servers -rurl https://repository.smart-tools.info/artifactory/smartactors_servers/
    Default for:
        aid  - servers.server2
        g    - info.smart_tools.smartactors
        v    - RELEASE
        path - current directory
        sn   - server
        rid  - smartactors_servers
        rurl - https://repository.smart-tools.info/artifactory/smartactors_servers/


download server core:
    > das dc -aid coreListArtifactId -g coreListGroupId -v coreListVersion -path ServerLocation -rid core-pack -rurl https://repository.smart-tools.info/artifactory/smartactors_core_and_core_features/
    Default for:
        aid  - core-pack
        g    - info.smart_tools.smartactors
        v    - RELEASE
        rid  - smartactors_core_and_core_features
        rurl - https://repository.smart-tools.info/artifactory/smartactors_core_and_core_features/
        path - current directory
download specific core:
    > das dc -path ServerLocation -sl coreListFileLocation
        coreList file must looks like as follows:
        {
            "repositories": [{
                    "repositoryId" : "real repository id",
                    "type": "default",
                    "url": "real repository url"
                },
                ...
            ],
            "features": [{
                    "group": "feature group id",
                    "name": "feature name",
                    "version": "feature version"
                },
                ...
            ]
        }
