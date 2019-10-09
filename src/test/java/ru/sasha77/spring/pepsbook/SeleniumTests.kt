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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import javax.servlet.http.HttpSession
import kotlin.math.roundToInt

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [PepsbookApplication::class])
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-integrationtest.properties"])
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class SeleniumTests {

    @LocalServerPort
    private val port: Int = 0

    @Autowired
    lateinit var tao : TestApplicationObject

    @Autowired
    lateinit var wao : WebApplicationObject

    companion object {
        lateinit var driver : WebDriver

        @BeforeClass @JvmStatic fun startDriver () {
            System.setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe")
            driver = ChromeDriver(ChromeOptions().apply {
//                addArguments("headless")
                addArguments("window-size=1200x600")
            })
        }
        @AfterClass @JvmStatic fun closeDriver () {
            driver.close()
        }
    }

    @Before
    fun initTao () {tao.doMvc = false}

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

    @Test fun uiTest002_Answers() {
        class TheAnswer (val s : String) {
            private fun findAnswer() = driver.findElements(By.className("answerEntity")).
                    find {
                        it.findElement(By.className("answerText")).text == s
                    }!!
            val menuLink get() = findAnswer().findElement(By.className("dropdown-toggle"))
            fun menuItem(className : String) = findAnswer().findElement(By.className(className))
        }
        with(tao) { doMvc = false;fillDB();currName = "Masha"}
        with(wao) {
            with (driver) {
                pause(For.LOAD)
                get("http://localhost:$port")
                pause(For.LOAD)
                findElement(By.name("username")).sendKeys("masha")
                findElement(By.name("password")).sendKeys("child")
                findElement(By.id("loginForm")).submit()
                pause(For.LOAD)

                //take first mind and answer it
                val mindText = findElement(By.className("mindEntity")).findElement(By.className("mindText")).getAttribute("innerHTML")
                clickAnswerMind(mindText)
                typeMindText("Хохохо")
                submitMind()
                tao.run { saveAnswer("Хохохо", mindText);tao.checkDB() }
                pause(For.LOAD)

                clickEditAnswer("Хохохо")
                typeMindText(" - ого",false)
                submitMind()
                tao.run { saveAnswer("Хохохо - ого", mindText, "Хохохо");checkDB() }
                pause(For.LOAD)

                clickDelAnswer("Хохохо - ого")
                tao.run { removeAnswer("Хохохо - ого", mindText);checkDB() }
            }
        }
    }
}

