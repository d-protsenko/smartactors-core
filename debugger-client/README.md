# SmartActors chain debugger

## Server configuration

Features `debugger` and `debugger-plugins` should be present and HTTP endpoint with default chain
choice strategy should be configured on server.

## Client requirements

_Python 2.x_ (at least 2.7) should be installed on client. Also you may install IPython to provide better experience
(with autocomplete, command history, etc.):

```
# pip install ipython
```

## Client startup

To start the CLI client type:

```
$ ./debug.py
```

By default the client will connect to local server at port `9909` and use chain named `"execute_debugger_command"` to send
command messages to debugger.

The following options are available:

* `-h`, `--help` - show this help message and exit
* `--url URL`, `-u URL` - use custom URL of the server endpoint
* `--cchain chain`, `-d chain` - use custom chain to send command messages to debugger
* `--chain chain`, `-c chain` - chain to be debugged (you may set it later)
* `--init script`, `-i script` - filename of the script to execute before start of interactive debugging. All the functions
available in interactive mode are also available in the script

## Interactive mode functions

CLI debugger client uses python (or ipython, if available) REPL with additional specific functions as a "user interface".

### Session management

To start debugging you need first to connect to a _debugger session_.

To create new session and connect to it call `newSession` function:

```
In [1]: newSession()
New session created: 3a620164-442f-4f95-97c2-7a3feea57aa7
```

This function will create new session and connect to it.

Sessions are stored until server shutdown or explicit deletion.

To show list of all present sessions call `sessions`:

```
In [6]: sessions()
0) 	67811483-7d77-466f-a03e-d9e5f468f4a9
1) 	012e1975-ab05-4d7e-9c3f-172c772101ab
2) 	a98cc5ac-317d-4fff-8714-31fe5be10355
3) 	0ff4c70a-8638-48cc-bc8e-b328f878a17f

```

To connect to one of sessions shown on previous `sessions` call use `connect` (you may also call `session` to get identifier
of current session):

```
In [7]: connect(2)
In [8]: session()
Out[8]: u'a98cc5ac-317d-4fff-8714-31fe5be10355'
```

To close current session call `close`:

```
In [11]: close()
In [12]: session()

```

### Session state

To show state of current session call `state`:

```
In [15]: state()
Not started ; debugging chain: <not set> ; step mode: all ; on exception: handle ; max stack depth: 5
Message content: <not set>
```

The first line of output will contain:

* State of the session:
    * "Not started" - debugging is not started yet
    * "Running" - the debugging is started and some step of message processing is being executed; message content is not
        available in this state.
    * "Paused" - the debugging is started and message processing is paused because of breakpoint hit, step completion in step
        mode or exception.
    * "Paused (completed)" - the debugging is started and the end of message processing sequence is reached.
* Name of the chain being debugged. It may be set by command line argument or by call to `setChain` in "Not started" state.
    Debuggable chain may not be changed after debugging is started.
* Step mode:
    * "all" - debugging will be paused after every step completion.
    * "none" - debugging wil not be paused.
    * _(number)_ - debugging will be paused after completion of any step on any level of message processing sequence lower
        than the number.
* Behaviour in case of exception:
    * "handle" - debugger will try to handle the exception using chain-level exception handling. The debugging will be
        paused if the exception cannot be handled.
    * "break" - debugging will be paused when any exception occurs.
* Maximum depth of chains stack in sequence. It may be changed by `setStackDepth` call.

The following lines may contain:

* Content of the message. Not available in "Running" state.
* The last exception occurred. You may use `processException` to handle it using chain-level exception handling.

There also are some functions to modify/access some parts of session state:

* `setMessage` - set the whole message; available only in "Not started" state
* `setMessageField` - set a single field of the message; available in "Not started" and "Paused" states
* `setChain` - set name of the chain to debug
* `setStackDepth` - set maximal depth of message processing sequence
* `setStepMode` - set step mode; acceptable arguments are `"all"`, `"none"` and positive numbers (not greater than `2**31`)
* `setBreakOnException` - set if debugger should pause message processing when exception occurs
* `getMessage` - get the message
* `getException` - get the last exception occurred (serialized Java exception)

### Start/stop and pause/continue debugging

To start processing of debuggable message call `start`:

```
In [12]: start()
OK
```

The debugging will not be started if message content is not set or debuggable chain is not set.

