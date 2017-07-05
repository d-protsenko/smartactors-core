# Scheduler actor

Scheduler actor configuration:

``` JavaScript
{
  "kind": "actor",
  "dependency": "scheduler actor",
  // Database connection settings
  "connectionOptionsDependency": ". . .",
  "connectionPoolDependency": ". . .",
  // Name of the collection to store entries in
  "collectionName": ". . ."
}
```

To add an entry send a message to `addEntry` handler. Method `getEntryArguments` of message wrapper should be configured to return an `IObject` containing `"strategy"` field with name of scheduling strategy dependency, `"message"` field with the message that should be sent to message bus by the schedule, `"setEntryId"` field with fieldname where the entryId should be set, and fields specific for the strategy.

To list all the active scheduler entries send message to `listEntries` handler.

To cancel (delete) specific entry send message to `deleteEntry` handler.

# Scheduling strategies
## "repeat continuously scheduling strategy"

This strategy will send the message with configured interval, entry will be saved to the database on creation and will not be updated. Requires the following additional arguments (in `IObject` returned by `getEntryArguments` method):
- `"start"` -- date and time (in [ISO 8601 local datetime](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#ISO_LOCAL_DATE_TIME) format). If this time is in the past then the actions that had to be executed before current time wil be skipped. If `null` then the current time is used.
- `"interval"` -- interval in ISO 8601 interval format (see [`Duration#parse`](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-)).
## "do once scheduling strategy"

This strategy will send the message once at configured time. Requires the following arguments:
- `"time"` -- time when to send the message.
- `"save"` -- `true` if the entry should be saved to database, `false` if not
- `"neverTooLate"` -- true if the message should be sent when the entry is loaded from database but the time it was scheduled on has already passed. May be omitted if `"save"` is `false`.
