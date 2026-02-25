package com.maxheight.camera.internal

import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.darwin.NSObject

internal class CaptureFileOutputDelegate() :
    NSObject(),
    AVCaptureFileOutputRecordingDelegateProtocol {

    var onRecordingStarted: () -> Unit = {}
    var onRecordingFinalized: (String?, String?) -> Unit = { _, _ -> }

    override fun captureOutput(
        output: AVCaptureFileOutput,
        didStartRecordingToOutputFileAtURL: NSURL,
        fromConnections: List<*>
    ) {
        onRecordingStarted()
    }

    override fun captureOutput(
        output: AVCaptureFileOutput,
        didFinishRecordingToOutputFileAtURL: NSURL,
        fromConnections: List<*>,
        error: NSError?
    ) {
        if (error == null) {
            onRecordingFinalized(didFinishRecordingToOutputFileAtURL.path, null)
        } else {
            onRecordingFinalized(null, error.localizedDescription)
        }
    }
}