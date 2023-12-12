
# Change Log
All notable changes to this project will be documented in this file.

## [1.3.7] - 2023-12-12
 
### Added
- Over-the-air firmware updates for Moby 5500.
### Changed

### Fixed
- Fixed issue for ACC on mastercard.

## [1.3.6] - 2023-11-02
 
### Added

### Changed
- Updated the Ingenico SDK to the latest version.
### Fixed
- Setup work-around for incorrect KSN by limiting it to the proper length.

## [1.3.5] - 2023-09-28
 
### Added

### Changed
- Made updates to sample app: Adding disconnect button, firmware check, and other UI updates.
- Updated MobyDevice to reset TransactionManager after disconnect.
### Fixed


## [1.3.4] - 2023-09-19
 
### Added

### Changed
- Updated provision loading to allow environment switching at runtime.
- Updated version of global payments java SDK.
### Fixed
- Fixed bug that caused an automatic reconnect when attempting to scan after disconnecting.

## [1.3.3] - 2023-08-29
 
### Added

### Changed
 
### Fixed
- Fixed maven issue with out-of-date dependency library.

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
