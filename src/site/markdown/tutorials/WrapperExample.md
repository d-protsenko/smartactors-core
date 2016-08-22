# How to use Wrappers

The Actors in SmartActors are able to declare the data items they need from the system and they want to return to the system.
This is done with Wrappers.

## Handler and wrapper interface

The actor declares a handler method which receives the wrapper interface.

    public class HelloActor {
        public void hello(final GreetingMessage message) throws HelloActorException {
            // do something with GreetingMessage
        }
    }
    
The interface defines the data pieces needed by the actor.

    public interface GreetingMessage {
        String getName() throws ReadValueException;
        void setGreeting(String greeting) throws ChangeValueException;
    }
    
Getter is used to take data from the system INto the actor. 
So we called it IN-method.
It throws `ReadValueException` because sometimes the reading of the data may fail.
    
Setter is used to put data from the actor OUTto the system.
So we called it OUT-method.
It throws `ChangeValueException` because sometimes the writing of the data may fail.

## Wrapper configuration

Which data to return by getters or receive in setters is defined by the wrapper configuration.
It's part of the [message map](MessageMapExample.html) definition.

For example:

    "wrapper": {
        "in_getName": "message/personName",
        "out_setGreeting": "response/greeting",
    }
    
TBD

## Environment
    
TBD

## List of transformations
    
### In/getter
    
TBD
    
### Out/setter
    
TBD

### Short syntax

TBD
    
## Transformation strategy
    
TBD    
    
## Under the hood

### Config normalization

TBD

### WDSObject

[WDSObject](../apidocs/info/smart_tools/smartactors/core/wds_object/WDSObject.html) is built over "wrapper" configuration object.

    IObject config = IOC.resolve(iObjectKey,
            "{" +
            "\"in_getName\": \"message/personName\"," +
            "\"out_setGreeting\": \"response/greeting\"" +
            "}");
    WDSObject wrapper = IOC.resolve(Keys.getOrAdd(WDSObject.class.getCanonicalName()), config);
    
TODO: canonical version of config

It's created for each "wrapper" section in the config.

WDSObject is initialized by the environment object when it's necessary to process the message.

    IObject environment = IOC.resolve(iObjectKey,
            "{" +
            "\"message\": { \"personName\": \"Ivan\" }," +
            "\"response\": {}" +
            "}");
    wrapper.init(environment);
    
The environment contains the message, context, response, etc. fields to process.
    
### WrapperGenerator

[WrapperGenerator](../apidocs/info/smart_tools/smartactors/core/wrapper_generator/WrapperGenerator.html) generates a class in runtime which implements IObject, IObjectWrapper and the interface of the parameter of the actor's handler.

Instance of this class is initialized by WDSObject created on the previous step.

Then the instance is passed to the actor's handler.
Each calls to it's methods are just translated into access to WDSObject fields causing the strategies to be applied to data and the result to affect the environment.

TODO: examples
    
    
