# FallWatch
Is an appication designed for people, who might have in their daily lifes a risk of falling down and hurting themselves. FallWatch uses devices internal acceleration sensor to detect sudden changes in your movement. Optionally user can use external MetaMotionC sensor instead. In case of collision, application will send an alert to contacts defined by user. Application is for Android platform.

## Requirements
- Minimum Android version: Android 5.0 API 21
- Targeted for: Android 7.1.1 API 25

## Optional
- MbientLab MetaMotionC external sensor.

## Installation
1. Clone the repository:
```git clone https://github.com/jamiamikko/FallWatch.git```

2. Open project in Android Studio

  * Download Android Studio: [https://developer.android.com/studio/index.html](https://developer.android.com/studio/index.html)

3. Configure your MetaMotionC external sensor (optional)

  * Download and install MetaWear app from Google Play: [https://play.google.com/store/apps/details?id=com.mbientlab.metawear.app](https://play.google.com/store/apps/details?id=com.mbientlab.metawear.app)

  * Connect to your MetaMotionC external sensor and check devices MAC address.

  * In Android Studio open class `ExternalDetectionClient` and find line: `private final String mwMacAddress = ...;` 

  * Insert your sensors MAC address as value for `mwMacAddress`.

5. Run Application

  * Attach your Android device to your computer. In Android Studio, click `Run (app)` in the toolbar. Select your device and click OK.
  * Now you're all set. Thank you for downloading our app!