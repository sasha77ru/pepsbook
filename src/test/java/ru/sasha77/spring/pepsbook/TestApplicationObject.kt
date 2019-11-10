package ru.sasha77.spring.pepsbook

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import ru.sasha77.spring.pepsbook.models.Answer
import ru.sasha77.spring.pepsbook.models.Mind
import ru.sasha77.spring.pepsbook.models.User
import ru.sasha77.spring.pepsbook.repositories.AnswerRepository
import ru.sasha77.spring.pepsbook.repositories.MindRepository
import ru.sasha77.spring.pepsbook.repositories.UserRepository
import java.util.*


/**
 * TAO class represents kind of test DB (see testClasses.svg).
 * Store its state in arrays: actualUsersArray<[TstUser]>, actualMindsArray<[TstMind]<[TstAnswer]>>
 * Change state using do... methods
 * Also has clearDB and fillDB that fills arrays with some test data
 */
@Component("Tao")
class TestApplicationObject (private val usersRepo: UserRepository,
                             private val mindsRepo: MindRepository,
                             private val answersRepo: AnswerRepository) {
    inner class TstUser(
            val name: String,
            val email: String,
            val country: String,
            val username : String,
            val password : String,
            val friendsNames: MutableSet<String> = mutableSetOf(),
            val matesNames: MutableSet<String> = mutableSetOf(),
            val user : User = User(name, email, country, username, passwordEncoder.encode(password)))

    @Autowired
    lateinit var tstProps: TstProps

    @Autowired
    lateinit var clk : Clickers

    @Autowired
    lateinit var chk : Checkers

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    var actualUsersArray = mutableListOf<TstUser>() // DATA STORAGE FOR TESTS

    val actualMindsArray = mutableListOf<TstMind>() // DATA STORAGE FOR TESTS

    fun isFriend (currUser : TstUser, x : TstUser) : Boolean  = currUser.friendsNames.contains(x.name)
    fun isMate (currUser : TstUser, x : TstUser) = currUser.matesNames.contains(x.name)
    fun positive (@Suppress("UNUSED_PARAMETER") x : TstUser) = true //Always returns true. Used for it signature


    /**
     * Clears DB.
     */
    fun clearDB () {
        usersRepo.deleteAll()
        actualUsersArray.clear()
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

        actualUsersArray =
                mutableListOf(
                        TstUser("Porky", "porky@pig.com", "USA", "porky", "pig",
                                if (friendship) mutableSetOf("Pluto", "Masha") else mutableSetOf()),
                        TstUser("Pluto", "pluto@dog.com", "USA", "pluto", "dog",
                                if (friendship) mutableSetOf("Porky") else mutableSetOf()),
                        TstUser("Masha", "masha@child.com", "Russia", "masha", "child"),
                        TstUser("Luntik", "luntik@alien.com", "Russia", "luntik", "alien",
                                if (friendship) mutableSetOf("Porky") else mutableSetOf())
                )
        actualUsersArray.apply {
            //Fill matesNames of TstUser by friendsNames
            if (friendship) forEach { user ->
                user.friendsNames.forEach { friend ->
                    getUserByName(friend).matesNames.add(user.user.name)
                }
            }
        }

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

        actualUsersArray.forEach { usersRepo.save(it.user) } // Save users
        actualUsersArray.forEach { theUser ->
            //Fill friendship info to DB
            if (friendship) {
                theUser.friendsNames.map { getUserByName(it) }.forEach { friend ->
                    theUser.user.friends.add(friend.user)
                }
            }
            usersRepo.save(theUser.user)
        }
        //save minds
        if (minds) actualMindsArray.forEach { tstMind ->
            val mind = Mind(tstMind.text, getUserByName(tstMind.user).user, tstMind.time)
                    .apply { answers = tstMind.answers.map { tstAnswer -> //add answers to mind
                        Answer(tstAnswer.text, this, usersRepo.findByName(tstAnswer.user), tstAnswer.time)
                    } }
            mindsRepo.save(mind)
            answersRepo.saveAll(mind.answers)
        }
    }

    fun getUserByName(name : String) = actualUsersArray.find { it.name == name} ?: throw IllegalArgumentException("No such TST_NAME $name")
    fun getUserByLogin(name : String) = actualUsersArray.find { it.username == name} ?: throw IllegalArgumentException("No such TST_USERNAME $name")
    fun getMindByText(text : String) = actualMindsArray.find { it.text == text} ?: throw IllegalArgumentException("No such TSTMIND $text")
    fun getDBMindByText(text : String) = mindsRepo.findLike(text).find { true }!!
    fun getDBAnswerByText(text : String) = answersRepo.findByText(text)!!

    fun doToFriends (name : String, friendName : String) {
        getUserByName(name).friendsNames.add(friendName)
        getUserByName(friendName).matesNames.add(name)
    }
    fun doFromFriends (name : String, friendName : String) {
        getUserByName(name).friendsNames.remove(friendName)
        getUserByName(friendName).matesNames.remove(name)
    }
    fun doAddMind (name : String, text: String, date: Date = Date()) {
        actualMindsArray.add(TstMind(text,name, date))
    }
    fun doChangeMind (oldText : String, newText : String) {
        getMindByText(oldText).text = newText
    }
    fun doRemoveMind (mindText : String) {
        actualMindsArray.remove(getMindByText(mindText))
    }
    fun doChangeAnswer (text : String, mindText : String, oldText : String) {
        getMindByText(mindText).changeAnswer(oldText,text)
    }
    fun doAddAnswer (name : String, text : String, mindText : String) {
        getMindByText(mindText).addAnswer(text,name, Date())
    }
    fun doRemoveAnswer (answerText : String, mindText: String) {
        getMindByText(mindText).removeAnswer(answerText)
    }

//    fun getUser (sc: Int = 200) {
//        mockMvc.perform(MockMvcRequestBuilders.get("/rest/getUser")
//                .with(httpBasic(currUser.username, currUser.password)).with(csrf()))
//    //                .andDo{
//    //                    println("!${it.response.contentAsString}!")
//    //                }
//                    .run {
//                        if (sc == 200) {
//                            andExpect(MockMvcResultMatchers.status().isOk)
//                            andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
//                                    "{\"id\":${currUser.user.id}," +
//                                            "\"name\":\"${currUser.user.name}\"," +
//                                            "\"email\":\"${currUser.user.email}\"}")))
//                        } else andExpect(MockMvcResultMatchers.status().`is`(sc))
//                    }
//    }

}

