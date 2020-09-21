import 'package:camera_deep_ar/camera_deep_ar.dart';
import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  CameraDeepArController cameraDeepArController;

  @override
  void initState() {
    super.initState();
    //initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  // Future<void> initPlatformState() async {
  //   String platformVersion;
  //   // Platform messages may fail, so we use a try/catch PlatformException.
  //   try {
  //     platformVersion = await CameraDeepAr.platformVersion;
  //   } on PlatformException {
  //     platformVersion = 'Failed to get platform version.';
  //   }
  //
  //   // If the widget was removed from the tree while the asynchronous platform
  //   // message was in flight, we want to discard the reply rather than calling
  //   // setState to update our non-existent appearance.
  //   if (!mounted) return;
  //
  //   setState(() {
  //     _platformVersion = platformVersion;
  //   });
  // }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Stack(
          children: [
            CameraDeepAr(
                androidLicenceKey:
                    "498eb09a2f04b2d44fa4e60c069ee6814e77c8eb972d3c5e14340d53f064637f8bb14452c0634e05",
                iosLicenceKey:
                    "53618212114fc16bbd7499c0c04c2ca11a4eed188dc20ed62a7f7eec02b41cb34d638e72945a6bf6",
                cameraDeepArCallback: (c) async {
                  cameraDeepArController = c;
                  _platformVersion = await c.isCameraReady();
                  setState(() {});
                }),
            Align(
              alignment: Alignment.bottomCenter,
              child: Container(
                padding: EdgeInsets.all(20),
                //height: 250,
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    Text(
                      'Response >>> : $_platformVersion\n',
                      textAlign: TextAlign.center,
                      style: TextStyle(fontSize: 14, color: Colors.white),
                    ),
                    SizedBox(
                      height: 20,
                    ),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        RaisedButton(
                          onPressed: () async {
                            if (cameraDeepArController == null) return;
                            _platformVersion = await cameraDeepArController
                                .previous(licenceKey: 'null');
                            setState(() {});
                          },
                          shape: CircleBorder(),
                          padding: EdgeInsets.all(20),
                          color: Colors.white,
                          child: Icon(Icons.navigate_before),
                        ),
                        RaisedButton(
                          onPressed: () async {
                            if (cameraDeepArController == null) return;
                            _platformVersion = await cameraDeepArController
                                .switchCamera(licenceKey: 'null');
                            setState(() {});
                          },
                          shape: CircleBorder(),
                          padding: EdgeInsets.all(20),
                          color: Colors.white,
                          child: Icon(Icons.switch_camera_outlined),
                        ),
                        RaisedButton(
                          onPressed: () async {
                            if (cameraDeepArController == null) return;
                            _platformVersion = await cameraDeepArController
                                .next(licenceKey: 'null');
                            setState(() {});
                          },
                          shape: CircleBorder(),
                          padding: EdgeInsets.all(20),
                          color: Colors.white,
                          child: Icon(Icons.navigate_next),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            )
          ],
        ),
      ),
    );
  }
}
