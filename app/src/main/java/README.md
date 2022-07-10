# LCL Measurement Tool Main Codebase

## Main Structure
```
└── lclmeasurementtool
    ├── ConnectivityDataFragment.java
    ├── Constants
    │   ├── IperfConstants.java
    │   ├── NetworkConstants.java
    │   └── SimCardConstants.java
    ├── Database
    │   ├── DB
    │   │   └── MeasurementResultDatabase.java
    │   └── Entity
    │       ├── AbstractViewModel.java
    │       ├── Connectivity.java
    │       ├── ConnectivityDAO.java
    │       ├── ConnectivityViewModel.java
    │       ├── DataEncodable.java
    │       ├── EntityEnum.java
    │       ├── SignalStrength.java
    │       ├── SignalStrengthDAO.java
    │       └── SignalViewModel.java
    ├── Functionality
    │   ├── AbstractIperfWorker.java
    │   ├── AbstractPingWorker.java
    │   ├── Iperf3Callback.java
    │   ├── Iperf3Client.java
    │   ├── Iperf3Config.java
    │   ├── IperfDownStreamWorker.java
    │   ├── IperfUpStreamWorker.java
    │   ├── NetworkTestViewModel.java
    │   ├── Ping.java
    │   ├── PingError.java
    │   ├── PingListener.java
    │   ├── PingStats.java
    │   ├── PingUtils.java
    │   └── PingWorker.java
    ├── HomeFragment.java
    ├── MainActivity.java
    ├── Managers
    │   ├── CellularChangeListener.java
    │   ├── CellularManager.java
    │   ├── KeyStoreManager.java
    │   ├── LocationServiceListener.java
    │   ├── LocationServiceManager.java
    │   ├── LocationUpdatesListener.java
    │   ├── NetworkChangeListener.java
    │   ├── NetworkManager.java
    │   └── UploadManager.java
    ├── Models
    │   ├── ConnectivityMessageModel.java
    │   ├── MeasurementDataModel.java
    │   ├── MeasurementDataReportModel.java
    │   ├── QRCodeKeysModel.java
    │   ├── RegistrationMessageModel.java
    │   └── SignalStrengthMessageModel.java
    ├── Receivers
    │   └── SimStatesReceiver.java
    ├── SettingsFragment.java
    ├── SignalDataFragment.java
    └── Utils
        ├── AbstractDataTransferRate.java
        ├── AnalyticsUtils.java
        ├── ConvertUtils.java
        ├── DataTransferRateUnit.java
        ├── DecoderException.java
        ├── ECDSA.java
        ├── EncoderException.java
        ├── Hex.java
        ├── LocationUtils.java
        ├── SecurityUtils.java
        ├── SerializationUtils.java
        ├── SignalStrengthLevel.java
        ├── TimeUtils.java
        ├── UIUtils.java
        └── UnitUtils.java

```
`ConnectivityDataFragment.java`, `HomeFragment.java`, `MainActivity.java`, `SettingsFragment.java`, `SignalDataFragment.java`
are major UI views and controllers for the measurement app. In particular, `MainActivity.java` is the entry point of the app.

For more detailed info, check out the readme file in each sub-directories.