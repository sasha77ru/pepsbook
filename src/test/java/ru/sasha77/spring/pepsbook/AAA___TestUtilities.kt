package ru.sasha77.spring.pepsbook

import org.hamcrest.Matchers
import org.junit.Assert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

data class TstMind(var text: String,
              val user: String,
              val time: Date)

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
                             val mindsRepo: MindRepository) {
    inner class TstUser(val name: String,
                  val email: String,
                  val country: String,
                  val username : String,
                  val password : String,
                  val friendsNames: MutableSet<String> = mutableSetOf(),
                  val matesNames: MutableSet<String> = mutableSetOf(),
                  val user : User = User(name, email, country, username, passwordEncoder.encode(password))
    ) {
        private fun MutableSet<String>.copy() : MutableSet<String> =
                mutableSetOf<String>().apply {this@copy.forEach { this.add(it) }}
        fun deepCopy() : TstUser =
                TstUser(user.name,user.email,user.country,username, password,friendsNames.copy(),matesNames.copy(), user = user)
    }

    @Autowired
    lateinit var mockMvc : MockMvc

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    lateinit var tstUsersArray : List<TstUser>
    lateinit var actualUsersArray : MutableList<TstUser>
    fun List<TstUser>.getByName(name: String) =
            find { it.name == name} ?: throw IllegalArgumentException("No such TST_NAME $name")
    fun List<TstUser>.getByUserName(name: String) =
            find { it.username == name} ?: throw IllegalArgumentException("No such TST_USERNAME $name")
    fun List<TstUser>.deepCopy() =
            mutableListOf<TstUser>().apply {this@deepCopy.forEach { this.add(it.deepCopy()) }}

    lateinit var tstMindsArray : List<TstMind>
    lateinit var actualMindsArray : MutableList<TstMind>
    fun List<TstMind>.getByText(text: String) =
            find { it.text == text} ?: throw IllegalArgumentException("No such TSTMIND $text")
    fun List<TstMind>.copy() =
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
                        TstUser("Porky",    "porky@pig.com",    "USA",  "porky", "pig",
                                if (friendship) mutableSetOf("Pluto","Masha") else mutableSetOf()),
                        TstUser("Pluto",    "pluto@dog.com",    "USA", "pluto", "dog",
                                if (friendship) mutableSetOf("Porky") else mutableSetOf()),
                        TstUser("Masha",    "masha@child.com",  "Russia", "masha", "child"),
                        TstUser("Luntik",   "luntik@alien.com", "Russia", "luntik", "alien",
                                if (friendship) mutableSetOf("Porky") else mutableSetOf())
                ).apply {
                    //Fill matesNames of TstUser by friendsNames
                    if (friendship) forEach { user -> user.friendsNames.forEach { friend ->
                        getByName(friend).matesNames.add(user.user.name) }}
                }
        actualUsersArray = tstUsersArray.deepCopy()

        tstMindsArray = listOf(
                TstMind("Hru-hru","Porky", Date()),
                TstMind("Gaff-gaff","Pluto",Date()),
                TstMind("Понятненько","Masha",Date()),
                TstMind("Я лунная пчела","Luntik",Date())
        )
        actualMindsArray = tstMindsArray.copy()

//    if (!friendship) actualUsersArray.forEach { it.friendsNames.removeIf { true };it.matesNames.removeIf { true }}

//        mindsRepo.deleteAll()
        /*WANTS TEST without schema, but with ddl-auto=create-drop. UNCOMMENT UPPER ROW. But learn cascades first*/
        usersRepo.deleteAll()

        tstUsersArray.forEach {usersRepo.save(it.user)}
        tstUsersArray.forEach {theUser ->
            //Fill friendship info to DB
            if (friendship) {
                theUser.friendsNames.map { tstUsersArray.getByName(it) }.forEach { friend ->
                    theUser.user.friends.add(friend.user)
                }
            }
            usersRepo.save(theUser.user)
        }
        if (minds) tstMindsArray.forEach { mindsRepo.save(Mind(it.text, tstUsersArray.getByName(it.user).user)) }
    }

    /**
     * Does checkDB for all users
     */
    fun checkAllDB () {actualUsersArray.forEach {checkDB(it)}}

    /**
     * Performs mvc requests and compare models with actualUsersArray,actualMindsArray
     */
    fun checkDB (currUser: TstUser = this.currUser) {
        mockMvc.perform(MockMvcRequestBuilders.get("/users")
                .param("subs","")
                .with(httpBasic(currUser.username, currUser.password)))
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
                .with(httpBasic(currUser.username, currUser.password)))
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
                .with(httpBasic(currUser.username, currUser.password)))
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
                .with(httpBasic(currUser.username, currUser.password)))
                    .andDo { mvcResult ->
                        @Suppress("UNCHECKED_CAST")
                        Assert.assertEquals("Different lists",
                                actualMindsArray.sortedBy { it.text }.joinToString("\n") { it.text },
                                (mvcResult.modelAndView!!.model["lizt"] as Iterable<Mind>).sortedBy { it.text }
                                        .joinToString("\n") { it.text }
                        )
                    }
    }

    fun getUser (sc: Int = 200) {
        mockMvc.perform(MockMvcRequestBuilders.get("/rest/getUser")
                .with(httpBasic(currUser.username, currUser.password)))
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

        mockMvc.perform(MockMvcRequestBuilders.patch("/rest/toFriends")
                .with(httpBasic(currUser.username, currUser.password))
                .param("friend_id", actualUsersArray.getByName(friendName).user.id.toString()))
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("error"))))
    }

    fun fromFriends (friendName : String) {
        currUser.friendsNames.remove(friendName)
        actualUsersArray.getByName(friendName).matesNames.remove(currName)

        mockMvc.perform(MockMvcRequestBuilders.patch("/rest/fromFriends")
                .with(httpBasic(currUser.username, currUser.password))
                .param("friend_id", actualUsersArray.getByName(friendName).user.id.toString()))
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("error"))))
    }

    fun saveMind (text : String, oldText : String? = null) {
        mockMvc.perform(MockMvcRequestBuilders.post("/rest/saveMind")
                .with(httpBasic(currUser.username, currUser.password))
                .param("text",text)
                .apply {
                    if (oldText != null) param("id",mindsRepo.findLike(oldText).find { true }!!.id.toString())})
                    .andExpect(MockMvcResultMatchers.status().isOk)
        if(oldText == null) actualMindsArray.add(TstMind(text,currName, Date()))
        else actualMindsArray.getByText(oldText).text = text
    }

    fun removeMind (oldText : String) {
        mockMvc.perform(MockMvcRequestBuilders.delete("/rest/removeMind")
                .with(httpBasic(currUser.username, currUser.password))
                .param("id",mindsRepo.findLike(oldText).find { true }!!.id.toString()))
                    .andExpect(MockMvcResultMatchers.status().isOk)
        actualMindsArray.remove(actualMindsArray.getByText(oldText))
    }
}
