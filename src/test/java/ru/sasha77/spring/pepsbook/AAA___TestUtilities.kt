package ru.sasha77.spring.pepsbook

import org.aspectj.lang.JoinPoint
import org.hamcrest.Matchers
import org.junit.Assert
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ImportResource
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*

enum class For {LOAD,SEE,REPEAT,LONG_LOAD}
fun pause (x : For) = Thread.sleep(when (x) {
    For.LOAD -> 200
    For.LONG_LOAD -> 1000
    For.SEE -> 0
    For.REPEAT -> 100})
fun pause (attempts : Int = 50, f : () -> Boolean) {
    pause(For.LOAD)
    repeat(attempts) {
        if (f()) return
        pause(For.REPEAT)
        print(" .")
    }
    throw RuntimeException("Waiting unsuccessful")
}

fun Date.myFormat () = SimpleDateFormat("dd.MM.yy HH:mm").format(this)

interface ObjWithDriver {
    val driver : WebDriver
}

data class TstMind(
    var text    : String,
    val user    : String,
    val time    : Date,
    var answers : MutableList<TstAnswer> = mutableListOf()) {
    init {
        println("TstMind $text")
    }
    fun getAnswerByText (findText : String) = answers.find { it.text == findText } ?: throw IllegalArgumentException("No such TSTANSWER $text")
    fun addAnswer (text: String,user: String,time: Date) {answers.add(TstAnswer(text,this,user,time))}
    fun removeAnswer (findText : String) {answers.remove(getAnswerByText(findText))}
    override fun toString(): String = "$text / $user / ${time.myFormat()} / ${answers.sortedBy { it.time }}"
}
data class TstAnswer(
    var text : String,
    val mind : TstMind,
    val user : String,
    val time : Date) {
    override fun toString(): String = "$text / ${mind.text} / $user / ${time.myFormat()}"
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

/**
 * The class contains applications proper state in arrays: actualUsersArray,actualMindsArray
 * The class has methods like toFriends, saveMind, etc that change this state and at the same time perform
 * mvc requests to application to make changes in DB. So the information in DB and state arrays should always be the same.
 * It's possible to check it with checkDB method, that performs mvc get requests and compare states.
 */
@Suppress("MemberVisibilityCanBePrivate")
@Component("Tao")
class TestApplicationObject (val usersRepo: UserRepository,
                             val mindsRepo: MindRepository,
                             val answersRepo: AnswerRepository) {
    inner class TstUser(
        val name: String,
        val email: String,
        val country: String,
        val username : String,
        val password : String,
        val friendsNames: MutableSet<String> = mutableSetOf(),
        val matesNames: MutableSet<String> = mutableSetOf(),
        val user : User = User(name, email, country, username, passwordEncoder.encode(password))) {
        private fun MutableSet<String>.copy() : MutableSet<String> =
                mutableSetOf<String>().apply {this@copy.forEach { this.add(it) }}
        fun deepCopy() : TstUser =
                TstUser(user.name,user.email,user.country,username, password,friendsNames.copy(),matesNames.copy(), user = user)
    }

    @Autowired
    lateinit var mockMvc : MockMvc

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    var doMvc = true
    var tstUsersArray = emptyList<TstUser>() // an initial test list
    var actualUsersArray = mutableListOf<TstUser>() // actual test list, initially equals to tstUsersArray but can be changed during tests
    fun List<TstUser>.getByName(name: String) = //fun for tstUsersArray
            find { it.name == name} ?: throw IllegalArgumentException("No such TST_NAME $name")
    fun List<TstUser>.getByUserName(name: String) = //fun for tstUsersArray
            find { it.username == name} ?: throw IllegalArgumentException("No such TST_USERNAME $name")
    fun List<TstUser>.deepCopy() = //fun for tstUsersArray
            mutableListOf<TstUser>().apply {this@deepCopy.forEach { this.add(it.deepCopy()) }}

    val actualMindsArray = mutableListOf<TstMind>()
    fun List<TstMind>.getByText(text: String) = //fun for tstMindsArray
            find { it.text == text} ?: throw IllegalArgumentException("No such TSTMIND $text")

    private var _currUser : TstUser? = null
    var currUser : TstUser
        get() = _currUser!!
        set(value) {_currUser = value }
    var currName: String
        get() = currUser.name
        set(value) {currUser = actualUsersArray.getByName(value)}
    var currUserName: String
        get() = currUser.username
        set(value) {currUser = actualUsersArray.getByUserName(value)}
    val currPassword: String
        get() = currUser.password
    fun isFriendToCurr (x : TstUser) : Boolean  = currUser.friendsNames.contains(x.name)
    fun isMateToCurr (x : TstUser) = currUser.matesNames.contains(x.name)
    fun positive (x : TstUser) = true


    /**
     * Clears DB.
     */
    fun clearDB () {
        //        mindsRepo.deleteAll()
        /*WANTS TEST without schema, but with ddl-auto=create-drop. UNCOMMENT UPPER ROW. But learn cascades first*/
        usersRepo.deleteAll()
        tstUsersArray = listOf()
        actualUsersArray = mutableListOf()
        actualMindsArray.clear()
    }

    /**
     * Clear DB, than fill it with data from tstUsersArray and tstMindsArray.
     * Than does actualUsersArray = tstUsersArray, actualMindsArray = tstMindsArray
     * if friendship==false then friendship information ignored
     */
    fun fillDB (friendship : Boolean = true, minds : Boolean = true) {
        var lmDate = Date(Date().time-86400000)
        fun mockDate () : Date {
            lmDate = Date(lmDate.time+100000)
            return lmDate
        }

        clearDB()

        tstUsersArray =
                listOf(
                        TstUser("Porky", "porky@pig.com", "USA", "porky", "pig",
                                if (friendship) mutableSetOf("Pluto", "Masha") else mutableSetOf()),
                        TstUser("Pluto", "pluto@dog.com", "USA", "pluto", "dog",
                                if (friendship) mutableSetOf("Porky") else mutableSetOf()),
                        TstUser("Masha", "masha@child.com", "Russia", "masha", "child"),
                        TstUser("Luntik", "luntik@alien.com", "Russia", "luntik", "alien",
                                if (friendship) mutableSetOf("Porky") else mutableSetOf())
                ).apply {
                    //Fill matesNames of TstUser by friendsNames
                    if (friendship) forEach { user ->
                        user.friendsNames.forEach { friend ->
                            getByName(friend).matesNames.add(user.user.name)
                        }
                    }
                }
        actualUsersArray = tstUsersArray.deepCopy()

        with (actualMindsArray) {
            add(TstMind("Hru-hru", "Porky", mockDate())
                    .apply {
                        answers = mutableListOf(
                                TstAnswer("Вы свинья", this, "Masha", mockDate()),
                                TstAnswer("Соласен", this, "Luntik", mockDate()))
                    })
            add(TstMind("Gaff-gaff", "Pluto", mockDate()))
            add(TstMind("Понятненько", "Masha", mockDate()))
            add(TstMind("Я лунная пчела", "Luntik", mockDate()))
        }

//    if (!friendship) actualUsersArray.forEach { it.friendsNames.removeIf { true };it.matesNames.removeIf { true }}

        tstUsersArray.forEach { usersRepo.save(it.user) }
        tstUsersArray.forEach { theUser ->
            //Fill friendship info to DB
            if (friendship) {
                theUser.friendsNames.map { tstUsersArray.getByName(it) }.forEach { friend ->
                    theUser.user.friends.add(friend.user)
                }
            }
            usersRepo.save(theUser.user)
        }
        //save minds
        if (minds) actualMindsArray.forEach { tstMind ->
            val mind = Mind(tstMind.text, tstUsersArray.getByName(tstMind.user).user, tstMind.time)
                    .apply { answers = tstMind.answers.map { tstAnswer -> //add answers to mind
                        Answer(tstAnswer.text, this, usersRepo.findByName(tstAnswer.user),tstAnswer.time)} }
            mindsRepo.save(mind)
            answersRepo.saveAll(mind.answers)
        }
    }


    /**
     * Does checkDB for all users
     */
    fun checkAllDB () {actualUsersArray.forEach {checkDB(it)}}

    /**
     * Performs mvc requests and compare models with actualUsersArray,actualMindsArray
     */
    fun checkDB (currUser: TstUser = this.currUser) {
        fun Date.round () = Date((this.time/10000+0.5).toLong()*10000)
        mockMvc.perform(MockMvcRequestBuilders.get("/users")
                .param("subs","")
                .with(httpBasic(currUser.username, currUser.password)).with(csrf()))
                    .andDo { mvcResult ->
                        @Suppress("UNCHECKED_CAST")
                        Assert.assertEquals("Different lists",
                                actualUsersArray.filter { it.user.id != currUser.user.id }.sortedBy { it.user.name }
                                        .joinToString("\n") { it.user.name },
                                (mvcResult.modelAndView!!.model["lizt"] as Iterable<User>).joinToString("\n") { it.name }
                        )
                    }
        mockMvc.perform(MockMvcRequestBuilders.get("/friends")
                .param("subs","")
                .with(httpBasic(currUser.username, currUser.password)).with(csrf()))
                    .andDo { mvcResult ->
                        @Suppress("UNCHECKED_CAST")
                        Assert.assertEquals("Different lists",
                                actualUsersArray.filter { tstUser ->
                                    tstUser.user.email != currUser.user.email
                                            && currUser.friendsNames.any { it == tstUser.user.name }
                                }.sortedBy { it.user.name }.joinToString("\n") { it.user.name },
                                (mvcResult.modelAndView!!.model["lizt"] as Iterable<User>).joinToString("\n") { it.name }
                        )
                    }
        mockMvc.perform(MockMvcRequestBuilders.get("/mates").param("subs","")
                .with(httpBasic(currUser.username, currUser.password)).with(csrf()))
                    .andDo { mvcResult ->
                        @Suppress("UNCHECKED_CAST")
                        Assert.assertEquals("Different lists",
                                actualUsersArray.filter { tstUser ->
                                    tstUser.user.email != currUser.user.email
                                            && currUser.matesNames.any { it == tstUser.user.name }
                                }.sortedBy { it.user.name }.joinToString("\n") { it.user.name },
                                (mvcResult.modelAndView!!.model["lizt"] as Iterable<User>).joinToString("\n") { it.name }
                        )
                    }
        mockMvc.perform(MockMvcRequestBuilders.get("/minds").param("subs","")
                .with(httpBasic(currUser.username, currUser.password)).with(csrf()))
                    .andDo { mvcResult ->
                        @Suppress("UNCHECKED_CAST")
                        Assert.assertEquals("Different lists",
                                actualMindsArray.sortedBy { it.text }
                                        .joinToString("\n") {mind ->
                                            with(mind) {"$text/$user/${time.round()}"} + " : " +
                                            mind.answers.joinToString(" | ") { with(it) {"$text/$user/${time.round()}"} } }
                                ,
                                (mvcResult.modelAndView!!.model["lizt"] as Iterable<Mind>).sortedBy { it.text }
                                        .joinToString("\n") {mind ->
                                            with(mind) {"$text/${user.name}/${time.round()}"} + " : " +
                                            mind.answers.joinToString(" | ") { with(it) {"$text/${user.name}/${time.round()}"} } }
                        )
                    }
    }

    fun getUser (sc: Int = 200) {
        mockMvc.perform(MockMvcRequestBuilders.get("/rest/getUser")
                .with(httpBasic(currUser.username, currUser.password)).with(csrf()))
    //                .andDo{
    //                    println("!${it.response.contentAsString}!")
    //                }
                    .run {
                        if (sc == 200) {
                            andExpect(MockMvcResultMatchers.status().isOk)
                            andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
                                    "{\"id\":${currUser.user.id}," +
                                            "\"name\":\"${currUser.user.name}\"," +
                                            "\"email\":\"${currUser.user.email}\"}")))
                        } else andExpect(MockMvcResultMatchers.status().`is`(sc))
                    }
    }

    fun toFriends (friendName : String) {
        currUser.friendsNames.add(friendName)
        actualUsersArray.getByName(friendName).matesNames.add(currName)

        if (doMvc) mockMvc.perform(MockMvcRequestBuilders.patch("/rest/toFriends")
                .with(httpBasic(currUser.username, currUser.password)).with(csrf())
                .param("friend_id", actualUsersArray.getByName(friendName).user.id.toString()))
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("error"))))
    }

    fun fromFriends (friendName : String) {
        currUser.friendsNames.remove(friendName)
        actualUsersArray.getByName(friendName).matesNames.remove(currName)

        if (doMvc) mockMvc.perform(MockMvcRequestBuilders.patch("/rest/fromFriends")
                .with(httpBasic(currUser.username, currUser.password)).with(csrf())
                .param("friend_id", actualUsersArray.getByName(friendName).user.id.toString()))
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("error"))))
    }

    fun saveMind (text : String, oldText : String? = null) {
        if (doMvc) mockMvc.perform(MockMvcRequestBuilders.post("/rest/saveMind")
                .with(httpBasic(currUser.username, currUser.password)).with(csrf())
                .param("text",text)
                .apply {
                    if (oldText != null) param("id",mindsRepo.findLike(oldText).find { true }!!.id.toString())})
                    .andExpect(MockMvcResultMatchers.status().isOk)
        if(oldText == null) actualMindsArray.add(TstMind(text,currName, Date()))
        else actualMindsArray.getByText(oldText).text = text
    }

    fun removeMind (oldText : String) {
        if (doMvc) mockMvc.perform(MockMvcRequestBuilders.delete("/rest/removeMind")
                .with(httpBasic(currUser.username, currUser.password)).with(csrf())
                .param("id",mindsRepo.findLike(oldText).find { true }!!.id.toString()))
                    .andExpect(MockMvcResultMatchers.status().isOk)
        actualMindsArray.remove(actualMindsArray.getByText(oldText))
    }


    fun saveAnswer (text : String, mindText : String, oldText : String? = null) {
        val parentMind = mindsRepo.findLike(mindText).find { true }!!
        if (doMvc) mockMvc.perform(MockMvcRequestBuilders.post("/rest/saveAnswer")
                .with(httpBasic(currUser.username, currUser.password)).with(csrf())
                .param("text",text)
                .param("parentMind",parentMind.id.toString())
                .apply {
                    if (oldText != null) param("id",answersRepo.findByText(oldText)!!.id.toString())})
                .andExpect(MockMvcResultMatchers.status().isOk)
        actualMindsArray.getByText(parentMind.text).apply {
            if (oldText == null) addAnswer(text,currName, Date())
            else getAnswerByText(oldText).text = text
        }
    }

    fun removeAnswer (oldText : String, mindText : String) {
        if (doMvc) mockMvc.perform(MockMvcRequestBuilders.delete("/rest/removeAnswer")
                .with(httpBasic(currUser.username, currUser.password)).with(csrf())
                .param("id",answersRepo.findByText(oldText)!!.id.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk)
        actualMindsArray.getByText(mindText).removeAnswer(oldText)
    }
}

