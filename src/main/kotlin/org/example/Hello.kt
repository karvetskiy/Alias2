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


    //Добавляем игрока в комнату
    Spark.get("addUser"){r,_ ->
        val roomid = r.queryParams("roomid").toInt()
        val user = User()
        val room = rooms.find { it.roomid == roomid }!!
        room.users.add(user)
        user.userid
    }

    // Следующий игрок
    Spark.get("nextUser"){r,_ ->
        val roomid = r.queryParams("roomid").toInt()
        val room = rooms.find { it.roomid == roomid } as Room
        val activeUser = room.users.find { it.userid == room.activeUserID }!!
        var nextUserIndex = room.users.indexOf(activeUser) + 1
        if (nextUserIndex > room.users.size){
            nextUserIndex = 0
        }
        room.activeUserID = room.users[nextUserIndex].userid
        ""
    }

    Spark.get("deleteUser"){r,_ ->
        val roomid = r.queryParams("roomid").toInt()
        val userid = r.queryParams("userid").toInt()
        val room = rooms.find { it.roomid == roomid }!!
        val user = room.users.find { it.userid == userid }!!
        if (room.users.size == 1){
            rooms.remove(room)
        }else {
            room.users.remove(user)
        }
        ""
    }

    //Изменение активного игрока
    Spark.get("activeUser"){r,_ ->
        val activeUserID = r.queryParams("userid").toInt()
        val room = rooms.find {it.roomid == r.queryParams("roomid").toInt()}!!
        room.activeUserID = activeUserID
        Gson().toJson(room)
    }

    Spark.get("getRoomState"){r,_ ->
        val room = rooms.find { it.roomid == r.queryParams("roomid").toInt() }
        Gson().toJson(room)
    }


    Spark.get("getWord"){_,_ ->
        val index = Random.nextInt(words.size)
        val word = words[index]
        Gson().toJson(word)
    }

    Spark.get("updateOnServer"){r,_ ->
        val room = rooms.find { it.roomid == r.queryParams("roomid").toInt() }!!
        val user = room.users.find { it.userid == r.queryParams("userid").toInt() }!!
        val score = r.queryParams("score").toInt()
        user.score = score
        user.username = r.queryParams("un")
        ""
    }

    Spark.get("winner"){r,_ ->
        val room = rooms.find {it.roomid == r.queryParams("roomid").toInt() }!!
        room.users.maxBy { it.score }

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

