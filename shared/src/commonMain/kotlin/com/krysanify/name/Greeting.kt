package com.krysanify.name

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}