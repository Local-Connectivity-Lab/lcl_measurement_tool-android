#Database

`Database` directory contains DB-related configuration and code. 

The project uses Android's Room DB as the underlying choice of database. `MeasurementResultDatabase` under the `DB` directory is a singleton used to connect to the databse and perform querying.

Current the database stores two entities: Signal Strength and Connectivity Measurement.
To query the data, use the respective `ViewModel` or the `AbstractViewModel` class which performs the data querying using the respective `data access model(DAO)`.
