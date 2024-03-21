
# Heartland Mobile Android SDK 
=======================

This Android SDK lets you connect to a C2X, C3X, or Moby 5500 device and process credit card payments. Included is an example application which shows the basics of using the SDK.

Maven
-------------
```java
implementation 'com.heartlandpaymentsystems:heartland-android-sdk:1.3.13'
```

Example App
-------------
To use the example app, you'll need to find these variables inside MainActivity:
```java
public static final String PUBLIC_KEY = "YOUR PUBLIC KEY HERE";
public static final String USERNAME = "YOUR USERNAME HERE";
public static final String PASSWORD = "YOUR PASSWORD HERE";
public static final String SITE_ID = "YOUR SITE ID HERE";
public static final String DEVICE_ID = "YOUR DEVICE ID HERE";
public static final String LICENSE_ID = "YOUR LICENSE ID HERE";
```
Simply update the placeholder values with your own credentials and then run the application. The example app will allow you to scan and connect to your C2X or Moby 5500 device, run manual entry transactions, and run card read transactions (using connected C2X or Moby 5500).

SDK Classes
-------------

- **C2XDevice** - This is the class used for scanning and connecting to C2X devices. It also used for transactions once connected.
- **MobyDevice** - This is the class used for scanning and connecting to Moby 5500 devices. It is also used for transactions once connected.
- **ConnectionConfig** - Used for initializing the C2XDevice or MobyDevice object with your credentials.
- **DeviceListener** - Listener interface for scanning/connecting callbacks.
- **TransactionListener** - Listener interface for transaction callbacks.
- **Card** - Used for manual card entry (C2X/Moby connection is not required for manual card entry).
- **CreditAuthBuilder**, **CreditCaptureBuilder**, **CreditSaleBuilder**, **CreditAdjustBuilder**, **CreditReturnBuilder**, **CreditVoidBuilder** - Builder classes for constructing different types of transactions.
