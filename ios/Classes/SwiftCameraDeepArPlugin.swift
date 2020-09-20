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
    private var arViewContainer: UIView!
    
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
        
        self.arViewContainer = UIView(frame: frame)
        //self.arViewContainer.isOpaque=false
        self.arViewContainer.backgroundColor = .clear
        //self.arViewContainer.backgroundColor = .yellow
        licenceKey="";
        channel = FlutterMethodChannel(name: "plugins.flutter.io/deep_ar_camera/\(viewId)", binaryMessenger: messenger)
        super.init()
        
        NotificationCenter.default.addObserver(self, selector: #selector(orientationDidChange), name:  Notification.Name("UIDeviceOrientationDidChangeNotification"), object: nil)
        
        
        if let dict = args as? [String: Any] {
            let licence: String = (dict["iosLicenceKey"] as? String)!
            self.licenceKey = licence
        }
        
        
        channel.setMethodCallHandler { call, result in
            if call.method == "isCameraReady" {
//                if let dict = call.arguments as? [String: Any] {
//                    if let licenceKey = (dict["licenceKey"] as? String) {
//                        self.licenceKey=licenceKey;
//                        result("iOS /\(String(describing: licenceKey))" + UIDevice.current.systemVersion)
//                    }
//                }
                result("iOS /\(String(describing: self.licenceKey))" + UIDevice.current.systemVersion)

            } else if call.method == "next" {
                result("captureImage")
            } else if call.method == "previous" {
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
            self.initCameraDeepAR()
            //self.addTargets()
        } else {
            // Fallback on earlier versions
        }
//        buttonModePairs = [(masksButton, .masks), (effectsButton, .effects), (filtersButton, .filters)]
//        buttonRecordingModePairs = [ (photoButton, RecordingMode.photo), (videoButton, RecordingMode.video), (lowQVideoButton, RecordingMode.lowQualityVideo)]
//        currentMode = .masks
//        currentRecordingMode = .photo
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
        let button   = UIButton(type: UIButton.ButtonType.system) as UIButton
        button.frame = CGRect(x: 100, y: 100, width: 100, height: 50)
        button.setTitle("Test Button", for: .normal)
        button.addTarget(self, action: #selector(didTapOnTakePhotoButton), for: UIControl.Event.touchUpInside)
        return arView;
    }
    
    
    @objc func didTapOnTakePhotoButton()  {
        
    }
    
    // MARK: - Private properties -
    
    private var maskIndex: Int = 0
    private var maskPaths: [String?] {
        return Masks.allCases.map { $0.rawValue.path }
    }
    //
    private var effectIndex: Int = 0
    private var effectPaths: [String?] {
        return Effects.allCases.map { $0.rawValue.path }
    }
    //
    private var filterIndex: Int = 0
    private var filterPaths: [String?] {
        return Filters.allCases.map { $0.rawValue.path }
    }
    //
    private var buttonModePairs: [(UIButton, Mode)] = []
    private var currentMode: Mode! {
        didSet {
            updateModeAppearance()
        }
    }
    //
    private var buttonRecordingModePairs: [(UIButton, RecordingMode)] = []
    private var currentRecordingMode: RecordingMode! {
        didSet {
            updateRecordingModeAppearance()
        }
    }
    
    
    private func updateModeAppearance() {
        buttonModePairs.forEach { (button, mode) in
            button.isSelected = mode == currentMode
        }
    }
    
    private func updateRecordingModeAppearance() {
        buttonRecordingModePairs.forEach { (button, recordingMode) in
            button.isSelected = recordingMode == currentRecordingMode
        }
    }
    
    private func switchMode(_ path: String?) {
        deepAR.switchEffect(withSlot: currentMode.rawValue, path: path)
    }
    
    
    private var isRecordingInProcess: Bool = false
    
    
    @available(iOS 9.0, *)
    @objc func  initCameraDeepAR(){
        
        self.deepAR = DeepAR()
        self.deepAR.delegate = self
        //var licenceKey="53618212114fc16bbd7499c0c04c2ca11a4eed188dc20ed62a7f7eec02b41cb34d638e72945a6bf6"
        self.deepAR.setLicenseKey(self.licenceKey)
        cameraController = CameraController()
        cameraController.deepAR = self.deepAR
        
        
        self.arView = self.deepAR.createARView(withFrame: self.frame) as? ARView
        self.arView.translatesAutoresizingMaskIntoConstraints = false
        //         self.arViewContainer.addSubview(self.arView)
        //         self.arView.leftAnchor.constraint(equalTo: self.arViewContainer.leftAnchor, constant: 0).isActive = true
        //         self.arView.rightAnchor.constraint(equalTo: self.arViewContainer.rightAnchor, constant: 0).isActive = true
        //         self.arView.topAnchor.constraint(equalTo: self.arViewContainer.topAnchor, constant: 0).isActive = true
        //         self.arView.bottomAnchor.constraint(equalTo: self.arViewContainer.bottomAnchor, constant: 0).isActive = true
        
        cameraController.startCamera()
    }
    
    @objc func addTargets() {
        switchCameraButton.addTarget(self, action: #selector(didTapSwitchCameraButton), for: .touchUpInside)
        recordActionButton.addTarget(self, action: #selector(didTapRecordActionButton), for: .touchUpInside)
        previousButton.addTarget(self, action: #selector(didTapPreviousButton), for: .touchUpInside)
        nextButton.addTarget(self, action: #selector(didTapNextButton), for: .touchUpInside)
        masksButton.addTarget(self, action: #selector(didTapMasksButton), for: .touchUpInside)
        effectsButton.addTarget(self, action: #selector(didTapEffectsButton), for: .touchUpInside)
        filtersButton.addTarget(self, action: #selector(didTapFiltersButton), for: .touchUpInside)
        
        
        photoButton.addTarget(self, action: #selector(didTapPhotoButton), for: .touchUpInside)
        videoButton.addTarget(self, action: #selector(didTapVideoButton), for: .touchUpInside)
        lowQVideoButton.addTarget(self, action: #selector(didTapLowQVideoButton), for: .touchUpInside)
    }
    
    @objc
    private func didTapSwitchCameraButton() {
        cameraController.position = cameraController.position == .back ? .front : .back
    }
    
    @objc
    private func didTapRecordActionButton() {
        //
        
        if (currentRecordingMode == RecordingMode.photo) {
            deepAR.takeScreenshot()
            return
        }
        
        if (isRecordingInProcess) {
            deepAR.finishVideoRecording()
            isRecordingInProcess = false
            return
        }
        
        let width: Int32 = Int32(deepAR.renderingResolution.width)
        let height: Int32 =  Int32(deepAR.renderingResolution.height)
        
        if (currentRecordingMode == RecordingMode.video) {
            deepAR.startVideoRecording(withOutputWidth: width, outputHeight: height)
            isRecordingInProcess = true
            return
        }
        
        if (currentRecordingMode == RecordingMode.lowQualityVideo) {
            let videoQuality = 0.1
            let bitrate =  1250000
            let videoSettings:[AnyHashable : AnyObject] = [
                AVVideoQualityKey : (videoQuality as AnyObject),
                AVVideoAverageBitRateKey : (bitrate as AnyObject)
            ]
            
            let frame = CGRect(x: 0, y: 0, width: 1, height: 1)
            
            deepAR.startVideoRecording(withOutputWidth: width, outputHeight: height, subframe: frame, videoCompressionProperties: videoSettings, recordAudio: true)
            isRecordingInProcess = true
        }
        
    }
    
    @objc
    private func didTapPreviousButton() {
        var path: String?
        
        switch currentMode! {
        case .effects:
            effectIndex = (effectIndex - 1 < 0) ? (effectPaths.count - 1) : (effectIndex - 1)
            path = effectPaths[effectIndex]
        case .masks:
            maskIndex = (maskIndex - 1 < 0) ? (maskPaths.count - 1) : (maskIndex - 1)
            path = maskPaths[maskIndex]
        case .filters:
            filterIndex = (filterIndex - 1 < 0) ? (filterPaths.count - 1) : (filterIndex - 1)
            path = filterPaths[filterIndex]
        }
        
        switchMode(path)
    }
    
    @objc
    private func didTapNextButton() {
        var path: String?
        
        switch currentMode! {
        case .effects:
            effectIndex = (effectIndex + 1 > effectPaths.count - 1) ? 0 : (effectIndex + 1)
            path = effectPaths[effectIndex]
        case .masks:
            maskIndex = (maskIndex + 1 > maskPaths.count - 1) ? 0 : (maskIndex + 1)
            path = maskPaths[maskIndex]
        case .filters:
            filterIndex = (filterIndex + 1 > filterPaths.count - 1) ? 0 : (filterIndex + 1)
            path = filterPaths[filterIndex]
        }
        
        switchMode(path)
    }
    
    @objc
    private func didTapMasksButton() {
        currentMode = .masks
    }
    
    @objc
    private func didTapEffectsButton() {
        currentMode = .effects
    }
    
    @objc
    private func didTapFiltersButton() {
        currentMode = .filters
    }
    
    @objc
    private func didTapPhotoButton() {
        currentRecordingMode = .photo
    }
    
    @objc
    private func didTapVideoButton() {
        currentRecordingMode = .video
    }
    
    @objc
    private func didTapLowQVideoButton() {
        currentRecordingMode = .lowQualityVideo
    }
    
    func didFinishPreparingForVideoRecording() { }

    public func didStartVideoRecording() { }

    public func didFinishVideoRecording(_ videoFilePath: String!) {

        let documentsDirectory = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first!
        let components = videoFilePath.components(separatedBy: "/")
        guard let last = components.last else { return }
        let destination = URL(fileURLWithPath: String(format: "%@/%@", documentsDirectory, last))

//        let playerController = AVPlayerViewController()
//        let player = AVPlayer(url: destination)
//        playerController.player = player
//        present(playerController, animated: true) {
//            player.play()
//        }
    }

    public func recordingFailedWithError(_ error: Error!) {}

   public func didTakeScreenshot(_ screenshot: UIImage!) {
        UIImageWriteToSavedPhotosAlbum(screenshot, nil, nil, nil)

        let imageView = UIImageView(image: screenshot)
        imageView.frame = self.frame
    self.arView.insertSubview(imageView, aboveSubview: arView)

        let flashView = UIView(frame: self.arView.frame)
        flashView.alpha = 0
        flashView.backgroundColor = .black
        self.arView.insertSubview(flashView, aboveSubview: imageView)

        UIView.animate(withDuration: 0.1, animations: {
            flashView.alpha = 1
        }) { _ in
            flashView.removeFromSuperview()

            DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
                imageView.removeFromSuperview()
            }
        }
    }

   public func didInitialize() {}

  public  func faceVisiblityDidChange(_ faceVisible: Bool) {}
    
}


extension String {
    var path: String? {
        return Bundle.main.path(forResource: self, ofType: nil)
    }
}

