package ru.sasha77.spring.pepsbook

import com.mongodb.client.MongoClients
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import ru.sasha77.spring.pepsbook.TestApplicationObject.TstUser
import ru.sasha77.spring.pepsbook.models.Answer
import ru.sasha77.spring.pepsbook.models.Mind
import ru.sasha77.spring.pepsbook.models.User
import ru.sasha77.spring.pepsbook.repositories.AnswerRepository
import ru.sasha77.spring.pepsbook.repositories.MessageRepository
import ru.sasha77.spring.pepsbook.repositories.MindRepository
import ru.sasha77.spring.pepsbook.repositories.UserRepository
import ru.sasha77.spring.pepsbook.services.UserService
import java.util.*
import kotlin.random.Random


/**
 * TAO class represents kind of test DB (see testClasses.svg).
 * Store its state in arrays: actualUsersArray<[TstUser]>, actualMindsArray<[TstMind]<[TstAnswer]>>
 * Change state using do... methods
 * Also has clearDB and fillDB that fills arrays with some test data
 */
@Component("Tao")
class TestApplicationObject (private val usersRepo: UserRepository,
                             private val mindsRepo: MindRepository,
                             private val answersRepo: AnswerRepository,
                             private val messageRepo: MessageRepository,
                             private val mongoOperations: MongoOperations) {
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
    lateinit var userService: UserService

    @Autowired
    lateinit var clk : Clickers

    @Autowired
    lateinit var chk : Checkers

    @Autowired
    lateinit var performanceCounter : PerformanceCounter

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Value("\${my.mindsPageSize}") var MINDS_PAGE_SIZE : Int = 0
    @Value("\${my.messagesPageSize}") var MESSAGES_PAGE_SIZE : Int = 0
    @Value("\${my.paginatorMaxSize}") var PAGINATOR_MAX_SIZE : Int = 0
    @Value("\${my.paginatorWide}") var PAGINATOR_WIDE : Int = 0
    @Value("\${spring.data.mongodb.database}") val mongoDBname : String = ""

    var actualUsersArray = mutableListOf<TstUser>() // DATA STORAGE FOR TESTS

    val actualMindsArray = mutableListOf<TstMind>() // DATA STORAGE FOR TESTS

    val actualMessagesArray = mutableListOf<TstMessage>() // DATA STORAGE FOR TESTS

    val actualInterlocutorsArray = mutableSetOf<TstInterlocutor>() // DATA STORAGE FOR TESTS

    fun isFriend (currUser : TstUser, x : TstUser) : Boolean  = currUser.friendsNames.contains(x.name)
    fun isMate (currUser : TstUser, x : TstUser) = currUser.matesNames.contains(x.name)
    fun positive (@Suppress("UNUSED_PARAMETER") x : TstUser) = true //Always returns true. Used for it signature


    /**
     * Clears DB.
     */
    fun clearDB () {
        userService.deleteAll()
        actualUsersArray.clear()
        actualMindsArray.clear()

        //		context.getBean(MyMongoTestRepository.class).deleteAll();
        mongoOperations.getCollection("messages").deleteMany(Document())
        mongoOperations.getCollection("interlocutors").deleteMany(Document())
    }

    /**
     * Clear DB, than fill it with data from tstUsersArray and tstMindsArray.
     * Than does actualUsersArray = tstUsersArray, actualMindsArray = tstMindsArray
     * if randomer is null DB will be filled with constants
     * if randomer (and usedStrings) isn't null DB will be filled with feign data
     * if friendship==false then friendship information ignored
     */
    fun fillDB (friendship : Boolean = true, minds : Boolean = true, randomer : Random? = null, usedStrings : MutableSet<String>? = null) {
        var lmDate = Date(Date().time-86400_0000_000)
        fun mockDate () : Date {
            lmDate = Date(lmDate.time+100_000)
            return lmDate
        }

        clearDB()

        if (randomer!=null) {
            with (object : FeignMixin() { // attach rand and feign funs from the mixin
                override val randomer : Random = randomer
                override val usedStrings : MutableSet<String> = usedStrings!!
            }) {
                // feign users
                actualUsersArray = (0..tstProps.monkey.feignDB.users).map {
                    TstUser(feignString(rand(2, 30)),
                            "${feignUsername()}@${feignUsername()}.${feignUsername(3)}",
                            feignString(rand(3,20)),
                            feignUsername(rand(2, 12)),
                            feignUsername(rand(8, 12)),
                            mutableSetOf())
                }.toMutableList()
//                println(actualUsersArray.map { "${it.name}\t${it.username}\t${it.email}" }.joinToString("\n"))
                // make random friendship bw users
                if (friendship) {
                    actualUsersArray.forEach { user ->
                        repeat (tstProps.monkey.feignDB.friendships) {
                            var name : String
                            do {
                                name = actualUsersArray.random(randomer).name
                            } while (name == user.name)
                            user.friendsNames.add(name)
                        }
                    }
                }
                // fill minds DB
                if (minds) {
                    repeat (tstProps.monkey.feignDB.minds) {
                        actualMindsArray.add(
                            TstMind(feignString(400),actualUsersArray.random(randomer).name,mockDate())
                        )
                    }
                    repeat (tstProps.monkey.feignDB.answers) {
                        actualMindsArray.random(randomer).run {
                            answers.add(TstAnswer(feignString(400), this,
                                actualUsersArray.random(randomer).name, mockDate()))
                        }
                    }
                }
            }
        } else {
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

            actualMindsArray.run {
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
        }

        actualUsersArray.apply {
            //Fill matesNames of TstUser by friendsNames
            if (friendship) forEach { user ->
                user.friendsNames.forEach { friend ->
                    getUserByName(friend).matesNames.add(user.user.name)
                }
            }
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
    fun getDBMindByText(text : String): Mind = mindsRepo.findByTextContaining(text)
    fun getDBAnswerByText(text : String) = answersRepo.findByText(text)!!
    fun getMessageByText(user : String, text : String) = actualMessagesArray.find { it.text == text && it.user == user}
    fun getDBMessageByText(text : String) = messageRepo.findByText(text)!!
    fun getInterlocutor(name : String, whose : String) = actualInterlocutorsArray.find { it.user == name && it.whose == whose }

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
    fun doAddMessage (name : String, whom : String, text: String, date: Date = Date()) {
        actualMessagesArray.add(TstMessage(name,whom,text,date))
        getInterlocutor(name,whom)!!.run {numNewMessages++;hasPreMessages = false}
    }
    fun doRemoveMessage (name : String, text: String) {
        actualMessagesArray.remove(getMessageByText(name,text))
    }
    fun doStartMessaging (name : String, whom : String, date: Date = Date()) {
        actualInterlocutorsArray.add(TstInterlocutor(name,whom,date))
        actualInterlocutorsArray.add(TstInterlocutor(whom,name,date))
    }
    fun doClearInterlocutorState(name : String, whose : String) {
        getInterlocutor(name,whose)?.run {numNewMessages = 0;hasPreMessages = false}
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

