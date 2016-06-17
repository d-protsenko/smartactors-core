# How to use Scopes

## Overview

Scope is a key-value storage, where system [service locators]((https://en.wikipedia.org/wiki/Service_locator_pattern)) like [IoC](IOCExample.html) are able to store their internal data.

### Requirement for the keys

Because the data is internal, it's better to use a random [GUID](https://en.wikipedia.org/wiki/Globally_unique_identifier) as a key to private data. This key is known only to the users of the data, other users cannot guess the key.

    Object scopeKey = ScopeProvider.createScope(null);
    IScope scope = ScopeProvider.getScope(scopeKey);
    Object key = new Key(java.util.UUID.randomUUID().toString());
    Object value = new Object();
    scope.setValue(key, value);
    // ...
    value = scope.getValue(key)

### Why you need scopes        
        
Service locators provide the globally available API, so the scopes give ability to separate the data of service locators in different contexts. For example, you can define totally independent scope for testing environment, so your tests will take the IoC data totally independent on the IoC of the main application.

The service locator should always take it's data from the _current scope_. But the current scope can be defined and changed externally.
  
Also scopes can be nested into each other. This allows to _override_ data. If some key is not defined in the current scope, it may be looked in the parent scope. As the opposite you can redefine the value in the current scope, and it doesn't affect the users of the parent scope.

## Nested scopes

For example, let define a main scope. It's has no parents, so `null` is passed to `createScope`.

    Object mainScopeKey = ScopeProvider.createScope(null);
    IScope mainScope = ScopeProvider.getScope(mainScopeKey);
    ScopeProvider.setCurrentScope(mainScope);
    
You can put a value to it. It'll be a main value.

    Object mainValue = new Object();
    ScopeProvider.getCurrentScope().setValue(key, mainValue);
    assertSame(mainValue, ScopeProvider.getCurrentScope().getValue(key));

Now you define a nested scope and make it the current. Pass a main scope as a parent to `createScope`.

    Object nestedScopeKey = ScopeProvider.createScope(mainScope);
    IScope nestedScope = ScopeProvider.getScope(nestedScopeKey);
    ScopeProvider.setCurrentScope(nestedScope);
    
When you read by the same key, you get the value from the main scope.

    assertSame(mainValue, ScopeProvider.getCurrentScope().getValue(key));
    
You can put a new value to the nested scope. Then you'll read the updated value.

    Object nestedValue = new Object();
    ScopeProvider.getCurrentScope().setValue(key, nestedValue);
    assertSame(nestedValue, ScopeProvider.getCurrentScope().getValue(key));
    
However, when you return back to the main scope, you'll read the original value.

    ScopeProvider.setCurrentScope(mainScope);
    assertSame(mainValue, ScopeProvider.getCurrentScope().getValue(key));
    
    