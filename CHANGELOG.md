
# Change Log
All notable changes to this project will be documented in this file.

## [1.4.2] - 2024-10-24
 
### Added

### Changed
- Updated the onError callback for reader timeout to have the value of "ReaderTimeout" for error.getMessage().
### Fixed
- Fixed intermittent exception caused by a bad PAN value for fleet check.


## [1.4.1] - 2024-10-15
 
### Added

### Changed

### Fixed
- Fixed logic for uploadSAF so that SafListener.onError is called when there is still no connection.


## [1.4.0] - 2024-09-17
 
### Added
- Added surcharge functionality which can be enabled/disabled with ConnectionConfig.setSurchargeEnabled(boolean).
### Changed
- Changed the return value for onCardholderInteractionRequested from void to boolean. You should return true if the interaction was handled and false if it wasn't.
### Fixed


## [1.3.17] - 2024-07-25
 
### Added
- Updated transactions to autofill the UniqueDeviceId header value with the last 4 digits of the connected terminal.
### Changed

### Fixed


## [1.3.16] - 2024-07-02
 
### Added

### Changed
- Updated gradle plugin version to resolve macroscope issues.
### Fixed
- Fixed issue with void and refund transactions caused by previous update for client Txn ID (1.3.14).


## [1.3.15] - 2024-06-25
 
### Added
- Added terminal status indicator to TerminalResponse.
### Changed
- Removed unused code that was giving warnings in macroscope.
### Fixed
- Fixed NullPointerException that occurred when attempting to uploadSAF.


## [1.3.14] - 2024-04-23
 
### Added
- Added support for cardBrandTxnId which is used for multi-use token requests.
### Changed

### Fixed
- Fixed issue with clientTxnId not being included in requests for multiple transaction types.


## [1.3.13] - 2024-03-21
 
### Added
- Added support for requesting a multi-use token to CreditSaleBuilder and also added support for using a multi-use token.
### Changed

### Fixed


## [1.3.12] - 2024-03-07
 
### Added
- Added config/provision over-the-air updating for Moby 5500.
### Changed

### Fixed


## [1.3.11] - 2024-02-22
 
### Added

### Changed

### Fixed
- Fixed issue for C2X/C3X which caused the previous device to connect instead of the newly selected device.

## [1.3.10] - 2024-02-08
 
### Added

### Changed

### Fixed
- Updated bbpos library to resolve connection issue for C3X.
- Fixed issue with onProgress callback for Moby firmware updates.

## [1.3.9] - 2024-01-16
 
### Added
- Store and forward (SAF) support added for C2X, C3X, and Moby 5500 devices.
### Changed

### Fixed
- Fixed issue with bad state caused by cancel call when no transaction is running.

## [1.3.8] - 2024-01-04
 
### Added

### Changed

### Fixed
- Added missing apostrophe for TerminalResponse toString().
- Fixed crash on transaction caused by config parsing changes introduced in 1.3.6.

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
