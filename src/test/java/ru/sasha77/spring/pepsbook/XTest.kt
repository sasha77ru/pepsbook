package ru.sasha77.spring.pepsbook

import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import javax.servlet.http.HttpServletResponse

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = [PepsbookApplication::class])
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-integrationtest.properties"])
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

class XTest {

    @Autowired
    internal lateinit var usersRepo: UserRepository

    @Autowired
    internal lateinit var mindsRepo: MindRepository
    
    @Autowired
    lateinit var tao : TestApplicationObject

    @Test
    @Throws(Exception::class)
    fun tst0001_MvcControllers() {
        with (tao) {
            fillDB(usersRepo, mindsRepo)
            checkAllDB()
        }
    }

    @Test
    @Throws(Exception::class)
    fun tst0002_Friendship() {
        with (tao) {
            fillDB(usersRepo, mindsRepo, friendship = false)
            currUserName = "Porky"

            //Add couple of friends
            listOf("Pluto", "Masha").forEach { toFriends(it) }
            checkAllDB()

            //Remove first friend
            fromFriends("Pluto")
            checkAllDB()

            //Remove second friend
            fromFriends("Masha")
            checkAllDB()

            //Make all friendships and check
            actualUsersArray = tstUsersArray.deepCopy()
            actualUsersArray.forEach { user ->
                currUser = user
                currUser.friendsNames.forEach { friend -> toFriends(friend) }
            }
            checkAllDB()
        }
    }

    @Test
    @Throws(Exception::class)
    fun tst0003_Minds() {
        with (tao) {
            fillDB(usersRepo, mindsRepo)
            currUserName = "Porky"

            saveMind("testText")
            checkAllDB()

            saveMind("anotherText", oldText = "testText")
            checkAllDB()

            removeMind("anotherText")
            checkAllDB()

            fillDB(usersRepo, mindsRepo)
        }
    }

    @Test
    @Throws(Exception::class)
    fun tst0004_LoginLogoff() {
        with (tao) {
            fillDB(usersRepo,mindsRepo)
            currUserName = "Porky"

            allUsersSimple()
            checkUser("porky@pig.com")
            checkUser("wrongLogin",sc = HttpServletResponse.SC_UNAUTHORIZED)
            userByCookie()

            logOff()
            logOff(all = true)
            userByCookie(sc = HttpServletResponse.SC_UNAUTHORIZED)
        }
    }
}

