# How to use Plugins

Plugin is a set of functionality, i.e. Java classes packed into one or multiple JAR files.
The Server loads and initializes such JARs.

Loading and initializing are two separate steps of the process.

At the first step a part of the server, the [`PluginLoader`](http://smarttools.github.io/smartactors-core/apidocs/info/smart_tools/smartactors/core/plugin_loader_from_jar/PluginLoader.html) scans the JAR file for implementations of [`IPlugin`](http://smarttools.github.io/smartactors-core/apidocs/info/smart_tools/smartactors/core/iplugin/IPlugin.html).
It uses [`PluginCreator`](http://smarttools.github.io/smartactors-core/apidocs/info/smart_tools/smartactors/core/plugin_creator/PluginCreator.html) to instantiate the Plugin by passing the instance of `IBootstrap` to it's constructor.
The `load()` method of each Plugin is called here.

In it's `load()` method the Plugin adds it's own [`IBootstrapItem`](http://smarttools.github.io/smartactors-core/apidocs/info/smart_tools/smartactors/core/ibootstrap_item/IBootstrapItem.html) into the [`IBootstrap`](http://smarttools.github.io/smartactors-core/apidocs/info/smart_tools/smartactors/core/ibootstrap/IBootstrap.html) known to him.
The `IBootstrapItem` gives the name of the part of the functionality provided by the plugin and allows to declare dependencies from the other bootstrap items using the `after()` and `before()` methods.
Finally the sequence of the bootstrap items according to the dependencies is constructed and their `executeProcess()` is called.
This is the second step of the initialization.

![Plugins and Bootstrap](http://www.plantuml.com/plantuml/img/IyxFBSZFIyqhKGXEBIfBBU9AXWi4v9IcP-OX2JZbvvSKbnGb5c0Jyon9pUNYWXYYaA-hQwUWfAK4DKF1IY4dFp6b65KUhXKedLgHcbnQaWfK0TMX1JC1nGAWoeAY_BBC591AX7wSYfFpSt9IaqkG5ODba6s7AarAJSilIYMiBZ6j11XA0000)

