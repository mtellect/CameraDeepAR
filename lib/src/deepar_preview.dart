/*
 * *
 *  * Created by mapps on 12/8/21, 8:18 AM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 12/8/21, 8:18 AM
 *
 */

import 'package:camera_deep_ar/src/deepar_controller.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class DeepArPreview extends StatelessWidget {
  const DeepArPreview(this.controller, {Key? key}) : super(key: key);

  final CameraDeepArController controller;

  @override
  Widget build(BuildContext context) {
    // if (controller.value.isInitialized)
    return ValueListenableBuilder(
        valueListenable: controller,
        builder: (context, value, child) {
          final args = controller.value.toJson();

          if (defaultTargetPlatform == TargetPlatform.android) {
            return AndroidView(
                viewType: 'plugins.flutter.io/deep_ar_camera',
                onPlatformViewCreated: _onPlatformViewCreated,
                creationParams: args,
                creationParamsCodec: StandardMessageCodec());
          }
          return UiKitView(
              viewType: 'plugins.flutter.io/deep_ar_camera',
              onPlatformViewCreated: _onPlatformViewCreated,
              creationParams: args,
              creationParamsCodec: StandardMessageCodec());
        });
    return Container();
  }

  void _onPlatformViewCreated(int id) => controller.init(id);
}
