package ru.sasha77.spring.pepsbook

import org.junit.Before
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [PepsbookApplication::class])
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-integrationtest.properties"])
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

class XTest {
    @Autowired
    lateinit var mvc : MvcMockers

    @Autowired
    lateinit var tao : TestApplicationObject

    @Before
    fun initTao () {tao.doMvc = true}

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
            //Fill Db wo friendship
            fillDB(friendship = false)
            currName = "Porky"

            //Add couple of friends
            listOf("Pluto", "Masha").forEach { mvc.mvcToFriends(currName,it) }
            checkAllDB()

            //Remove first friend
            mvc.mvcFromFriends(currName,"Pluto")
            checkAllDB()

            //Remove second friend
            mvc.mvcFromFriends(currName,"Masha")
            checkAllDB()

            //Make all friendships and check
            actualUsersArray = tstUsersArray.deepCopy()
            actualUsersArray.forEach { user ->
                currUser = user
                currUser.friendsNames.forEach { friend -> mvc.mvcToFriends(currName,friend) }
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

            mvc.mvcAddMind(currName,"testText")
            checkAllDB()

            mvc.mvcChangeMind(currName, "anotherText", "testText")
            checkAllDB()

            mvc.mvcRemoveMind(currName, "anotherText")
            checkAllDB()

            fillDB()
        }
    }

    @Test
    @Throws(Exception::class)
    fun tst0004_Answers() {
        with (tao) {
            fillDB()
            currName = "Porky"

            mvc.mvcAddMind(currName, "Master Mind")

            mvc.mvcAddAnswer(currName, "MM Answer","Master Mind")
            checkAllDB()

            mvc.mvcChangeAnswer(currName, "anotherText", "Master Mind","MM Answer")
            checkAllDB()

            mvc.mvcRemoveAnswer(currName, "anotherText", "Master Mind")
            checkAllDB()

            fillDB()
        }
    }
}

