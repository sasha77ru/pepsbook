package ru.sasha77.spring.pepsbook

import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import javax.servlet.http.HttpServletResponse
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [PepsbookApplication::class])
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
//@TestPropertySource(locations = ["classpath:application-integrationtest.properties"])
@ActiveProfiles("dev,tst")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

class A_BackEndTests {
    @Autowired
    lateinit var mvc : MvcMockers

    @Autowired
    lateinit var tao : TestApplicationObject

    companion object {
        @AfterClass
        @JvmStatic
        fun afterClass() {
            Thread.sleep(2000)
        }
    }

    @Test
    @Throws(Exception::class)
    fun tst0001_MvcControllers() {
        with (tao) {
            fillDB()
            mvc.checkAllDB()
            mvc.checkDB("Masha","ла")
            mvc.checkDB("Pluto","ash")
        }
    }

    @Test
    @Throws(Exception::class)
    fun tst0002_Friendship() {
        with (tao) {
            //Fill Db wo friendship
            fillDB(friendship = false)
            val currName = "Porky"

            //Add couple of friends
            listOf("Pluto", "Masha").forEach { mvc.mvcToFriends(currName,it) }
            mvc.checkAllDB()

            //Remove first friend
            mvc.mvcFromFriends(currName,"Pluto")
            mvc.checkAllDB()

            //Remove second friend
            mvc.mvcFromFriends(currName,"Masha")
            mvc.checkAllDB()

            //Make all friendships and check
            fillDB(friendship = false)
            actualUsersArray.forEach { user ->
                user.friendsNames.forEach { friend -> mvc.mvcToFriends(currName,friend) }
            }
            mvc.checkAllDB()
        }
    }

    @Test
    @Throws(Exception::class)
    fun tst0003_Minds() {
        with (tao) {
            fillDB()
            val currName = "Porky"

            mvc.mvcAddMind(currName,"testText")
            mvc.checkAllDB()

            mvc.mvcChangeMind(currName, "anotherText", "testText")
            mvc.checkAllDB()

            mvc.mvcRemoveMind(currName, "anotherText")
            mvc.checkAllDB()

            fillDB()
        }
    }

    @Test
    @Throws(Exception::class)
    fun tst0004_Answers() {
        with (tao) {
            fillDB()
            val currName = "Porky"

            mvc.mvcAddMind(currName, "Master Mind")

            mvc.mvcAddAnswer(currName, "MM Answer","Master Mind")
            mvc.checkAllDB()

            mvc.mvcChangeAnswer(currName, "anotherText", "Master Mind","MM Answer")
            mvc.checkAllDB()

            mvc.mvcRemoveAnswer(currName, "anotherText", "Master Mind")
            mvc.checkAllDB()

            fillDB()
        }
    }
}

