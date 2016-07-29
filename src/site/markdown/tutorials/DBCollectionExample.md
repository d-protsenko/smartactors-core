# How to work with database collections

## Overview

Collection is the set of documents stored in a database.
We know the document can be presented as [JSON](https://en.wikipedia.org/wiki/JSON) string or [IObject](IObjectExample.html).

Each collection has it's name — the string of alphanumeric characters and `_`.

The only sign the object belongs to the collection is existence in the object the field named "collection_nameID". This field contains the unique identifier of the object in this collection.

For example, if the document is in the "forms" collections, it has the field "formsID". The value of the field is the unique identifier of this document in the collection "forms".

The document can be in multiple collections at the same time. It has multiple ID fields in such case.

## Database query

You should do the following to perform a database query.

1. Resolve [IoC](IOCExample.html) dependency to take command object which interacts with DB.

        ITask task = IOC.resolve(
            Keys.getOrAdd("command_name"),               // each command has it's own unique name
            connection,                             // object - connection to the DB
            "collection_name",                      // each document belongs to the collection
            other comma-separated parameters        // the set of parameters depends on the command
        );

    If the dependency cannot be resolved, `IOC.resolve` throws `ResolveDependencyException`.

2. Execute the task.

        task.execute();

    If the command cannot be executed, `task.execute()` throws `TaskExecutionException`.

## Database commands

### Upsert

Adds the new object to the collection or updates the existed object.

Command name: `db.collection.upsert`

Additional parameters:

- document — `IObject` — object which should be inserted/updated in the DB

The Upsert command checks the existence of value of the "collection_nameID" field, if the ID field is absent it inserts the document, otherwise it updates the document.
When the document is successfully inserted, the command adds the field "collection_nameID" to the document.

#### Example

    ITask task = IOC.resolve(
            Keys.getOrAdd("db.collection.upsert"),
            connection,
            collectionName,
            document
    );
    task.execute();

### Insert

Adds new object to the collection.

Command name: `db.collection.insert`

If the document contains the field "collection_nameID" the command throws a successor of `TaskExecutionException`. In other cases it's behavior is the same as of Upsert command.

It's recommended to use Upsert if there is no strong necessity to use Insert.

### Delete

Deletes the object from the collection.

Command name: `db.collection.delete`

Additional parameters:

- document — `IObject` — the object which should be deleted from the DB collection.

The object must have the field "collection_nameID" which contains the unique identifier of the document in the collection.

When this field is present in the document, it's be tried to delete the object from the collection, the field "collection_nameID" is deleted from the document.

If the document is absent in the collection, no error appears because the absence of the document with the specified id is the target postcondition, the field "collection_nameID" is deleted from the in-memory document. 

### Search

Searching of the document in the collection.

Command name: `db.collection.search`

Additional parameters:

- criteria — search criteria for documents which should be selected from the collection

    TBD

- callback — lambda of type `IAction<IObject[]>` which receives the set of selected documents 

### GetById

Takes the document by it's id.

Command name: `db.collection.getbyid`

Additional parameters:

- id — unique identifier of the document in the collection

- callback — lambda of type `IAction<IObject>` which receives the document got by id

If the object with such id does not exist, the `TaskExecutionException` is thrown.

#### Example

    ITask task = IOC.resolve(
            Keys.getOrAdd("db.collection.getbyid"),
            connection,
            collectionName,
            documentiId,
            (IAction<IObject>) foundDoc -> {
                try {
                    System.out.println("Found");
                    System.out.println((String) doc.serialize());
                } catch (SerializeException e) {
                    throw new ActionExecuteException(e);
                }
            }
    );
    task.execute();    

## Example of usage

Get the document by id.

    public interface IGetDocumentMessage {
        CollectionName collectionName();
        string id();
        void document(IObject doc);
    };

    public class MessageHandler {

        void Handle(final IGetDocumentMessage mes) {
    
            IPool pool = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"));
            try (PoolGuard guard = new PoolGuard(pool)) {
                 
                ITask task = IOC.resolve(
                    Keys.getOrAdd("db.collection.getbyid"),
                    guard.getObject(),
                    mes.collectionName(),
                    mes.id(),
                    (doc) -> { mes.document(doc); }
            );
             
            task.execute();
            
        }
    }

Also see the sample [server implementation](http://smarttools.github.io/smartactors-core/xref/info/smart_tools/smartactors/core/examples/db_collection/package-frame.html) for details how IOC should be initialized for the tasks.
