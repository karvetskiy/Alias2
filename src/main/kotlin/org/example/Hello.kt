package org.example

import dev.karvetskiy.Room
import dev.karvetskiy.User
import spark.Spark

val rooms = ArrayList<Room>()

fun main(args: Array<String>) {


    var portNumber = 9999

    try{
        portNumber = args[0].toInt()
    }catch (e: Exception){
        e.printStackTrace()
    }
    //подключаемся к полученному порту
    Spark.port(portNumber)

    //Создаем комнату
    Spark.get("createRoom"){ _, _ ->
        val room = Room()
        isRoomIDExist(room.id, room)
        rooms.add(room)
        room
    }

    //Удаляем комнату
    Spark.post("destroyRoom"){r,_ ->
        val roomid = r.queryParams("roomid").toInt()
        val index = rooms.indexOf(rooms.find{it.id.toInt() == roomid})
        rooms.removeAt(index)
        ""
    }

    //Добавляем игрока в комнату
    Spark.get("addUser"){r,_ ->
        val roomid = r.queryParams("roomid").toInt()
        val user = User()
        val room = rooms.find { it.id.toInt() == roomid } as Room
        isUserIDExist(user, room)
        room.users.add(user)
        room
    }

    //Удаляем игрока из комнаты
    Spark.post("deleteUser"){r,_ ->
        val roomid = r.queryParams("roomid").toInt()
        val userid = r.queryParams("userid").toInt()
        val room = rooms.find { it.id.toInt() == roomid } as Room
        room.users.remove(room.users.find { it.id.toInt()==userid })
        ""
    }

    //Изменение активного игрока
    Spark.post("activeUser"){r,_ ->
        val activeUserID = r.queryParams("userid").toInt()
        val room = rooms.find {it.id.toInt() == r.queryParams("roomid").toInt()} as Room
        room.activeUserID = activeUserID
        ""
    }

    Spark.get("instance"){r,_ ->
        val room = rooms.find { it.id.toInt() == r.queryParams("roomid").toInt() } as Room
        room
    }


    Spark.get("getWord"){_,_ ->
        val word = "WORD"
        word
    }




    //Обработка некорректных запросов
    Spark.get("*"){ _, _ ->
        "404 : Page not found"
    }
}

fun isRoomIDExist(id: Integer, room: Room){
    while (rooms.find{it.id == id}!=null){
        room.createRoomID()
    }
}

fun isUserIDExist(user: User, room: Room){
    while (room.users.find{it.id == user.id}!=null){
        user.createUserID()
    }
}

