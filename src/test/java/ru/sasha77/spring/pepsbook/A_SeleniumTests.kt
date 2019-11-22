@file:Suppress("DEPRECATION")

package ru.sasha77.spring.pepsbook

import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import kotlin.math.roundToInt

/**
 * Every method in Selenium Tests click or type something using Clickers
 * than check result using [MvcMockers] for ALL USERS
 * than check result using [Checkers] for current user
 * (see testClasses.svg)
 */
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [PepsbookApplication::class])
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
//@TestPropertySource(locations = ["classpath:application-integrationtest.properties"])
@ActiveProfiles("dev,tst")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ASeleniumTests : ObjWithDriver {

    @LocalServerPort
    private val port: Int = 0

    @Autowired
    lateinit var tao : TestApplicationObject

    //TODO DEBUG
    /*@Autowired
    lateinit*/ var mvc : MvcMockers = mock(MvcMockers::class.java).apply { `when`(checkAllDB()).then { pause(For.LONG_LOAD) } }

    @Autowired
    lateinit var clk : Clickers

    @Autowired
    lateinit var chk : Checkers

    lateinit var wao : WebApplicationObject

    override lateinit var driver : WebDriver

    private var initialized : Boolean = false

    @Before
    fun initialize () {
        if (!initialized) {
            wao = WebApplicationObject(tao,port)
            driver = wao.driver
            initialized = true
        }
    }

    private fun login (login : String, password : String, doNotSetCurrUser : Boolean = false) {
        pause(For.LOAD)
        driver.get("http://localhost:$port")
        pause(For.LOAD)
        driver.findElement(By.name("username")).sendKeys(login)
        driver.findElement(By.name("password")).sendKeys(password)
        driver.findElement(By.id("loginForm")).submit()
        pause(For.LOAD)
        if (!doNotSetCurrUser) wao.currUserName = login
    }

    @Test fun uiTest001_Registration() {
        tao.fillDB()

        driver.get("http://localhost:$port/register")

        assertEquals("Pepsbook registration",driver.title)

        /**
         * Fills registration form
         */
        fun WebDriver.submitForm (username : String, email : String) {
            assertEquals("Pepsbook registration",driver.title)
            findElement(By.name("username")).apply { clear();sendKeys(username) }
            findElement(By.name("password")).apply { clear();sendKeys("aaa") }
            findElement(By.name("repeatPassword")).apply { clear();sendKeys("aaa") }
            findElement(By.name("name")).apply { clear();sendKeys(" Lopeh") }
            findElement(By.name("email")).apply { clear();sendKeys(email) }
            findElement(By.name("country")).apply { clear();sendKeys("USA") }

            findElement(By.tagName("form")).submit()
        }

        with (driver) {
            submitForm("masha","xfvdfv@lkj.com")
            submitForm("loppo","porky@pig.com")
            submitForm("lopeh","lopeh@gmail.com")
        }

        assertEquals("Pepsbook",driver.title)
        Thread.sleep(500)
        //TODO("Get rid of it, use clickLogout")
        driver.findElement(By.id("fakeLogOffButton")).click()
        Thread.sleep(500)
        assertEquals("Pepsbook Login",driver.title)

        login("wrongUserName","wrongUserPass",doNotSetCurrUser = true)
        Thread.sleep(500)
        assertEquals("Pepsbook Login",driver.title)
        assertEquals(1,driver.findElements(By.id("passwordErrors")).size)

        login("masha","wrongUserPass",doNotSetCurrUser = true)
        Thread.sleep(500)
        assertEquals("Pepsbook Login",driver.title)
        assertEquals(1,driver.findElements(By.id("passwordErrors")).size)

        login("lopeh","aaa",doNotSetCurrUser = true)
        Thread.sleep(500)
        assertEquals("Pepsbook",driver.title)
    }

    @Test fun uiTest002_Friendship () {
        tao.fillDB(friendship = false)
        login("porky","pig")
        with(clk) {
            clickMainUsers()
            mvc.checkAllDB()
            chk.run { wao.what="users";wao.checkUsers() }
            //<editor-fold desc="Add users to friends">
            clickUserToFriends("Pluto");tao.doToFriends("Porky","Pluto")
            clickUserToFriends("Luntik");tao.doToFriends("Porky","Luntik")
            clickUserToFriends("Masha");tao.doToFriends("Porky","Masha")
            pause(For.LOAD)
            mvc.checkAllDB()
            chk.run { wao.what="users";wao.checkUsers() }
            //</editor-fold>
            //<editor-fold desc="Remove users from friends">
            clickUserFromFriends("Masha");tao.doFromFriends("Porky","Masha")
            pause(For.LOAD)
            mvc.checkAllDB()
            chk.run { wao.what="users";wao.checkUsers() }
            //</editor-fold>
        }
    }

    @Test fun uiTest003_Minds () {
        tao.fillDB()
        login("masha","child")
        with(clk) {
            clickMainMinds()
            pause(For.LOAD)
            //<editor-fold desc="Not submit a mind">
            clickNewMind()
            typeMindText("Мысля")
            clickCloseMind()
            pause(For.LOAD)
            mvc.checkAllDB()
            chk.run { wao.what="minds";wao.checkMinds() }
            //</editor-fold>
            //<editor-fold desc="Try to submit wrong long mind (takes a lot of time)">
//            clickNewMind()
//            repeat(401) { typeMindText("123456789 ", clear = false) }
//            submitMind()
//            pause(For.LOAD)
//            mvc.checkAllDB()
//            clickCloseMind()
            //</editor-fold>
            //<editor-fold desc="Submit mind">
            clickNewMind()
            typeMindText("Мысля")
            submitMind()
            tao.doAddMind("Masha","Мысля")
            pause(For.LOAD)
            mvc.checkAllDB()
            chk.run { wao.what="minds";wao.checkMinds() }
            //</editor-fold>
            //<editor-fold desc="Edit mind">
            clickEditMind("Мысля")
            typeMindText(" поумнее",clear = false)
            submitMind()
            tao.doChangeMind("Мысля","Мысля поумнее")
            pause(For.LOAD)
            mvc.checkAllDB()
            chk.run { wao.what="minds";wao.checkMinds() }
            //</editor-fold>
            //<editor-fold desc="Delete mind">
            clickDelMind("Мысля поумнее")
            tao.doRemoveMind("Мысля поумнее")
            pause(For.LOAD)
            mvc.checkAllDB()
            chk.run { wao.what="minds";wao.checkMinds() }
            //</editor-fold>
        }
    }

    @Test fun uiTest004_Answers() {
        tao.fillDB()
        login("masha","child")
        with(clk) {
            //take first mind and answer it
            val mindText = driver.findElement(By.className("mindEntity")).findElement(By.className("mindText")).getAttribute("innerHTML")
            //<editor-fold desc="Answer mind">
            clickAnswerMind(mindText)
            typeMindText("Хохохо")
            submitMind()
            tao.doAddAnswer("Masha","Хохохо", mindText)
            mvc.checkAllDB()
            chk.run { wao.what="minds";wao.checkMinds() }
            pause(For.LOAD)
            //</editor-fold>
            //<editor-fold desc="Edit answer">
            clickEditAnswer("Хохохо")
            typeMindText(" - ого",false)
            submitMind()
            tao.doChangeAnswer("Хохохо - ого", mindText, "Хохохо")
            mvc.checkAllDB()
            chk.run { wao.what="minds";wao.checkMinds() }
            pause(For.LOAD)
            //</editor-fold>
            //<editor-fold desc="Delete answer">
            clickDelAnswer("Хохохо - ого")
            tao.doRemoveAnswer("Хохохо - ого",mindText)
            mvc.checkAllDB()
            chk.run { wao.what="minds";wao.checkMinds() }
            //</editor-fold>
        }
    }

    @Test fun xClose () {
        if (tao.tstProps.closeBrowser) driver.close()
    }
}

