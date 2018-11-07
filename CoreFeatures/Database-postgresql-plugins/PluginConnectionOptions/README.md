# Postgres database configuration

Example of configuration:

``` json
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

You should define the configuration in the feature that use this configuration or feature that loading before.
For Postgresql database field "type" should have value "PostgresConnectionOptionsStrategy". User can define several 
configurations and resolve them using IOC, for example:

    Object connectionOptions = IOC.resolve(Keys.resolveByName("PostgresConnectionOptions"));
 