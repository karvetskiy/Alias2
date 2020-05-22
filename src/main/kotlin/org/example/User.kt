package org.example

import java.util.*


class User {
    var userid = 0
    var score: Int = 0

    init {
        createUserID()
    }

    private fun createUserID(){
        userid = (10000 + Random().nextInt(90000))
    }
}