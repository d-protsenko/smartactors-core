@ECHO OFF
if "%1" == "make" (

    if "%2" == "jar" (

        if "%3" == "source" (

            echo "make project jars (source)"
            echo "execute [mvn clean install -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=]"
            mvn clean install -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=
        ) else (
            echo "make project jars"
            echo "execute [mvn clean install -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**]"
            mvn clean install -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
        )
    ) else if "%2" == "zip" (

        echo "make project zips"
        echo "execute [mvn clean install -Dbuild.format=zip -Dbuild.unpack=false -Dbuild.includeBaseDirectory=true -Dbuild.exclude=**/**]"
        mvn clean install -Dbuild.format=zip -Dbuild.unpack=false -Dbuild.includeBaseDirectory=true -Dbuild.exclude=**/**
    ) else (
        echo "make project jars"
        echo "execute [mvn clean install -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**]"
        mvn clean install -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
    )
) else if "%1" == "deploy" (

    if "%2" == "jar" (

        if "%3" == "source" (

            echo "deploy jar artifacts (source)"
            echo "execute [mvn deploy -Dmaven.test.skip=true -DdeployOnly=true -Ddeploy.format=jar -Ddeploy.classifier=sources]"
            mvn deploy -Dmaven.test.skip=true -DdeployOnly=true -Ddeploy.format=jar -Ddeploy.classifier=sources
        ) else (
            echo "deploy jar artifacts"
            echo "execute [mvn deploy -Dmaven.test.skip=true -DdeployOnly=true -Ddeploy.format=jar -Ddeploy.classifier=]"
            mvn deploy -Dmaven.test.skip=true -DdeployOnly=true -Ddeploy.format=jar -Ddeploy.classifier=
        )
    ) else if "%2" == "zip" (

        echo "deploy zip artifacts"
        echo "execute [mvn deploy -Dmaven.test.skip=true -DdeployOnly=true -Ddeploy.format=zip -Ddeploy.classifier=]"
        mvn deploy -Dmaven.test.skip=true -DdeployOnly=true -Ddeploy.format=zip -Ddeploy.classifier=
    ) else (
        echo "deploy jar artifacts"
        echo "execute [mvn deploy -Dmaven.test.skip=true -DdeployOnly=true -Ddeploy.format=jar -Ddeploy.classifier=]"
        mvn deploy -Dmaven.test.skip=true -DdeployOnly=true -Ddeploy.format=jar -Ddeploy.classifier=
    )
    echo "deploy project components"
) else (
    echo "das ..."
    java -D -jar "C:\Program Files\das\das.jar" %*
)