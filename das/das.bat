@ECHO OFF
if "%1" == "make" (
    if "%2" == "jar" (
        if "%3" == "source" (
            echo "make project jars (source)"
            mvn clean install -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=
        ) else (
            echo "make project jars"
            mvn clean install -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
        )
    ) else if "%2" == "zip" (
        echo "make project zips"
        mvn clean install -Dbuild.format=zip -Dbuild.unpack=false -Dbuild.includeBaseDirectory=true -Dbuild.exclude=**/**
    ) else (
        echo "make project zips"
        mvn clean install -Dbuild.format=zip -Dbuild.unpack=false -Dbuild.includeBaseDirectory=true -Dbuild.exclude=**/**
    )
) else if "%1" == "deploy" (
    if "%2" == "jar" (
        if "%3" == "source" (
            echo "make project jars"
            mvn -Dmaven.test.skip=true -DdeployOnly=true -Ddeploy.format=jar -Ddeploy.classifier=sources deploy
        ) else (
            echo "make project jars"
            mvn -Dmaven.test.skip=true -DdeployOnly=true -Ddeploy.format=jar -Ddeploy.classifier= deploy
        )
    ) else if "%2" == "zip" (
        mvn -Dmaven.test.skip=true -DdeployOnly=true -Ddeploy.format=zip -Ddeploy.classifier= deploy
    ) else (
        mvn -Dmaven.test.skip=true -DdeployOnly=true -Ddeploy.format=zip -Ddeploy.classifier= deploy
    )
    echo "deploy project components"
) else (
    echo "das ..."
    java -D -jar "C:\Program Files\das\das.jar" %*
)