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

Declaring such exceptions for getters and setters is the only requirement for the wrapper interface.

## Wrapper configuration

Which data to return by getters or receive in setters is defined by the wrapper configuration.
It's part of the [message map](MessageMapExample.html) definition.

For example:

    "wrapper": {
        "in_getName": "message/personName",
        "out_setGreeting": "response/greeting",
    }
    
This was a short form of wrapper definition. Actually it's expanded to this.

    "wrapper": {
        "in_getName": [
            { 
                "name": "wds_getter_strategy",
                "args": [ "message/personName" ]
            }
        ],
        "out_setGreeting": [[
            {
                "name": "wds_target_strategy",
                args: [ "local/value", "response/greeting" ]
            }
        ]]
    }
    
In methods have a list of transformation strategies which are to extract data.
Out methods have a list of a list of transformation strategies which are to set data.
The two nesting level for the setter is necessary because the same single value passed to the setter can be put to different set of system objects, to multiple destinations.

## Environment
    
Note the "message", "local" and "response" strings in the wrapper configuration above. 
They are environment objects.

Each of them is represented as [IObject](IObjectExample.html): a set of named fields and values.
The values can be scalars, like strings and numbers, lists, nested IObjects or even plain Java objects.

To access the values for each environment object use a slash-separated path: `object_name/field_name`.
The path can go deeper to nested objects: `object_name/nested_object/nested_field`.

### Message

`message` is the current processing message represented as IObject.
It's the message received by the endpoint and processed by the previous actors in the message map.

Actors are able to modify fields of the message, add new fields, delete fields, etc...

Note one actor can use a setter of it's wrapper to set a value to the message, another actor can read this value using it's wrapper getter.
It's not necessary for both actors to negotiate the name of the message field they use, because all mapping, in both directions from actor to the message and from the message to the actor, are handled by the wrapper.

### Response

`response` is the object (as IObject) which will be returned via endpoint as the response to the request.

Initially response is empty. 
It is passed through all actors in the message map, each actor can add or modify fields in it.
Then the "respond" actor returns the response back to the client who sent the message to the endpoint.
 
### Local
 
There is only one value called `local/value`. 
It's used to pass a value between transformation strategies.
 
For getters the initial `local/value` is `null`.
For setters the initial `local/value` is set to the object passed to the setter from the actor.
Then, for next strategies in the transformation chain (see below), the `local/value` is set to the result of execution of the previous strategy in the chain.

### Const

`const` is a special way to pass a constant string through the getter to your actor.

For example, `const/value` will pass the String "value" to the actor.

Think of such constants as a way to tune you actor in-place.
You define some getter in the wrapper to get some configuration parameter.
And you define the actual parameter value in the message map wrapper configuration.

### Context

`context` is used to keep some unserializable Java objects which are not part of request or response and are actual during the current request processing.

The typical example of such object is HTTP request of the HTTP endpoint, it's available as `context/request`.
Also the context can be used to set HTTP headers and cookies to the HTTP response.

Note the context mostly contain data specified to the used endpoint and protocol. 
Typical business logic actors should avoid to use `context`. 
They should use `message` and `response` to interact with each other and the client.

## List of transformation strategies
    
### In/getter
    
TBD
    
### Out/setter
    
TBD

### Short syntax

TBD
    
## How to write own transformation strategy
    
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
    
    
