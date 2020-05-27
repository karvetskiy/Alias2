package org.example

import com.google.gson.Gson
import spark.Spark
import java.net.URISyntaxException
import java.sql.Connection
import java.sql.DriverManager
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
    Spark.get("nextUser"){r,_ ->
        val roomid = r.queryParams("roomid").toInt()
        val room = rooms.find { it.roomid == roomid }!!
        var activeUser = User()
        room.users.find { it.userid == room.activeUserID}?.let { activeUser = it }
        var nextUserIndex = room.users.indexOf(activeUser) + 1
        if (nextUserIndex >= room.users.size){
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
        val room = rooms.find { it.roomid == r.queryParams("roomid").toInt() }!!
        Gson().toJson(room)
    }


    Spark.get("getWord"){_,_ ->
        Gson().toJson(getWordFromDataBase())
    }

    Spark.get("updateOnServer"){r,_ ->
        val room = rooms.find { it.roomid == r.queryParams("roomid").toInt() }!!
        val user = room.users.find { it.userid == r.queryParams("userid").toInt() }!!
        user.score = r.queryParams("score").toInt()
        user.username = r.queryParams("username")
        ""
    }

    Spark.get("winner"){r,_ ->
        val room = rooms.find {it.roomid == r.queryParams("roomid").toInt() }!!
        val winner = room.users.maxBy { it.score }!!
        winner.userid

    }

    Spark.get("start"){r,_ ->
        val room = rooms.find { it.roomid == r.queryParams("roomid").toInt() }!!
        room.isStarted = true
        room.isEnded = false
        ""
    }

    Spark.get("end"){r,_ ->
        val room = rooms.find { it.roomid == r.queryParams("roomid").toInt() }!!
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
        var word: String
        val statement = connection!!.createStatement()
        val minSet = statement.executeQuery("SELECT min(id) FROM words")
        minSet.last()
        val min = minSet.getInt("id")
        val maxSet = statement.executeQuery("SELECT max(id) FROM words")
        maxSet.last()
        val max = maxSet.getInt("id")
        val rand = Random.nextInt(min, max+1)
        val wordSet = statement.executeQuery("SELECT word FROM words WHERE id = $rand")
        wordSet.last()
        word = wordSet.getString("word")
        statement.close()
        return word
    }

    fun addWordToDataBase(word: String){
        val statement = connection!!.createStatement()
        statement.executeUpdate("INSERT INTO words(word) VALUES ('$word')")
        statement.close()
    }


