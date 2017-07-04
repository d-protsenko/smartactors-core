# PostgreSQL Connection Options Feature

This feature allows you to configure postgres connections. Inside this features we have an Actor and message chain.

To configure connection you should send special message to this core-chain.

Example.

Create your custom feature with single config.json file of this content:

```json
{
  "featureName": "com.my-project:create-collections",
  "afterFeatures": [
    "info.smart_tools.smartactors:create-postgres-collection-if-not-exists-feature"
  ],
  "onFeatureLoading": [
    {
      "chain": "registerPostgresConnectionOptions",
      "messages": [
        {
          "connectionOptionsRegistrationName": "MyConnectionOptions",
          "url": "jdbc:postgresql://localhost:5432/example",
          "username": "example",
          "password": "example",
          "maxConnections": 250
        }
      ]
    }, 
    
    
    {
      "example": "You can add more actions here. For example create collections. See CreateIfNotExists on CommonFeatures."
    },
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