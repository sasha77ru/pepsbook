package ru.sasha77.spring.pepsbook

import junit.framework.Assert.assertEquals
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.math.roundToInt

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [PepsbookApplication::class])
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
//@TestPropertySource(locations = ["classpath:application-integrationtest.properties"])
@ActiveProfiles("dev,tst")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class A_SeleniumTests : ObjWithDriver {

    @LocalServerPort
    private val port: Int = 0

    @Autowired
    lateinit var tao : TestApplicationObject

    @Autowired
    lateinit var mvc : MvcMockers

    @Autowired
    lateinit var clkDelMEE : Clickers

    override lateinit var driver : WebDriver

    var initialized : Boolean = false

    @Before
    fun initialize () {
        if (!initialized) {
            System.setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe")
            driver = ChromeDriver(ChromeOptions().apply {
                if (tao.tstProps.headLess) addArguments("headless")
                addArguments("window-size=1200x600")
            })
            initialized = true
        }
    }

    private fun login (login : String, password : String) {
        pause(For.LOAD)
        driver.get("http://localhost:$port")
        pause(For.LOAD)
        driver.findElement(By.name("username")).sendKeys(login)
        driver.findElement(By.name("password")).sendKeys(password)
        driver.findElement(By.id("loginForm")).submit()
        pause(For.LOAD)
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
            submitForm("lopeh" + (Math.random() * 100).roundToInt(),"lopeh" + (Math.random() * 100).roundToInt() + "@gmail.com")
        }

        assertEquals("Pepsbook",driver.title)
        Thread.sleep(500)
        //TODO("Get rid of it, use clickLogout")
        driver.findElement(By.id("fakeLogOffButton")).click()
        Thread.sleep(500)
        assertEquals("Pepsbook Login",driver.title)
    }

    @Test fun uiTest002_Friendship () {
        tao.fillDB(friendship = false)
        login("porky","pig")
        with(clkDelMEE) {
            clickMainUsers()
            mvc.checkAllDB()
            clickUserToFriends("Pluto");tao.doToFriends("Porky","Pluto")
            clickUserToFriends("Luntik");tao.doToFriends("Porky","Luntik")
            clickUserToFriends("Masha");tao.doToFriends("Porky","Masha")
            pause(For.LOAD)
            mvc.checkAllDB()

            clickUserFromFriends("Masha");tao.doFromFriends("Porky","Masha")
            pause(For.LOAD)
            mvc.checkAllDB()
        }
    }

    @Test fun uiTest003_Minds () {
        tao.fillDB()
        login("masha","child")
        with(clkDelMEE) {
            clickMainMinds()
            pause(For.LOAD)
            //<editor-fold desc="Not submit a mind">
            clickNewMind()
            typeMindText("Мысля")
            clickCloseMind()
            pause(For.LOAD)
            mvc.checkAllDB()
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
            //</editor-fold>
            //<editor-fold desc="Edit mind">
            clickEditMind("Мысля")
            typeMindText(" поумнее",clear = false)
            submitMind()
            tao.doChangeMind("Мысля","Мысля поумнее")
            pause(For.LOAD)
            mvc.checkAllDB()
            //</editor-fold>
            //<editor-fold desc="Delete mind">
            clickDelMind("Мысля поумнее")
            tao.doRemoveMind("Мысля поумнее")
            pause(For.LOAD)
            mvc.checkAllDB()
            //</editor-fold>
        }
    }

    @Test fun uiTest004_Answers() {
        tao.fillDB()
        login("masha","child")
        with(clkDelMEE) {
            //take first mind and answer it
            val mindText = driver.findElement(By.className("mindEntity")).findElement(By.className("mindText")).getAttribute("innerHTML")
            clickAnswerMind(mindText)
            typeMindText("Хохохо")
            submitMind()
            tao.doAddAnswer("Masha","Хохохо", mindText)
            mvc.checkAllDB()
            pause(For.LOAD)

            clickEditAnswer("Хохохо")
            typeMindText(" - ого",false)
            submitMind()
            tao.doChangeAnswer("Хохохо - ого", mindText, "Хохохо")
            mvc.checkAllDB()
            pause(For.LOAD)

            clickDelAnswer("Хохохо - ого")
            tao.doRemoveAnswer("Хохохо - ого",mindText)
            mvc.checkAllDB()
        }
    }

    @Test fun xClose () {
        if (tao.tstProps.closeBrowser) driver.close()
    }
}

