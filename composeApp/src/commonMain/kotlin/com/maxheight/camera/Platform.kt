package com.maxheight.camera

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform