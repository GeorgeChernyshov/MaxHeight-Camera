package com.maxheight.camera.demo

class Greeting {
    private val platform = getPlatform()

    fun gret(): String {
        return "Hello, ${platform.name}!"
    }
}