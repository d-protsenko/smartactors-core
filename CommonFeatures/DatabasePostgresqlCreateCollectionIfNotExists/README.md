# Create Postgres Collections Feature

This feature allows you to create collection in your database, even if the collection already exists in the DB.
_Core features do not have this IDatabaseTask._

This feature has custom
- `IDatabaseTask` to create collection if not exists
- `Actor` to create collection by a name.

To utilize this feature you can just add `onFeatureLoading` section in your feature, where a message will be sent to create a collection. 

Example:

```json
{
  "featureName": "com.my-project:create-collections",
  "afterFeatures": [
    "info.smart_tools.smartactors:database-postgresql-create-collection-if-not-exists"
  ],
  "onFeatureLoading": [
    {
      "chain": "createCollections",
      "messages": [
        {
          "collectionName": "example_collection",
          "connectionOptionsRegistrationName": "MyConnectionOptions"
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
