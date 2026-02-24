package com.maxheight.camera.demo.data

import com.maxheight.camera.demo.domain.repository.LocalStorageRepository
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

class LocalStorageRepositoryImpl : LocalStorageRepository {

    override fun createTempFile(): Any {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        )

        val documentsDirectory = paths.firstOrNull() as String
        val formatter = NSDateFormatter()
        formatter.setDateFormat("yyyy-MM-dd-HH-mm-ss-SSS")
        val uniqueFileName = formatter.stringFromDate(NSDate()) + ".mp4"
        val outputDirectory = NSURL.fileURLWithPath(documentsDirectory)

        return outputDirectory.URLByAppendingPathComponent(uniqueFileName)!!
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun deleteLocalFile(
        filePath: String
    ): Boolean {
        val fileManager = NSFileManager.defaultManager

        val fileUrl = NSURL.fileURLWithPath(filePath)

        return try {
            fileManager.removeItemAtURL(fileUrl, null)
            println("$TAG: Successfully deleted file at path: $filePath")

            true
        } catch (e: Throwable) {
            println("$TAG: Failed to delete file at path: $filePath. Error: ${e.message}")

            false
        }
    }

    companion object {
        private const val TAG = "LocalStorageRepository"
    }
}