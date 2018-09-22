time /T
cd Base
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd ClassManagement
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd FeatureLoadingSystem
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Helpers
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Security
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Security-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Scope
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Scope-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd IObject
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd IObject-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd DumpableInterface
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Dumpable-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd ConfigurationManager
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Configuration-manager-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd IOC
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd IOC-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Field
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Field-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd IOCStrategyPack
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd IOCStrategyPack-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Task
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd TaskBlockingQueue-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd TaskNonBlockingQueue-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Database
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Database-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd DatabaseNullConnectionPool-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Database-postgresql
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Database-postgresql-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Database-in-memory
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Database-in-memory-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd IObjectExtension
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd IObject-extension-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd MessageProcessingInterfaces
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd MessageProcessing
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd MessageProcessing-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd FeatureManagement
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Endpoint
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Endpoint-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd MessageBus
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd HttpEndpoint
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd HttpEndpoint-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd HttpsEndpoint
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd HttpsEndpoint-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Testing
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Testing-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Timer
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Timer-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Scheduler
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Scheduler-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Checkpoint
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Checkpoint-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Parser
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Statistics
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Statistics-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Debugger
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Debugger-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Shutdown-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd SchedulerAutoStartup
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd CheckpointAutoStartup
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd Shutdown
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd DatabasePostgresqlAsyncOpsCollection
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd DatabasePostgresqlCachedCollection
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd MessageBus-plugins
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd CoreServiceStarter
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd InMemoryDatabaseServiceStarter
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd MessageBusServiceStarter
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd OnFeatureLoadingServiceStarter
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd GlobalConstantsServiceStarter
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd EndpointServiceStarter
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..
cd DatabaseServiceStarter
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=jar -Dbuild.unpack=true -Dbuild.includeBaseDirectory=false -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..\..\Servers\Server2
call mvn clean install -o -Dmaven.test.skip=true -Dbuild.format=zip -Dbuild.unpack=false -Dbuild.includeBaseDirectory=true -Dbuild.exclude=**/**
if not "%ERRORLEVEL%" == "0" exit /b
cd ..\..\CoreFeatures
time /T
