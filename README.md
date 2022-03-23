
# Heartland Mobile Android SDK 
=======================

This Android SDK lets you connect to a C2X device and process credit card payments. Included is an example application which shows the basics of using the SDK.

Example App
-------------
To use the example app, you'll need to find these variables inside MainActivity:
```java
public static final String PUBLIC_KEY = "YOUR PUBLIC KEY HERE";
private static final String USERNAME = "YOUR USERNAME HERE";
private static final String PASSWORD = "YOUR PASSWORD HERE";
private static final String SITE_ID = "YOUR SITE ID HERE";
private static final String DEVICE_ID = "YOUR DEVICE ID HERE";
private static final String LICENSE_ID = "YOUR LICENSE ID HERE";
```
Simply update the placeholder values with your own credentials and then run the application. The example app will allow you to scan and connect to your C2X device, run manual entry transactions, and run card read transactions (using connected C2X).

SDK Classes
-------------

**C2XDevice** - This is the class used for scanning and connecting to C2X devices. It also used for transactions once connected.
**ConnectionConfig** - Used for initializing the C2XDevice object with your credentials.
**DeviceListener** - Listener interface for scanning/connecting callbacks.
**TransactionListener** - Listener interface for transaction callbacks.
**Card** - Used for manual card entry (C2X connection is not required for manual card entry).
**CreditAuthBuilder**, **CreditCaptureBuilder**, **CreditSaleBuilder**, **CreditAdjustBuilder**, **CreditReturnBuilder**, **CreditVoidBuilder** - Builder classes for constructing different types of transactions.