To stop debugging call `stop`:

```
In [102]: stop()
OK
```

**Important:** `stop` _**does not**_ interrupt the current receiver being executed, so it will not stop it if it hangs

To pause message processing use `pause`:

```
In [14]: pause()
OK
```

**Important:** `pause` _**does not**_ interrupt the current receiver being executed, so it will not stop it if it hangs
and _**does not**_ pause the message processing immediately - it will e paused only when current step is completed

To continue processing of the message when it is paused call `cont` (I could not use `continue` as it is a Python keyword):

```
In [15]: cont()
OK
```

If you call `cont` after exception occurred the exception _**will not**_ be processed using chain-level exception handling.
Even if it was not processed at all ("on exception: break") or could not be handled automatically ("on exception: handle",
or `processException` was called).

### Message processing sequence state

To show current state of message processing sequence ("stack trace") call `trace`:

```
In [29]: trace()
Level 0 ; chain: buggyChain
-> 0; 0) actorA#handlerB
   0; 1) otherActor#someHandler
Level 1 ; chain: chain2
-  1; 0) actorB#handlerZ
-> 1; 1) actorA#handlerB
Level 2 ; chain: chain3
   2; 0) actorA#handlerX
   2; 1) myActor#yourHandler
   2; 2) yourActor#myHandler
```

The pointer (`->`) points to the last _executed_ receiver at each level.

It's also possible to access the raw stack trace object using `getStackTrace`.

You can manipulate sequence state in two ways:

* use `call` to call a chain with a specific name.
* use `goTo` to go to a specific step at some sequence level. `goTo` takes two arguments - level index and step index you
    may use the ones you can find in output of `trace`.

### Breakpoints

You may set a breakpoint at specific step of a chain by calling `setBp`:

```
In [40]: setBp('buggyChain', 1)
1
In [40]: setBp('chain3', 2, False)
2
```

The first parameter is a name of the chain, the second one is a index of step (starting from 0, just like `trace` shows)
and the third one (optional) is if the newly created breakpoint should be enabled immediately.

The `trace` function will show the breakpoints in the chains present in chains stack (`"*"` means that the breakpoint is
enabled, `"-"` - disabled):

```
In [42]: trace()
Level 0 ; chain: buggyChain
-> 0; 0) actorA#handlerB
    (*1)
   0; 1) otherActor#someHandler
Level 1 ; chain: chain2
-  1; 0) actorB#handlerZ
-> 1; 1) actorA#handlerB
Level 2 ; chain: chain3
   2; 0) actorA#handlerX
   2; 1) myActor#yourHandler
    (-2)
   2; 2) yourActor#myHandler
```

To show _all_ breakpoints use `listBp`:

```
In [43]: listBp()
*	1)	step#1 @ chain1	otherActor#someHandler
-	2)	step#0 @ chain2	yourActor#myHandler
```

To modify a breakpoint use `modBp`:

```
In [44]: modBp(2,enable=True)
OK

In [45]: listBp()
*	1)	step#1 @ chain1	otherActor#someHandler
*	2)	step#0 @ chain2	yourActor#myHandler
```

Parameters are breakpoint id (as shown by `listBp`) and named parameters describing breakpoint modifications. The following
modifications are available:

* `enable` - `True` or `False` to enable or disable the breakpoint

### Advanced

#### Direct use of debugger commands

Not all debugger commands are available through individual functions but it's possible to execute any command by calling
`cmd` function:

```
In [15]: cmd('isPaused')
Out[15]: False
```

The first parameter is a name of the command for full list of commands (see sources of [DebuggerActor](https://github.com/SmartTools/smartactors-core/blob/develop/CoreFeatures/Debugger/DebuggerActor/src/main/java/info/smart_tools/smartactors/debugger/actor/DebuggerActor.java)
and [DebuggerSessionImpl](https://github.com/SmartTools/smartactors-core/blob/develop/CoreFeatures/Debugger/DebuggerSessionImpl/src/main/java/info/smart_tools/smartactors/debugger/session_impl/DebuggerSessionImpl.java))
the second (optional, defaults to `None`/JSON `null`) parameter is passed as command argument and the third should be set to
`False` if the command does not require current session id (optional, defaults to `True`).

The `cmd` function returns the command execution result or raises exception if error occurs executing the command.
