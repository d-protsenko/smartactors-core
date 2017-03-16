Installation:

- compile project:
   > mvn clean package
- make directory
   > ~/bin
- copy to this directory the 'das' file and the compiled jar file - 'das.jar'
- add 'bin' directory to the PATH:
   > export $PATH=$PATH:~/bin/
- in any directory run 'das':
   > das
   and result should be like a:
   > Distributed Actor System. Design, assembly and deploy tools.
   > Version 0.3.2.


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
