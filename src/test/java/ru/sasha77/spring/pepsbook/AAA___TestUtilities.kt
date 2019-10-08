package ru.sasha77.spring.pepsbook

import org.hamcrest.Matchers
import org.junit.Assert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

data class TstMind(
    var text    : String,
    val user    : String,
    val time    : Date,
    var answers : MutableList<TstAnswer> = mutableListOf()) {
    fun getAnswerByText (findText : String) = answers.find { it.text == findText } ?: throw IllegalArgumentException("No such TSTANSWER $text")
    fun addAnswer (text: String,user: String,time: Date) {answers.add(TstAnswer(text,this,user,time))}
    fun removeAnswer (findText : String) {answers.remove(getAnswerByText(findText))}
}
data class TstAnswer(
    var text : String,
    val mind : TstMind,
    val user : String,
    val time : Date)

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
@Component
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
    lateinit var tstUsersArray : List<TstUser> // an initial test list
    lateinit var actualUsersArray : MutableList<TstUser> // actual test list, initially equals to tstUsersArray but can be changed during tests
    fun List<TstUser>.getByName(name: String) = //fun for tstUsersArray
            find { it.name == name} ?: throw IllegalArgumentException("No such TST_NAME $name")
    fun List<TstUser>.getByUserName(name: String) = //fun for tstUsersArray
            find { it.username == name} ?: throw IllegalArgumentException("No such TST_USERNAME $name")
    fun List<TstUser>.deepCopy() = //fun for tstUsersArray
            mutableListOf<TstUser>().apply {this@deepCopy.forEach { this.add(it.deepCopy()) }}

    lateinit var tstMindsArray : List<TstMind>
    lateinit var actualMindsArray : MutableList<TstMind>
    fun List<TstMind>.getByText(text: String) = //fun for tstMindsArray
            find { it.text == text} ?: throw IllegalArgumentException("No such TSTMIND $text")
    fun List<TstMind>.copy() = //fun for tstMindsArray
            mutableListOf<TstMind>().apply {this@copy.forEach { this.add(it.copy()) }}

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


    /**
     * Clear DB, than fill it with data from tstUsersArray and tstMindsArray.
     * Than does actualUsersArray = tstUsersArray, actualMindsArray = tstMindsArray
     * if friendship==false then friendship information ignored
     */
    fun fillDB (friendship : Boolean = true, minds : Boolean = true) {

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

        tstMindsArray = listOf(
                TstMind("Hru-hru", "Porky", Date())
                        .apply {
                            answers = mutableListOf(
                                    TstAnswer("Вы свинья", this, "Masha", Date()),
                                    TstAnswer("Соласен", this, "Luntik", Date()))
                        },
                TstMind("Gaff-gaff", "Pluto", Date()),
                TstMind("Понятненько", "Masha", Date()),
                TstMind("Я лунная пчела", "Luntik", Date())
        )
        actualMindsArray = tstMindsArray.copy()

//    if (!friendship) actualUsersArray.forEach { it.friendsNames.removeIf { true };it.matesNames.removeIf { true }}

//        mindsRepo.deleteAll()
        /*WANTS TEST without schema, but with ddl-auto=create-drop. UNCOMMENT UPPER ROW. But learn cascades first*/
        usersRepo.deleteAll()

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
        if (minds) tstMindsArray.forEach { tstMind ->
            val mind = Mind(tstMind.text, tstUsersArray.getByName(tstMind.user).user)
                    .apply { answers = tstMind.answers.map { tstAnswer -> //add answers to mind
                        Answer(tstAnswer.text, this, usersRepo.findByName(tstAnswer.user))} }
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
                    if (oldText != null) param("id",answersRepo.findByText(oldText).find { true }!!.id.toString())})
                .andExpect(MockMvcResultMatchers.status().isOk)
        actualMindsArray.getByText(parentMind.text).apply {
            if (oldText == null) addAnswer(text,currName, Date())
            else getAnswerByText(oldText).text = text
        }
    }

    fun removeAnswer (oldText : String, mindText : String) {
        if (doMvc) mockMvc.perform(MockMvcRequestBuilders.delete("/rest/removeAnswer")
                .with(httpBasic(currUser.username, currUser.password)).with(csrf())
                .param("id",answersRepo.findByText(oldText).find { true }!!.id.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk)
        actualMindsArray.getByText(mindText).removeAnswer(oldText)
    }
}
