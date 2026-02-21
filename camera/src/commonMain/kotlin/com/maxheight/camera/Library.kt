package com.maxheight.camera

import com.maxheight.camera.Library.sayHello

expect object Library {
    fun sayHello(): String
}

fun sayHelloWrapper(): String {
    return sayHello()
}