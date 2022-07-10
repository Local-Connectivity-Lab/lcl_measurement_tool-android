# Models

`Models` directory contains boilerplate code for data models when communicating with the backend server.

Do not modify the code in this directory without first communicating with Esther Jang and Mark Theeranantachai 

## Sample Data Model in JSON

### SignalStrengthMessageModel

```json
{
  "dbm": -90,
  "level_code": 2,
  "latitude": 123.345,
  "longitude": 123.345,
  "timestamp": "2021-01-01T00:00:00Z",
  "cell_id": "CELL ID",
  "device_id": "DEVICE ID"
}
```

### RegistrationMessageModel

```json
{
  "sigma_r": "ABCDEFGHIJK",
  "h": "JKLMGDHSKHKD",
  "R": "BNMI76876HJHI"
}
```

### QRCodeKeysModel

```json
{
  "sigma_t": "HJKHEHKJHEIUNK",
  "sk_t": "YEYTDIONOO88904654HKIGF",
  "pk_a": "GJFYETRETDFUIHOJOB"
}
```

### MeasurementDataReportModel

```json
{
  "sigma_m": "JIUTIYDYDIYCOHININV",
  "h_pkr": "GWQWERTHBVCDRTYUIKJHJY738213",
  "M": "ECGUGNKNIOGYV",
  "show_data": true
}
```

### MeasurementDataModel
It is the base class for other data models
```json
{
  "latitude": 123.345,
  "longitude": 123.345,
  "timestamp": "2021-01-01T00:00:00Z",
  "cell_id": "CELL ID",
  "device_id": "DEVICE ID"
}
```

### ConnectivityMessageModel
```json
{
  "upload_speed": 12.3,
  "download_speed": 12.4,
  "ping": 34.5,
  "latitude": 123.345,
  "longitude": 123.345,
  "timestamp": "2021-01-01T00:00:00Z",
  "cell_id": "CELL ID",
  "device_id": "DEVICE ID"
}
```