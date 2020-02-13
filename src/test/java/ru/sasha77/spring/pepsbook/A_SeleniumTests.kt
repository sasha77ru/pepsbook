@file:Suppress("DEPRECATION")

package ru.sasha77.spring.pepsbook

import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

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
@ActiveProfiles("dev,tst,tst-simple")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ASeleniumTests : ObjWithDriver {

    @LocalServerPort
    private val port: Int = 0

    @Autowired
    lateinit var tao : TestApplicationObject

    @Autowired
    lateinit var mvc : MvcMockers/* = mock(MvcMockers::class.java).apply { `when`(checkAllDB()).then { pause(For.LOAD) } }*/

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
        with(clk) {clickLogout()}
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
            pause(For.LOAD)
            clickMainUsers()
            mvc.checkAllDB()
            chk.run { wao.what="users";wao.checkUsers() }
            //<editor-fold desc="Add users to friends">
            clickUserToFriends("Pluto");tao.doToFriends("Porky","Pluto");pause(For.LOAD)
            clickUserToFriends("Luntik");tao.doToFriends("Porky","Luntik");pause(For.LOAD)
            clickUserToFriends("Masha");tao.doToFriends("Porky","Masha");pause(For.LOAD)
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
        //Works only with my.mindsPageSize: 2 !!!!!!!!!!!!!
        tao.fillDB()
        login("porky","pig")
        with(clk) {
            //<editor-fold desc="Not submit a mind">
            clickNewMind()
            typeMindText("Мысля")
            clickCloseMind();pause(For.LOAD)
            mvc.checkAllDB()
            chk.run { wao.what="minds";wao.checkMinds() }
            //</editor-fold>
            //<editor-fold desc="Try to submit wrong long mind (takes a lot of time)">
//            clickNewMind()
//            repeat(401) { typeMindText("123456789 ", clear = false) }
//            submitMind();pause(For.LOAD)
//            mvc.checkAllDB()
//            clickCloseMind()
            //</editor-fold>
            //<editor-fold desc="Submit mind">
            clickNewMind()
            typeMindText("Мысля")
            submitMind();pause(For.LOAD)
            tao.doAddMind("Porky","Мысля")
            mvc.checkAllDB()
            clickPaginator(1);pause(For.LOAD);chk.run { wao.mindsPage = 1;wao.what="minds";wao.checkMinds() }
            clickPaginator(0);pause(For.LOAD);chk.run { wao.mindsPage = 0;wao.what="minds";wao.checkMinds() }
            //</editor-fold>
            //<editor-fold desc="Edit mind">
            clickEditMind("Мысля")
            typeMindText(" поумнее",clear = false)
            submitMind();pause(For.LOAD)
            tao.doChangeMind("Мысля","Мысля поумнее")
            mvc.checkAllDB()
            clickPaginator(1);pause(For.LOAD);chk.run { wao.mindsPage = 1;wao.what="minds";wao.checkMinds() }
            clickPaginator(0);pause(For.LOAD);chk.run { wao.mindsPage = 0;wao.what="minds";wao.checkMinds() }
            //</editor-fold>
            //<editor-fold desc="Delete mind">
            clickPaginator(1);pause(For.LOAD);chk.run { wao.mindsPage = 1;wao.what="minds";wao.checkMinds() }
            clickDelMind("Hru-hru")
            tao.doRemoveMind("Hru-hru")
            pause(For.LOAD)
            chk.run { wao.mindsPage = 1;wao.what="minds";wao.checkMinds() }
            mvc.checkAllDB()
            //</editor-fold>
            //<editor-fold desc="Pagination">
            clickPaginator(1);pause(For.LOAD)
            wao.mindsPage = 1
            chk.run { wao.what="minds";wao.checkMinds() }
            clickPaginator(0);pause(For.LOAD)
            wao.mindsPage = 0
            chk.run { wao.what="minds";wao.checkMinds() }
            clickPaginator(-2);pause(For.LOAD)
            wao.mindsPage = 1
            chk.run { wao.what="minds";wao.checkMinds() }
            clickPaginator(-1);pause(For.LOAD)
            wao.mindsPage = 0
            chk.run { wao.what="minds";wao.checkMinds() }
            //</editor-fold>

        }
    }


    @Test fun uiTest003_Minds_2 () {
        //Works only with my.mindsPageSize: 2 !!!!!!!!!!!!!
        tao.fillDB()
        login("masha", "child")
        with(clk) {
            //<editor-fold desc="Submit mind">
            (2..4).forEach {
                clickNewMind()
                typeMindText("Мысля $it")
                submitMind();pause(For.LOAD)
                tao.doAddMind("Masha", "Мысля $it")
                //check if after adding a new mind page goes to 0
                chk.run { wao.mindsPage = 0;wao.what = "minds";wao.checkMinds() }
                if (it > 2) {
                    clickPaginator(1);pause(For.LOAD)
                    chk.run { wao.mindsPage = 1;wao.what = "minds";wao.checkMinds() }
                }
            }
            mvc.checkAllDB()
            //</editor-fold>
            //<editor-fold desc="Edit mind">
            clickEditMind("Мысля 2")
            typeMindText(" поумнее", clear = false)
            submitMind();pause(For.LOAD)
            tao.doChangeMind("Мысля 2", "Мысля 2 поумнее")
            mvc.checkAllDB()
            //</editor-fold>
            clickPaginator(1);pause(For.LOAD)

            clickDelMind("Понятненько")
            tao.doRemoveMind("Понятненько")
            pause(For.LOAD)
            clickDelMind("Мысля 2 поумнее")
            tao.doRemoveMind("Мысля 2 поумнее")
            pause(For.LOAD)
            //check that if empty page goes to previous
            chk.run { wao.mindsPage = 0;wao.what = "minds";wao.checkMinds() }
            mvc.checkAllDB()
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
            tao.doAddAnswer("Masha","Хохохо", mindText);pause(For.LOAD)
            mvc.checkAllDB()
            chk.run { wao.what="minds";wao.checkMinds() }
            pause(For.LOAD)
            //</editor-fold>
            //<editor-fold desc="Edit answer">
            clickEditAnswer("Хохохо")
            typeMindText(" - ого",false)
            submitMind()
            tao.doChangeAnswer("Хохохо - ого", mindText, "Хохохо");pause(For.LOAD)
            mvc.checkAllDB()
            chk.run { wao.what="minds";wao.checkMinds() }
            pause(For.LOAD)
            //</editor-fold>
            //<editor-fold desc="Answer answer">
            clickAnswerAnswer("Хохохо - ого")
            typeMindText("Ответ",false)
            submitMind()
            tao.doAddAnswer("Masha","@Masha Ответ", mindText)
            mvc.checkAllDB()
            chk.run { wao.what="minds";wao.checkMinds() }
            pause(For.LOAD)
            //</editor-fold>
            //<editor-fold desc="Delete answer">
            clickDelAnswer("Хохохо - ого")
            tao.doRemoveAnswer("Хохохо - ого",mindText);pause(For.LOAD)
            mvc.checkAllDB()
            chk.run { wao.what="minds";wao.checkMinds() }
            //</editor-fold>
        }
    }

    @Test fun xClose () {
        if (tao.tstProps.closeBrowser) driver.close()
    }
}

