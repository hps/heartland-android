
# Change Log
All notable changes to this project will be documented in this file.

## [1.3.1] - 2023-07-18
 
### Added

- C2XDevice now also supports C3X devices.
- Remote Key Injection added for C2X and C3X devices.

### Changed
 
### Fixed
- Fixed issue with OTA config updates performing firmware update instead.
 
## [1.3.0] - 2023-06-15
 
### Added

- Moby 5500 support was added to the SDK. Use the MobyDevice class to interact with Moby 5500 devices.
- The onDisconnected() function was added to the DeviceListener interface.
- Added ErrorType variable to the onError() callbacks for DeviceListener and TransactionListener.

### Changed
 
### Fixed
