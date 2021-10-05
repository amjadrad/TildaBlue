# TildaBlue
Easy Connect to Bluetooth Devices


**How To Use.**

1. Add it in your root build.gradle at the end of repositories:

```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

2. Add the dependency

```
dependencies {
	       	        implementation 'com.github.amjadrad:TildaBlue:1.1'
}
```

3. Add this permissions
```
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```


4. IF API>23
```
if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
} 
```


5. Use in code
```
TildaBlueUtils tildaBlueUtils = new TildaBlueUtils(YourActivity.this);
```

Functions:
```
isEnable(); // check if bluetooth is turned on.

enable(); //Turn bluetooth on

disable(); //Turn bluetooth off

getBondedDevices(); //Get list of bonded devices.

search(int secondsToScan, OnSearchDeviceListener onSearchDeviceListener); // search for new devices for seconds and listen to result.

connect(String macAddress, OnMessageReceiveListener onMessageReceiveListener); // Connect to a device by ***MAC Address*** and set a listener to receive data.

sendData(String data); // Send your data as string to connected device.


```
