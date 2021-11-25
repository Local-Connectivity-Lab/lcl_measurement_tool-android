# LCL Network Performance Measurement Tool

LCL Network Measurement Tool is an Android App that measures a variety of network metrics, including but not limited to *ping*, *upload/download* speed, *signal strength*.

## HTTP Schema:
-  `signalStrength`
    - latitude: string
    - longitude: string
    - timestamp: string
    - dBm: int
    - level_code : int
    - cell_id: ""
    - device_id: UUID string
- `connectivity`
    - latitude: string
    - longitude: string
    - timestamp: string
    - upload_speed: double
    - download_speed: double
    - ping: double
    - cell_id: ""
    - device_id: UUID string
