package dev.karvetskiy

import java.util.*
import kotlin.collections.ArrayList

class Room {
    lateinit var id: Integer
    val users = ArrayList<User>()
    var activeUserID = 0

    init {
        createRoomID()
    }

    fun createRoomID(){
        id =(1000 + Random().nextInt(9000)) as Integer
    }


}