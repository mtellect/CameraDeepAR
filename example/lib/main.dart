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
                onImageCaptured: (path) {},
                onVideoRecorded: (path) {},
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
                    Container(
                        height: 150,
                        padding: EdgeInsets.all(15),
                        child: PageView.builder(
                            scrollDirection: Axis.horizontal,
                            itemCount: Masks.values.length,
                            controller: vp,
                            onPageChanged: (p) {
                              currentPage = p;
                              cameraDeepArController.changeMask(p);
                              setState(() {});
                            },
                            itemBuilder: (ctx, p) {
                              bool active = currentPage == p;

                              return Container(
                                  margin: EdgeInsets.all(5),
                                  padding: EdgeInsets.all(12),
                                  width: active ? 120 : 100,
                                  height: active ? 120 : 100,
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
                                  ));
                            })),
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
