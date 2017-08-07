# Checkpoints

## 1. Actor configuration

There should be one checkpoint actor configured for a server (no matter how many checkpoints present in application). The checkpoint actor description (in `"objects"` section) should look like the following:

``` JavaScript
{
  "name": "checkpoint", // Reserved name for checkpoint actor
  "kind": "stateless_actor",
  "dependency": "checkpoint actor",
  // Dependencies of connection options and connection pool
  "connectionOptionsDependency": ". . .",
  "connectionPoolDependency": ". . .",
  // Name o collection to use to store messages
  "collectionName": ". . ."
}
```

You should also define the chain that will be used to send feedback messages:

```JavaScript
{
    "id": "checkpoint_feedback_chain",          // Reserved name for feedback chain
    "steps": [
        {
            "target": "checkpoint",
            "handler": "feedback",
            "wrapper": {
                "in_getPrevCheckpointEntryId": "message/prevCheckpointEntryId",
                "in_getPrevCheckpointId": "message/prevCheckpointId",
                "in_getCheckpointEntryId": "message/checkpointEntryId",
                "in_getResponsibleCheckpointId": "message/responsibleCheckpointId"
            }
        }
    ],
   "exceptional": []
}
```

This chain may be different if application runs on multiple servers with own checkpoint actors.

## 2. Checkpoint creation

The checkpoint may be added in the end of a receiver chain by adding the `"checkpoint"` section to it's definition:

``` JavaScript
{
  "maps": [
    {
      "id": "myChain",
      "steps": [
        {...}
      ],
      ...,
      "checkpoint": {
        "id": "myCheckpoint1",  // Unique identifier of this checkpoint
        "scheduling": {
          "strategy": "checkpoint repeat strategy",  // Name of scheduling strategy used to determine
                                                                           // when to re-send a message (see pt.3)
          ...
        },
        "recover": {
          "strategy": "single chain recover strategy", // Strategy determining what chain to use when
                                                                              // re-sending a message (see pt.4)
          ...
        }
      }
    }
  ]
}
```

The checkpoint declared this way is **always** implicitly placed **in the end** of a chain. There may be only **one checkpoint per chain**.

It's also possible to insert checkpoint in any other place of a chain:

```JavaScript
{
  "maps": [
    {
      "id": "myChain",
      "steps": [
        ...,
        {
          "target": "checkpoint",
          "handler": "enter",
          "wrapper": {
            "in_getProcessor":"processor",
            "in_getMessage":"message",
            "in_getCheckpointId":"arguments/id",
            "in_getSchedulingConfiguration":"arguments/scheduling",
            "in_getRecoverConfiguration":"arguments/recover",
            "in_getCheckpointStatus":"message/checkpointStatus",
            "out_setCheckpointStatus":"message/checkpointStatus"
          },

          // Same fields as in previous case
          "id": "myCheckpoint1",
          "scheduling": {...},
          "recover": {...}
        },
        ...
      ]
    }
  ]
}
```

## 3. Scheduling strategies

There are two strategies available:
- `"checkpoint repeat strategy"` -- re-send message fixed amount of times with fixed intervals:

``` JavaScript
{
  "strategy": "checkpoint repeat strategy",
  "interval": "PT3H", //Interval in ISO-8601 format
  "times": 3 // How many times to re-send the message
}
```
- `"checkpoint fibonacci repeat strategy"` -- re-send message with intervals proportional to Fibonacci numbers:

``` JavaScript
{
  "strategy": "checkpoint fibonacci repeat strategy",
  "baseInterval": "PT30M", // Interal that will be multiplied by the next number of Fibonacci
                                          // sequence every time
  "times": 4 // How many times to re-send the message
}
```
## 4. Recovery strategies

Recover strategy determines where (to what chain) to re-send the message.

There are two strategies available:

- `"single chain recover strategy"` -- re-send message to the same chain every time:

``` JavaScript
{
  "strategy": "single chain recover strategy",
  "chain": "recoveryChain" // Name of the chain where the message will be sent
}
```
- `"chain sequence recover strategy"` -- re-send message to (probably) different chains every time:

``` JavaScript
{
  "strategy": "chain sequence recover strategy",
  "trials": [1,2,1,3],                          // Number of re-send trials to switch to next chain after
  "chains": ["A", "B", "C", "D", "E"]  // Names of chains to use

  // In this example the message will be re-sent to chain "A" once then two times to chain "B" then
  // once to "C" then 3 times to "D" and then to "E" all remaining times (of course the message will not
  // be re-sent if the next checkpoint notifies this one)
}
```

## 5. Failure actions

Checkpoint executes a action (named *"checkpoint failure action"*) on lost messages (the message is lost
when checkpoint does not get a feedback message from the next checkpoint and has no more trials to re-send the message).

The failure action may be configured using `"checkpoint_failure_action"` section:

```JavaScript
"checkpoint_failure_action": {
  // Name of the action dependency
  "action": "send to chain checkpoint failure action",

  // Action-specific parameters (example for "send to chain" action,
  // the only available now implementation):

  // Chain to send envelope to
  "targetChain": "myLastResortChain",
  // Field of the envelope where to store the message
  "messageField": "message"
}
```

