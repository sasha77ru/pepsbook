package ru.sasha77.spring.pepsbook

import org.openqa.selenium.WebDriver
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import ru.sasha77.spring.pepsbook.MyUtilities.myDate
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*

enum class For {LOAD,SEE,REPEAT,LONG_LOAD}
fun pause (x : For) = Thread.sleep(when (x) {
    For.LOAD -> 300
    For.LONG_LOAD -> 1000
    For.SEE -> 0
    For.REPEAT -> 100})

/**
 * Pause until predicate it truw. Try it every For.REPEAT ms
 */
fun pause (attempts : Int = 50, predicate : () -> Boolean) {
    pause(For.LOAD)
    repeat(attempts) {
        if (predicate()) return
        pause(For.REPEAT)
    }
    throw RuntimeException("Waiting unsuccessful")
}

interface ObjWithDriver {
    val driver : WebDriver
}

data class TstMind(
    var text    : String,
    val user    : String,
    val time    : Date,
    var answers : MutableList<TstAnswer> = mutableListOf()) {
    fun getAnswerByText (findText : String) = answers.find { it.text == findText } ?: throw IllegalArgumentException("No such TSTANSWER $text")
    fun addAnswer (text: String,user: String,time: Date) {answers.add(TstAnswer(text,this,user,time))}
    fun removeAnswer (findText : String) {answers.remove(getAnswerByText(findText))}
    fun changeAnswer (findText : String, newText : String) {getAnswerByText(findText).text = newText}
    override fun toString(): String = "$text / $user / ${myDate(time)} / ${answers.sortedBy { it.time }}"
}
data class TstAnswer(
    var text : String,
    val mind : TstMind,
    val user : String,
    val time : Date) {
    override fun toString(): String = "$text / ${mind.text} / $user / ${myDate(time)}"
}


/**
 * Properties holder
 */
@Component
@ConfigurationProperties(prefix = "my.tst")
class TstProps {
    var headLess : Boolean = false
    var closeBrowser : Boolean = true
    var monkey : MonkeyTestProps = MonkeyTestProps()
    open class MonkeyTestProps {
        var seed : Long = 0
        var rounds : Int = 0
        var steps : Int = 0
        var failImmediately : Boolean = true
    }
}

//fun fillDBwithSQL () {
//    val connection = DriverManager.getConnection("jdbc:h2:mem:testo", "sa", "")
//    val s = connection.createStatement()
//    try {
//        s.execute("DROP TABLE IF EXISTS users;")
//        s.execute("CREATE TABLE users (id INT PRIMARY KEY,name VARCHAR(100), email VARCHAR(100));")
//        tstUsersArray.forEachIndexed { i, user ->
//            s.execute("INSERT INTO users (id,name,email,country) VALUES ($i,'$user.name','$user.email','$user.country');")
//        }
//    } catch (sqle: SQLException) {
//        println("EXSEPSION:$sqle")
//    } finally {
//        connection.close()
//    }
//}
