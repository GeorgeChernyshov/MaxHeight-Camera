package com.maxheight.camera.internal

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.CoreMedia.CMSampleBufferRef
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
internal class VideoDataOutputSampleBufferDelegate :
    NSObject(),
    AVCaptureVideoDataOutputSampleBufferDelegateProtocol {

    var processFrame: (CMSampleBufferRef) -> Unit = {}

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection
    ) {
        didOutputSampleBuffer?.let {
            processFrame(it)
        }
    }
}