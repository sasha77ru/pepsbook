package ru.sasha77.spring.pepsbook

import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import javax.servlet.http.HttpServletResponse
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = [PepsbookApplication::class])
@AutoConfigureMockMvc
//@TestPropertySource(locations = ["classpath:application-integrationtest.properties"])
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

class XTest {

    @Autowired
    lateinit var tao : TestApplicationObject

    @Test
    @Throws(Exception::class)
    fun tst0001_MvcControllers() {
        with (tao) {
            fillDB()
            checkAllDB()
        }
    }

    @Test
    @Throws(Exception::class)
    fun tst0002_Friendship() {
        with (tao) {
            fillDB(friendship = false)
            currName = "Porky"

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
            fillDB()
            currName = "Porky"

            saveMind("testText")
            checkAllDB()

            saveMind("anotherText", oldText = "testText")
            checkAllDB()

            removeMind("anotherText")
            checkAllDB()

            fillDB()
        }
    }
//
//    @Test
//    @Throws(Exception::class)
//    fun tst0004_LoginLogoff() {
//        with (tao) {
//            fillDB()
//            currName = "Porky"
//
//            allUsersSimple()
//            checkUser("porky@pig.com")
//            checkUser("wrongLogin",sc = HttpServletResponse.SC_UNAUTHORIZED)
//            getUser()
//
//            logOff()
//            logOff(all = true)
//            getUser(sc = HttpServletResponse.SC_UNAUTHORIZED)
//        }
//    }

    @Test
    @Throws(Exception::class)
    fun tst0000_XXXXXX() {
        with (tao) {
            fillDB()
            currName = "Porky"
            mockMvc.perform(get("/rest/getUser").with(httpBasic("masha", "child")))
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andDo { println(it.response.contentAsString) }
        }
    }
}

