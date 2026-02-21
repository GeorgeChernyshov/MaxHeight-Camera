package com.maxheight.camera.demo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform