package ru.sasha77.spring.pepsbook

import org.junit.AfterClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [PepsbookApplication::class])
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
//@TestPropertySource(locations = ["classpath:application-integrationtest.properties"])
@ActiveProfiles("dev,tst,tst-simple")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

class ABackEndTests {
    @Autowired lateinit var mvc : MvcMockers
    @Autowired lateinit var tao : TestApplicationObject

    companion object {
        @AfterClass
        @JvmStatic
        fun afterClass() {
            Thread.sleep(1000)
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
            mvc.checkMinds(currName, "", page = 1, size = MINDS_PAGE_SIZE)

            mvc.checkMinds(currName, "h")
            mvc.checkMinds(currName, "", page = 1, deceivePage = 2, size = MINDS_PAGE_SIZE)

            val log = LoggerFactory.getLogger(ABackEndTests::class.java)!!.also { it.debug("========") }
            mvc.checkMinds(currName, "", page = 0, size = MINDS_PAGE_SIZE, log = log)
            mvc.checkMinds(currName, "", page = 0, size = MINDS_PAGE_SIZE, log = log)
            mvc.checkMinds(currName, "", page = 0, size = MINDS_PAGE_SIZE, log = log)
            mvc.checkMinds(currName, "", page = 0, size = MINDS_PAGE_SIZE, log = log)
            mvc.checkMinds(currName, "", page = 0, size = MINDS_PAGE_SIZE, log = log)
            mvc.checkMinds(currName, "", page = 0, size = MINDS_PAGE_SIZE, log = log)
            mvc.checkMinds(currName, "", page = 0, size = MINDS_PAGE_SIZE, log = log)
            mvc.checkMinds(currName, "", page = 0, size = MINDS_PAGE_SIZE, log = log)
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

