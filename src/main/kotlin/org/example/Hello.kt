package org.example

import com.example.alias_client.data.RequestBody
import com.google.gson.Gson
import spark.Spark
import java.net.URISyntaxException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.random.Random

val rooms = ArrayList<Room>()
val words = listOf("КОМПОЗИТОР", "КАРНИЗ", "ЛОБОТОМИЯ", "САНАТОРИЙ", "РЕВИЗОР", "ЛАВАНДА", "ВЫДРА","РЕБЕНОК","ПАРИК","ЛОДКА","БОЛЬНИЦА","ОДИНОЧЕСТВО","ПОЭТ","МОЛНИЯ","ПЕЛЬМЕНИ","ВИТРАЖ","МАРКЕР","ВИТРИНА","СЕМЕЧКИ","КУРИЦА","СПОРТСМЕН","ПАРАД","ТАБЛИЦА","ЭКСКУРСИЯ","МАНТИЯ","ТРОСТЬ","АГИТАЦИЯ","РАВИОЛИ","ВКЛАД","КЛЕВЕР","РАДИАТОР")
var connection: Connection? = null

fun main(args: Array<String>) {

    getConnection()

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
    Spark.post("nextUser"){r,_ ->
        var room = Room()
        val request = Gson().fromJson(r.body().toString(), RequestBody::class.java)
        rooms.find { it.roomid == request.roomid }?.let { room = it }
        var activeUser = User()
        room.users.find { it.userid == room.activeUserID}?.let { activeUser = it }
        var nextUserIndex = room.users.indexOf(activeUser) + 1
        if (nextUserIndex >= room.users.size){
            nextUserIndex = 0
        }
        room.activeUserID = room.users[nextUserIndex].userid
        ""
    }

    Spark.post("deleteUser"){r,_ ->
        var room = Room()
        var user = User()
        val request = Gson().fromJson(r.body().toString(), RequestBody::class.java)
        rooms.find { it.roomid == request.roomid }?.let { room = it }
        room.users.find { it.userid == request.userid }?.let { user = it }

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
        val room = rooms.find { it.roomid == r.queryParams("roomid").toInt() }!!
        Gson().toJson(room)
    }


    Spark.get("getWord"){_,_ ->
        Gson().toJson(getWordFromDataBase())
    }

    Spark.post("updateOnServer"){r,_ ->
        var room = Room()
        var user = User()
        val request = Gson().fromJson(r.body().toString(), RequestBody::class.java)
        rooms.find { it.roomid == request.roomid }?.let { room = it }
        room.users.find { it.userid == request.userid }?.let { user = it }
        user.score = request.score
        user.username = request.username
        ""
    }

    Spark.get("winner"){r,_ ->
        val room = rooms.find {it.roomid == r.queryParams("roomid").toInt() }!!
        val winner = room.users.maxBy { it.score }!!
        winner.userid

    }

    Spark.post("start"){r,_ ->
        var room = Room()
        val request = Gson().fromJson(r.body().toString(), RequestBody::class.java)
        rooms.find { it.roomid == request.roomid }?.let { room = it }
        room.isStarted = true
        room.isEnded = false
        ""
    }

    Spark.post("end"){r,_ ->
        val request = Gson().fromJson(r.body().toString(), RequestBody::class.java)
        var room = Room()
        rooms.find { it.roomid == r.queryParams("roomid").toInt() }?.let { room = it }
        room.isEnded = true
        room.isStarted = false
        for (user in room.users){
            user.score = 0
        }
        ""
    }


    //Обработка некорректных запросов
    Spark.get("*"){ _, _ ->
        "404 : Page not found"
    }
}

    fun isRoomIDExist(id: Int, room: Room) {
        while (rooms.find { it.roomid == id } != null) {
            room.createRoomID()
        }
    }

    @Throws(URISyntaxException::class, SQLException::class)
    private fun getConnection() {
        val dbUrl = System.getenv("JDBC_DATABASE_URL")
        connection = DriverManager.getConnection(dbUrl)
    }

    fun getWordFromDataBase(): String{
        var word = ""
        val statement = connection!!.createStatement()
        val wordSet = statement.executeQuery("SELECT word FROM words ORDER BY random() LIMIT 1")
        while (wordSet.next()){
            word = wordSet.getString("word")
        }
        statement.close()
        return word
    }

    fun addWordToDataBase(word: String){
        val statement = connection!!.createStatement()
        statement.executeUpdate("INSERT INTO words(word) VALUES ('$word')")
        statement.close()
    }


