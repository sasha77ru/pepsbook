package ru.sasha77.spring.pepsbook

import org.aspectj.lang.ProceedingJoinPoint
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import ru.sasha77.spring.pepsbook.MyUtilities.myDate
import java.lang.RuntimeException
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
        var seeds : String = ""
        var rounds : Int = 0
        var steps : Int = 0
        var failImmediately : Boolean = true
    }
}

/**
 * Story in map numbers of performance measurements and their sums
 */
@Component
open class PerformanceCounter {
    class Meas {var num = 0; var sum = 0L; override fun toString () = "sum : $sum, avg : ${sum/num}"}
    var m = mutableMapOf<String,Meas>()
    fun addValue (name : String, value : Long) {m.getOrPut(name,::Meas).apply { num++; sum+=value }}
    fun getAndReset (name : String) = m[name].also { m = mutableMapOf() }
    fun getAllAndReset () = m.also { m = mutableMapOf() }
}

/**
 * The aspect launched around Rest Controllers
 */
@Component("restPerformanceAop")
open class RestPerformanceAspect {
    @Autowired lateinit var performanceCounter : PerformanceCounter
    private var log = LoggerFactory.getLogger(this::class.java)!!
    @Suppress("unused")
    fun performanceAdvice(joinPoint : ProceedingJoinPoint): Any? {
        val beforeTime = System.nanoTime()
        val result = joinPoint.proceed(joinPoint.args)
        performanceCounter.addValue(joinPoint.signature.name,System.nanoTime() - beforeTime)
//        log.info("${System.nanoTime() - beforeTime} < ${joinPoint.signature.name} (${joinPoint.args.drop(1).joinToString(",")})")
        return result
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
