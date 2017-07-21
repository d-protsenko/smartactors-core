# Postgres database configuration

Example of configuration:

``` JavaScript
{
  "database": [
    {
      // IOC key for this database connection settings
      "key": "PostgresConnectionOptions",
      // Name of strategy for this type of database
      "type": "PostgresConnectionOptionsStrategy",
      // Database connection options
      "config": {
        "url": "jdbc:postgresql://localhost:5432/vp_actors",
        "username": "username",
        "password": "password",
        "maxConnections": 20
      }
    }
  ]
}
```