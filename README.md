# Smart Actors

Java library to develop server-side applications strongly following [SOLID](https://en.wikipedia.org/wiki/SOLID_\(object-oriented_design\)) principles and implementing [actor model](https://en.wikipedia.org/wiki/Actor_model).

## Documentation

* [Maven site](http://smarttools.github.io/smartactors-core/develop/)
* [JavaDocs](http://smarttools.github.io/smartactors-core/develop/apidocs/index.html)
* [Tutorials](http://smarttools.github.io/smartactors-core/develop/tutorials/)

## Binaries

* [Apache repository](https://repository.smart-tools.info/artifactory/#browse~smartactors_core_and_core_features)

### Use in Maven

When you add a dependency to your `pom.xml`, for example:

    <dependencies>
        <dependency>
            <groupId>info.smart_tools.smartactors</groupId>
            <artifactId>iobject.iobject</artifactId>
            <version>0.2.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
    
You should also add a repository URL to allow Maven to download JARs:

    <repositories>
        <repository>
            <id>smartactors_core_and_core_features</id>
            <name>Smart Actors Core Repository</name>
            <url>https://repository.smart-tools.info/artifactory/</url>
        </repository>
    </repositories>

