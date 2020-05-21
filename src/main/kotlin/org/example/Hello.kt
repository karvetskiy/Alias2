package org.example

import com.google.gson.Gson
import spark.Spark
import kotlin.collections.ArrayList
import kotlin.random.Random

val rooms = ArrayList<Room>()
val words = listOf("КОМПОЗИТОР", "КАРНИЗ", "ЛОБОТОМИЯ", "САНАТОРИЙ", "РЕВИЗОР", "ЛАВАНДА", "ВЫДРА")


fun main(args: Array<String>) {

    var portNumber = if (args.isNotEmpty()){
        args[0].toInt()
    } else
        9999

    println("Connect to http://localhost:$portNumber")

    //подключаемся к полученному порту
    Spark.port(portNumber)

    //Создаем комнату
    Spark.get("createRoom"){ _, _ ->
        val room = Room()
        isRoomIDExist(room.roomid, room)
        rooms.add(room)
        Gson().toJson(room)
    }

    //Удаляем комнату
    Spark.get("destroyRoom"){r,_ ->
        val roomid = r.queryParams("roomid").toInt()
        val index = rooms.indexOf(rooms.find{it.roomid == roomid})
        rooms.removeAt(index)
        ""
    }

    //Добавляем игрока в комнату
    Spark.get("addUser"){r,_ ->
        val roomid = r.queryParams("roomid").toInt()
        val user = User()
        val room = rooms.find { it.roomid == roomid } as Room
        isUserIDExist(user, room)
        room.users.add(user)
        room
    }

    //Удаляем игрока из комнаты
    Spark.get("deleteUser"){r,_ ->
        val roomid = r.queryParams("roomid").toInt()
        val userid = r.queryParams("userid").toInt()
        val room = rooms.find { it.roomid == roomid } as Room
        room.users.remove(room.users.find { it.userid==userid })
        ""
    }

    //Изменение активного игрока
    Spark.get("activeUser"){r,_ ->
        val activeUserID = r.queryParams("userid").toInt()
        val room = rooms.find {it.roomid == r.queryParams("roomid").toInt()} as Room
        room.activeUserID = activeUserID
        room
    }

    Spark.get("instance"){r,_ ->
        val room = rooms.find { it.roomid == r.queryParams("roomid").toInt() } as Room
        room
    }


    Spark.get("getWord"){_,_ ->
        val index = Random(words.size - 1).nextInt()
        val word = words[index]
        word
    }


    //Обработка некорректных запросов
    Spark.get("*"){ _, _ ->
        "404 : Page not found"
    }
}

fun isRoomIDExist(id: Int, room: Room){
    while (rooms.find{ it.roomid == id }!=null){
        room.createRoomID()
    }
}

fun isUserIDExist(user: User, room: Room){
    while (room.users.find{it.userid == user.userid}!=null){
        user.createUserID()
    }
}

