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


enum Mode: String {
    case masks
    case effects
    case filters
}

enum RecordingMode : String {
    case photo
    case video
    case lowQualityVideo
}

enum Masks: String, CaseIterable {
    case none
    case aviators
    case bigmouth
    case dalmatian
    case bcgSeg
    case look2
    case fatify
    case flowers
    case grumpycat
    case koala
    case lion
    case mudMask
    case obama
    case pug
    case slash
    case sleepingmask
    case smallface
    case teddycigar
    case tripleface
    case twistedFace
}

enum Effects: String, CaseIterable {
    case none
    case fire
    case heart
    case blizzard
    case rain
}

enum Filters: String, CaseIterable {
    case none
    case tv80
    case drawingmanga
    case sepia
    case bleachbypass
    case realvhs
    case filmcolorperfection
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
    var licenceKey: String
    
    
    // MARK: - IBOutlets -
    
    @IBOutlet weak var switchCameraButton: UIButton!
    
    @IBOutlet weak var masksButton: UIButton!
    @IBOutlet weak var effectsButton: UIButton!
    @IBOutlet weak var filtersButton: UIButton!
    
    @IBOutlet weak var previousButton: UIButton!
    @IBOutlet weak var nextButton: UIButton!
    @IBOutlet weak var recordActionButton: UIButton!
    
    @IBOutlet weak var lowQVideoButton: UIButton!
    @IBOutlet weak var videoButton: UIButton!
    @IBOutlet weak var photoButton: UIButton!
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
        licenceKey="";
        channel = FlutterMethodChannel(name: "plugins.flutter.io/deep_ar_camera/\(viewId)", binaryMessenger: messenger)
        super.init()
        
        NotificationCenter.default.addObserver(self, selector: #selector(orientationDidChange), name:  Notification.Name("UIDeviceOrientationDidChangeNotification"), object: nil)
        
        channel.setMethodCallHandler { call, result in
            if call.method == "initialize" {
                if let dict = call.arguments as? [String: Any] {
                    if let licenceKey = (dict["licenceKey"] as? String) {
                        self.licenceKey=licenceKey;
                        //self.initCameraDeepAR(key: licenceKey)
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
        if #available(iOS 9.0, *) {
            self.initCameraDeepAR(key: licenceKey)
        } else {
            // Fallback on earlier versions
        }
        
        
    }
    
    
    @objc func orientationDidChange() {
        //        @available(iOS 13.0, *)
        if #available(iOS 13.0, *) {
            guard let orientation = UIApplication.shared.windows.first?.windowScene?.interfaceOrientation else { return }
            switch orientation {
            case .landscapeLeft:
                cameraController.videoOrientation = .landscapeLeft
                break
            case .landscapeRight:
                cameraController.videoOrientation = .landscapeRight
                break
            case .portrait:
                cameraController.videoOrientation = .portrait
                break
            case .portraitUpsideDown:
                cameraController.videoOrientation = .portraitUpsideDown
            default:
                break
            }
        } else {
            // Fallback on earlier versions
        }
        
        
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
        return arView;
    }
    
    
    @objc func didTapOnTakePhotoButton()  {
        
    }
    
    // MARK: - Private properties -
    
    //    private var maskIndex: Int = 0
    //    private var maskPaths: [String?] {
    //        return Masks.allCases.map { $0.String.rawValue.path }
    //    }
    //
    //    private var effectIndex: Int = 0
    //    private var effectPaths: [String?] {
    //        return Effects.allCases.map { $0.rawValue.path }
    //    }
    //
    //    private var filterIndex: Int = 0
    //    private var filterPaths: [String?] {
    //        return Filters.allCases.map { $0.rawValue.path }
    //    }
    //
    //    private var buttonModePairs: [(UIButton, Mode)] = []
    //    private var currentMode: Mode! {
    //        didSet {
    //            updateModeAppearance()
    //        }
    //    }
    //
    //    private var buttonRecordingModePairs: [(UIButton, RecordingMode)] = []
    //    private var currentRecordingMode: RecordingMode! {
    //        didSet {
    //            updateRecordingModeAppearance()
    //        }
    //    }
    
    private var isRecordingInProcess: Bool = false
    
    
    @available(iOS 9.0, *)
    @objc func  initCameraDeepAR(key: String){
        
         self.deepAR = DeepAR()
         self.deepAR.delegate = self
         var licenceKey="53618212114fc16bbd7499c0c04c2ca11a4eed188dc20ed62a7f7eec02b41cb34d638e72945a6bf6"
         self.deepAR.setLicenseKey(licenceKey)
         
         cameraController = CameraController()
         cameraController.deepAR = self.deepAR
         
        self.arView = self.deepAR.createARView(withFrame: self.frame) as? ARView
         //self.arView.translatesAutoresizingMaskIntoConstraints = false
//         self.arViewContainer.addSubview(self.arView)
//         self.arView.leftAnchor.constraint(equalTo: self.arViewContainer.leftAnchor, constant: 0).isActive = true
//         self.arView.rightAnchor.constraint(equalTo: self.arViewContainer.rightAnchor, constant: 0).isActive = true
//         self.arView.topAnchor.constraint(equalTo: self.arViewContainer.topAnchor, constant: 0).isActive = true
//         self.arView.bottomAnchor.constraint(equalTo: self.arViewContainer.bottomAnchor, constant: 0).isActive = true
         
         cameraController.startCamera()
    }
    
    
    
    
}



