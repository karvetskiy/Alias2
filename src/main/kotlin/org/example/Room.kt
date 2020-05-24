package org.example

import java.util.*
import kotlin.collections.ArrayList

class Room {
    var roomid = 0
    val users = ArrayList<User>()
    var activeUserID = 0
    var isEnded = true
    var isStarted = false

    init {
        createRoomID()
    }

    fun createRoomID(){
        roomid =(1000 + Random().nextInt(9000))
    }


}