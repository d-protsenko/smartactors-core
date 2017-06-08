# Create Postgres Collections Feature

This feature allows you to create collection in your database, even if the collection already exists in the DB.
_Core features do not have this IDatabaseTask._

This feature has custom
- `IDatabaseTask` to create collection if not exists
- `Actor` to create collection by a name.

To utilize this feature you can create a chain with the `CreateCollectionActor` inside.

Example:

```json
{
  "featureName": "create-tables",
  "afterFeatures": [
    "create-table-plugin"
  ],
  "maps": [
    {
      "externalAccess": false,
      "id": "CreateTables.InnerServerChain",
      "steps": [
        {
          "target": "CreateCollectionIfNotExists",
          "handler": "createTable",
          "wrapper": {
            "in_getCollectionName": "const/$collection_name"
          }
        }
      ]
    }
  ]
}
```

## IOC Keys

- `CreateCollectionIfNotExistsActor`
- `CreateCollectionIfNotExistsActorPlugin`
- `CreatePostgresCollectionIfNotExistsPlugin`
- `db.collection.create-if-not-exists` - it's the ITask
