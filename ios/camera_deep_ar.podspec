#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint camera_deep_ar.podspec' to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'camera_deep_ar'
  s.version          = '0.0.1'
  s.summary          = 'A new Flutter plugin.'
  s.description      = <<-DESC
A new Flutter plugin.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '8.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'armv7' }
  s.swift_version = '5.0'


  #My own addition to the .podspec
  s.preserve_paths = 'DeepAR.framework'
  s.xcconfig = { 'OTHER_LDFLAGS' => '-framework DeepAR' }
  s.vendored_frameworks = 'DeepAR.framework'

#  s.resource_bundles = {
#    'Effects' => ['camera_deep_ar/Effects/**/*']
#  }
#  s.resource = 'Pod/Resources/**/*'
  s.resources = ['Effects/*']
  s.resource_bundle = { 'Effects' => 'Effects/*.' }



end
