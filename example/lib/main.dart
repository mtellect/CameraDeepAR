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
  int currentPage = 0;
  final vp = PageController(viewportFraction: .24);
  Effects currentEffect = Effects.none;
  Filters currentFilter = Filters.none;
  Masks currentMask = Masks.none;
  bool isRecording = false;

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        backgroundColor: Colors.black,
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Stack(
          children: [
            CameraDeepAr(
                onCameraReady: (isReady) {
                  _platformVersion = "Camera status $isReady";
                  setState(() {});
                },
                onImageCaptured: (path) {
                  _platformVersion = "Image Taken @ $path";
                  setState(() {});
                },
                onVideoRecorded: (path) {
                  _platformVersion = "Video Recorded @ $path";
                  isRecording = false;
                  setState(() {});
                },
                androidLicenceKey:
                    "3b58c448bd650192e7c53d965cfe5dc1c341d2568b663a3962b7517c4ac6eeed0ba1fb2afe491a4b",
                iosLicenceKey:
                    "53618212114fc16bbd7499c0c04c2ca11a4eed188dc20ed62a7f7eec02b41cb34d638e72945a6bf6",
                cameraDeepArCallback: (c) async {
                  cameraDeepArController = c;
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
                      children: [
                        Expanded(
                          child: FlatButton(
                            onPressed: () {
                              if (null == cameraDeepArController) return;
                              if (isRecording) return;
                              cameraDeepArController.snapPhoto();
                            },
                            child: Icon(Icons.camera_enhance_outlined),
                            color: Colors.white,
                            padding: EdgeInsets.all(15),
                          ),
                        ),
                        if (isRecording)
                          Expanded(
                            child: FlatButton(
                              onPressed: () {
                                if (null == cameraDeepArController) return;
                                cameraDeepArController.stopVideoRecording();
                                isRecording = false;
                                setState(() {});
                              },
                              child: Icon(Icons.videocam_off),
                              color: Colors.red,
                              padding: EdgeInsets.all(15),
                            ),
                          )
                        else
                          Expanded(
                            child: FlatButton(
                              onPressed: () {
                                if (null == cameraDeepArController) return;
                                cameraDeepArController.startVideoRecording();
                                isRecording = true;
                                setState(() {});
                              },
                              child: Icon(Icons.videocam),
                              color: Colors.green,
                              padding: EdgeInsets.all(15),
                            ),
                          ),
                      ],
                    ),
                    SingleChildScrollView(
                      padding: EdgeInsets.all(15),
                      scrollDirection: Axis.horizontal,
                      child: Row(
                        children: List.generate(Masks.values.length, (p) {
                          bool active = currentPage == p;
                          return GestureDetector(
                            onTap: () {
                              currentPage = p;
                              cameraDeepArController.changeMask(p);
                              setState(() {});
                            },
                            child: Container(
                                margin: EdgeInsets.all(5),
                                padding: EdgeInsets.all(12),
                                width: active ? 100 : 80,
                                height: active ? 100 : 80,
                                alignment: Alignment.center,
                                decoration: BoxDecoration(
                                    color:
                                        active ? Colors.orange : Colors.white,
                                    shape: BoxShape.circle),
                                child: Text(
                                  "$p",
                                  textAlign: TextAlign.center,
                                  style: TextStyle(
                                      fontSize: active ? 16 : 14,
                                      color: Colors.black),
                                )),
                          );
                        }),
                      ),
                    )
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
