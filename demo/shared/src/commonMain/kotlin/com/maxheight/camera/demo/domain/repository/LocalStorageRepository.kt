package com.maxheight.camera.demo.domain.repository

interface LocalStorageRepository {

    fun createTempFile(): Any

    /**
     * Deletes a local file from the device.
     *
     * @param filePath The absolute path to the local file.
     * @return True if deletion was successful, false otherwise.
     */
    fun deleteLocalFile(filePath: String): Boolean
}