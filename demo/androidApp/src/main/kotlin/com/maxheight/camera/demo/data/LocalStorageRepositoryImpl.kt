package com.maxheight.camera.demo.data

import android.content.Context
import android.util.Log
import com.maxheight.camera.demo.domain.repository.LocalStorageRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class LocalStorageRepositoryImpl(
    private val context: Context
) : LocalStorageRepository {

    override fun createTempFile(): Any {
        val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis()) + ".mp4"

        return File(context.filesDir, fileName)
    }

    override fun deleteLocalFile(
        filePath: String
    ): Boolean {
        val file = File(filePath)
        return if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                Log.i(TAG, "Local file deleted: $filePath")
            } else {
                Log.e(TAG, "Failed to delete local file: $filePath")
            }
            deleted
        } else {
            Log.w(TAG, "Attempted to delete non-existent local file: $filePath")
            true // Considered successful if the file doesn't exist
        }
    }

    companion object {
        private const val TAG = "LocalStorageRepository"
    }
}