@Component("logAop")
open class LogAspect {
    var log = LoggerFactory.getLogger(this::class.java)
    fun beforeAdvice(joinPoint : JoinPoint) {
        log.info("> ${joinPoint.signature.name} (${joinPoint.args.drop(1).joinToString(",")})")
    }
}


@Component
@ImportResource("LogAop.xml")
open class WebApplicationObject {
    open fun ObjWithDriver.clickLogo () {
        driver.findElement(By.className("navbar-brand")).click()
    }
    open fun ObjWithDriver.clickMainMinds () {
        driver.findElement(By.id("mainMinds")).click()
    }
    open fun ObjWithDriver.clickMainUsers () {
        driver.findElement(By.id("mainUsers")).click()
    }
    open fun ObjWithDriver.clickMainFriends () {
        driver.findElement(By.id("mainFriends")).click()
    }
    open fun ObjWithDriver.clickMainMates () {
        driver.findElement(By.id("mainMates")).click()
    }
    open fun ObjWithDriver.clickUserToFriends (userName: String) {
        driver.findElements(By.className("userEntity"))
                .find {
                    it.findElement(By.className("userName")).text
                        .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"") ==
                    userName.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                }!!.run {
                    findElement(By.className("dropdown-toggle")).click()
                    findElement(By.className("toFriends")).click()
                }
    }
    open fun ObjWithDriver.clickUserFromFriends (userName: String) {
        driver.findElements(By.className("userEntity"))
                .find {
                    it.findElement(By.className("userName")).text
                        .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"") ==
                    userName.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                }!!.run {
                    findElement(By.className("dropdown-toggle")).click()
                    findElement(By.className("fromFriends")).click()
                }
    }
    open fun ObjWithDriver.clickEditMind (mindText: String) {
        driver.findElements(By.className("mindEntity"))
                .find {
                    it.findElement(By.className("mindText")).text
                            .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"") ==
                            mindText.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                }!!.run {
            findElement(By.className("dropdown-toggle")).click()
            findElement(By.className("editMind")).click()
        }
    }
    open fun ObjWithDriver.clickDelMind (mindText: String) {
        driver.findElements(By.className("mindEntity"))
                .find {
                    it.findElement(By.className("mindText")).text
                        .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"") ==
                    mindText.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                }!!.run {
                    findElement(By.className("dropdown-toggle")).click()
                    findElement(By.className("delMind")).click()
                }
    }
    open fun ObjWithDriver.clickAnswerMind (mindText: String) {
        driver.findElements(By.className("mindEntity"))
                .find {
                    it.findElement(By.className("mindText")).text
                        .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"") ==
                    mindText.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                }!!.run {
                    findElement(By.className("dropdown-toggle")).click()
                    findElement(By.className("answerMind")).click()
                }
    }
    open fun ObjWithDriver.clickEditAnswer (answerText: String) {
        driver.findElements(By.className("answerEntity"))
                .find {
                    it.findElement(By.className("answerText")).text
                        .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"") ==
                    answerText.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                }!!.run {
                    findElement(By.className("dropdown-toggle")).click()
                    findElement(By.className("editAnswer")).click()
                }
    }
    open fun ObjWithDriver.clickDelAnswer (answerText: String) {
        driver.findElements(By.className("answerEntity"))
                .find {
                    it.findElement(By.className("answerText")).text
                            .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"") ==
                    answerText.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                }!!.run {
                    findElement(By.className("dropdown-toggle")).click()
                    findElement(By.className("delAnswer")).click()
                }
    }
    open fun ObjWithDriver.clickAnswerAnswer (answerText: String) {
        driver.findElements(By.className("answerEntity"))
                .find {
                    it.findElement(By.className("answerText")).text
                        .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"") ==
                    answerText.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                }!!.run {
                    findElement(By.className("dropdown-toggle")).click()
                    findElement(By.className("answerAnswer")).click()
                }
    }
    open fun ObjWithDriver.clickLogout () {
        (driver as JavascriptExecutor).executeScript("logOff();")
    }
    open fun ObjWithDriver.typeFilter (filterText: String, clear : Boolean = true) {
        driver.findElement(By.id("mainFilter")).run {
            if (clear) clear()
            filterText.forEach { sendKeys(it.toString()) }
        }
    }
    open fun ObjWithDriver.clickNewMind () {
        driver.findElement(By.id("newMind")).click()
    }
    open fun ObjWithDriver.clickCloseMind () {
        driver.findElement(By.id("closeMind")).click()
    }
    open fun ObjWithDriver.typeMindText (mindText: String, clear : Boolean = true) {
        driver.findElement(By.id("mindTextArea")).run { if (clear) clear();sendKeys(mindText) }
    }
    open fun ObjWithDriver.submitMind () {
        driver.findElement(By.id("mindWindow")).findElement(By.className("btn-primary")).click() }
}