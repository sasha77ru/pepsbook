package ru.sasha77.spring.pepsbook

import org.hamcrest.Matchers
import org.junit.Assert
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*
import javax.servlet.http.Cookie

class TstUser(name: String, email: String, country: String,
              val friendsNames: MutableSet<String> = mutableSetOf(),
              val matesNames: MutableSet<String> = mutableSetOf(),
              val user : User = User(name, email,country)) {
    private fun MutableSet<String>.copy() : MutableSet<String> =
        mutableSetOf<String>().apply {this@copy.forEach { this.add(it) }}
    fun deepCopy() : TstUser =
        TstUser(user.name,user.email,user.country,friendsNames.copy(),matesNames.copy(),user)
}

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
class TestApplicationObject (val mockMvc : MockMvc,
                             val usersRepo: UserRepository,
                             val mindsRepo: MindRepository) {

    lateinit var tstUsersArray : List<TstUser>
    lateinit var actualUsersArray : MutableList<TstUser>
    fun List<TstUser>.getByName(name: String) =
            find { it.user.name == name} ?: throw IllegalArgumentException("No such TSTUSER $name")
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
        set(value) {_currUser = value;keyCookie = Cookie("keyCookie", value.user.keyCookie)}
    var currUserName: String
        get() = currUser.user.name
        set(value) {currUser = actualUsersArray.getByName(value)}
    lateinit var keyCookie : Cookie

    /**
     * Clear DB, than fill it with data from tstUsersArray and tstMindsArray.
     * Than does actualUsersArray = tstUsersArray, actualMindsArray = tstMindsArray
     * if friendship==false then friendship information ignored
     */
    fun fillDB (usersRepo : UserRepository, mindsRepo : MindRepository, friendship : Boolean = true) {

        tstUsersArray =
                listOf(
                        TstUser("Porky",    "porky@pig.com",    "USA",      if (friendship) mutableSetOf("Pluto","Masha") else mutableSetOf()),
                        TstUser("Pluto",    "pluto@dog.com",    "USA",      if (friendship) mutableSetOf("Porky") else mutableSetOf()),
                        TstUser("Masha",    "masha@child.com",  "Russia"),
                        TstUser("Luntik",   "luntik@alien.com", "Russia",   if (friendship) mutableSetOf("Porky") else mutableSetOf())
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
        tstMindsArray.forEach { mindsRepo.save(Mind(it.text, tstUsersArray.getByName(it.user).user)) }
    }

    /**
     * Does checkDB for all users
     */
    fun checkAllDB () {actualUsersArray.forEach {checkDB(it)}}

    /**
     * Performs mvc requests and compare models with actualUsersArray,actualMindsArray
     */
    fun checkDB (currUser: TstUser = this.currUser) {
        val keyCookie = Cookie("keyCookie", currUser.user.keyCookie)
        mockMvc.perform(MockMvcRequestBuilders.get("/users").param("subs","").cookie(keyCookie))
                .andDo { mvcResult ->
                    @Suppress("UNCHECKED_CAST")
                    Assert.assertEquals("Different lists",
                            actualUsersArray.filter { it.user.id != currUser.user.id }.sortedBy { it.user.name }
                                    .joinToString("\n") { it.user.name },
                            (mvcResult.modelAndView!!.model["lizt"] as Iterable<User>).joinToString("\n") { it.name }
                    )
                }
        mockMvc.perform(MockMvcRequestBuilders.get("/friends").param("subs","").cookie(keyCookie))
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
        mockMvc.perform(MockMvcRequestBuilders.get("/mates").param("subs","").cookie(keyCookie))
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
        mockMvc.perform(MockMvcRequestBuilders.get("/minds").param("subs","").cookie(keyCookie))
                .andDo { mvcResult ->
                    @Suppress("UNCHECKED_CAST")
                    Assert.assertEquals("Different lists",
                            actualMindsArray.sortedBy { it.text }.joinToString("\n") { it.text },
                            (mvcResult.modelAndView!!.model["lizt"] as Iterable<Mind>).sortedBy { it.text }
                                    .joinToString("\n") { it.text }
                    )
                }
    }

    fun userByCookie (sc: Int = 200) {
        mockMvc.perform(MockMvcRequestBuilders.get("/rest/userByCookie").cookie(keyCookie))
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
        actualUsersArray.getByName(friendName).matesNames.add(currUserName)

        mockMvc.perform(MockMvcRequestBuilders.patch("/rest/toFriends")
                .param("friend_id", actualUsersArray.getByName(friendName).user.id.toString()).cookie(keyCookie))
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("error"))))
    }

    fun fromFriends (friendName : String) {
        currUser.friendsNames.remove(friendName)
        actualUsersArray.getByName(friendName).matesNames.remove(currUserName)

        mockMvc.perform(MockMvcRequestBuilders.patch("/rest/fromFriends")
                .param("friend_id", actualUsersArray.getByName(friendName).user.id.toString())
                .cookie(keyCookie))
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("error"))))
    }

    fun allUsersSimple() {
        mockMvc.perform(MockMvcRequestBuilders.get("/rest/allUsersSimple").cookie(keyCookie))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .apply {actualUsersArray.forEach {
                    andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
                            "\"name\":\"${it.user.name}\",\"email\":\"${it.user.email}\"")))
                }}
    }

    fun logOff(all : Boolean = false) {
        mockMvc.perform(MockMvcRequestBuilders.patch("/rest/logOff")
                .apply { if (all) param("all","1") }
                .cookie(keyCookie))
                    .andExpect { MockMvcResultMatchers.cookie().maxAge("keyCookie",-1) }
                    .andExpect(MockMvcResultMatchers.status().isOk)
    }

    fun checkUser(login : String, sc: Int = 200) {
        mockMvc.perform(MockMvcRequestBuilders.get("/rest/checkUser").param("login",login))
                .andExpect { MockMvcResultMatchers.cookie().exists("keyCookie") }
                .andExpect(MockMvcResultMatchers.status().`is`(sc))
    }

    fun saveMind (text : String, oldText : String? = null) {
        mockMvc.perform(MockMvcRequestBuilders.post("/rest/saveMind")
                .param("text",text)
                .apply {
                    if (oldText != null) param("id",mindsRepo.findLike(oldText).find { true }!!.id.toString())}
                .cookie(keyCookie))
                    .andExpect(MockMvcResultMatchers.status().isOk)
        if(oldText == null) actualMindsArray.add(TstMind(text,currUserName, Date()))
        else actualMindsArray.getByText(oldText).text = text
    }

    fun removeMind (oldText : String) {
        mockMvc.perform(MockMvcRequestBuilders.delete("/rest/removeMind")
                .param("id",mindsRepo.findLike(oldText).find { true }!!.id.toString())
                .cookie(keyCookie))
                    .andExpect(MockMvcResultMatchers.status().isOk)
        actualMindsArray.remove(actualMindsArray.getByText(oldText))
    }
}
