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

//<editor-fold desc="Some funs to compare strings from db and from page">
fun dbRegExp (s : String) = s.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
        .replace(Regex("\\[ "), "[")
fun pageRegExp (s : String) = s.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
        .replace(Regex("\\[ "), "[")
//        .replace("&lt;","<") // because noTag is on duty
//        .replace("&gt;",">")
fun myCompare (dbString : String, pageString : String) = dbRegExp(dbString) == pageRegExp(pageString)
//</editor-fold>

enum class For {LOAD,SEE,REPEAT,LONG_LOAD}
fun pause (x : For) = Thread.sleep(when (x) {
    For.LOAD -> 600 /*todo debug 300*/
    For.LONG_LOAD -> 1000
    For.SEE -> 0
    For.REPEAT -> 100})

/**
 * Pause until predicate is true. Try it every For.REPEAT ms
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
    var monkey = MonkeyTestProps()
    open class MonkeyTestProps {
        open class FeignDB {
            var enabled = "no"
            var users = 30
            var friendships = 10
            var minds = 200
            var answers = 300
        }
        var feignDB = FeignDB()
        var seeds : String = ""
        var rounds : Int = 0
        var steps : Int = 0
        var failImmediately : Boolean = true
    }
}

/**
 * Story in map numbers of performance measurements and their sums. Can return num and sum (so avg) of all added values
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
 * The aspect launched around Rest Controllers to collect performance statistics
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

/**
 * A mixin that allow to get random and geign strings by given randomer and history of used feign strings (for their uniq)
 */
abstract class FeignMixin {
    abstract val randomer : kotlin.random.Random
    abstract val usedStrings : MutableSet<String> //to eliminate dups (uniq because users, minds and answers in test must be uniq)

    fun rand(x : Int = 100) = randomer.nextInt(x)
    /**
     * Returns random Int by randomer
     * @param from inclusive
     * @param to inclusive
     * */
    fun rand(from : Int,to : Int) = randomer.nextInt(to - from + 1) + from
    /**
     * Returns random Int by randomer. The probability of 0 could be set
     * @param max inclusive
     * @param zeroProbability in percents (default 30%)
     * */
    fun randZero(max : Int,zeroProbability : Int = 30) : Int {
        val addon = max/100*zeroProbability
        val result = randomer.nextInt(max+1+addon)-addon
        return if (result < 0) 0 else result
    }
    private fun validFirstChar () = (('a'..'z') + ('A'..'Z')).random(randomer)
    private fun validChar () = (('a'..'z') + ('A'..'Z') + ('0'..'9') + '_' + '-').random(randomer)
    fun invalidChar () = "`~!@#№$%^&*()+=[]{};:'\"\\|,.<>/? ".random(randomer)
    /**
     * Feign uniq username (uniq because users and their emails in test must be uniq)
     */
    fun feignUsername (len : Int = 8, charFrom : () -> Char = ::validChar) : String = String(mutableListOf(validFirstChar()).
            apply { repeat(len-2) { add(charFrom()) };add(validFirstChar()) }.toCharArray()) //in emails last char also can't be - or _
            .let { if (it in usedStrings) feignUsername(len,charFrom) else {usedStrings.add(it);it} } //to eliminate dups
//        val syLIST = listOf(" а","Ва","ло","Но","uv"," d"," W")
//        val syLIST = listOf(" а","Ва","ло","Но","uv"," d","W ")
    val syLIST = listOf(" А","ва","ло","ем","ок"," У","ы ")
    /**
     * Feign uniq string (uniq because minds and answers in test must be uniq)
     * of APPROXIMATELY (may be a little bit longer) len length
     */
    fun feignString (len : Int = 12) : String = StringBuilder().apply {
        repeat(len/2) {
            append((syLIST+listOf(String(listOf(invalidChar(),invalidChar()).toCharArray()))).random(randomer))
        } }.toString()
            .let { if (it in usedStrings) feignString(len+2) else {usedStrings.add(it);it} } //to eliminate dups
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
