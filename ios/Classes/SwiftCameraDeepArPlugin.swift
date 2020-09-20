import Flutter
import UIKit
import DeepAR

public class SwiftCameraDeepArPlugin: NSObject, FlutterPlugin {
    public static func register(with registrar: FlutterPluginRegistrar) {
        //let channel = FlutterMethodChannel(name: "camera_deep_ar", binaryMessenger: registrar.messenger())
        //let instance = SwiftCameraDeepArPlugin()
        //registrar.addMethodCallDelegate(instance, channel: channel)
        let viewFactory = DeepArCameraViewFactory(messenger: registrar.messenger())
        registrar.register(viewFactory, withId: "plugins.flutter.io/deep_ar_camera")
    }
    
    //public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    //  result("iOS " + UIDevice.current.systemVersion)
    //}
}


//Factory view for camera ar
public class DeepArCameraViewFactory: NSObject, FlutterPlatformViewFactory {
    let messenger: FlutterBinaryMessenger
    
    init(messenger: FlutterBinaryMessenger) {
        self.messenger = messenger
    }
    public func create(withFrame frame: CGRect,
                       viewIdentifier viewId: Int64,
                       arguments args: Any?) -> FlutterPlatformView {
        return DeepArCameraView(messenger: messenger,
                                frame: frame, viewId: viewId,
                                args: args)
    }
    public func createArgsCodec() -> FlutterMessageCodec & NSObjectProtocol {
        return FlutterStandardMessageCodec.sharedInstance()
    }
}



//The main view for DeepAr Camera
public class DeepArCameraView : NSObject,FlutterPlatformView,DeepARDelegate{
    
    let messenger: FlutterBinaryMessenger
    let frame: CGRect
    let viewId: Int64
    let channel: FlutterMethodChannel
    
    
    @IBOutlet weak var arViewContainer: UIView!
    
    private var deepAR: DeepAR!
    private var arView: ARView!
    
    
    // This class handles camera interaction. Start/stop feed, check permissions etc. You can use it or you
    // can provide your own implementation
    private var cameraController: CameraController!
    
    
    
    init(messenger: FlutterBinaryMessenger, frame: CGRect, viewId: Int64, args: Any?){
        self.messenger=messenger
        self.frame=frame
        self.viewId=viewId
        deepAR = DeepAR()

        channel = FlutterMethodChannel(name: "plugins.flutter.io/deep_ar_camera/\(viewId)", binaryMessenger: messenger)
        super.init()

        channel.setMethodCallHandler { call, result in
            if call.method == "initialize" {
               if let dict = call.arguments as? [String: Any] {
                                   if let licenceKey = (dict["licenceKey"] as? String) {
                                    result("iOS /\(String(describing: licenceKey))" + UIDevice.current.systemVersion)
                                   }
                               }
                result("iOS Initialized" + UIDevice.current.systemVersion)
            } else if call.method == "captureImage" {
                result("captureImage")
            } else if call.method == "setPreviewRatio" {
                result("setPreviewRatio")
            } else if call.method == "switchCamera" {
                result("switchCamera")
            } else if call.method == "setFlashType" {
                result("setFlashType")
            } else if call.method == "setSessionPreset" {
                result("setSessionPreset")
            }
        }
        
        
        
    }
    
    func receiveFromFlutter(text: String) {
        
    }
    
    
    public func sendFromNative(_ text: String) {
        channel.invokeMethod("sendFromNative", arguments: text)
    }
    
    
    public func view() -> UIView {
        let uiWindow = UIWindow(frame: frame)
        uiWindow.backgroundColor = .yellow
        uiWindow.addSubview(UISlider(frame: frame))
        
        //let slider = UISlider(frame: frame)
        //let button = UIButton(frame: frame)
        
        let button   = UIButton(type: UIButton.ButtonType.system) as UIButton
        // set the frame
        button.frame = CGRect(x: 100, y: 100, width: 100, height: 50)
        
        // add image
        //        button.setBackgroundImage(UIImage(named:"SearchIcon"), for: .normal)
        // button title
        button.setTitle("Test Button", for: .normal)
        // add action
        button.addTarget(self, action: #selector(didTapOnTakePhotoButton), for: UIControl.Event.touchUpInside)
        return button;
    }
    
    
    @objc func didTapOnTakePhotoButton()  {
        
    }
    
    @objc func  initCameraDeepAR(key: String){
        self.deepAR = DeepAR()
        self.deepAR.delegate = self
        let licenceKey="33a61ae2b83fa46a8bd068cbb41f03afaf9eda361c7a3315ae7ca47878f7aadeb17829dc9a11060d"
        self.deepAR.setLicenseKey(licenceKey)
        
        cameraController = CameraController()
        cameraController.deepAR = self.deepAR
        
        self.arView = self.deepAR.createARView(withFrame: self.arViewContainer.frame) as? ARView
        self.arView.translatesAutoresizingMaskIntoConstraints = false
        self.arViewContainer.addSubview(self.arView)
        if #available(iOS 9.0, *) {
            self.arView.leftAnchor.constraint(equalTo: self.arViewContainer.leftAnchor, constant: 0).isActive = true
            self.arView.rightAnchor.constraint(equalTo: self.arViewContainer.rightAnchor, constant: 0).isActive = true
            self.arView.topAnchor.constraint(equalTo: self.arViewContainer.topAnchor, constant: 0).isActive = true
            self.arView.bottomAnchor.constraint(equalTo: self.arViewContainer.bottomAnchor, constant: 0).isActive = true
        } else {
            // Fallback on earlier versions
        }
        
        cameraController.startCamera()
    }
    
    
    
    
}



