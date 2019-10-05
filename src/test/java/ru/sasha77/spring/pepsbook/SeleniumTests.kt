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
            findElement(By.name("username")).apply { clear();sendKeys(username) }
            findElement(By.name("password")).apply { clear();sendKeys("aaa") }
            findElement(By.name("repeatPassword")).apply { clear();sendKeys("aaa") }
            findElement(By.name("name")).apply { clear();sendKeys("Lopeh") }
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
        driver.get("http://localhost:$port/logout")
        assertEquals("Pepsbook Login",driver.title)
    }

    @Test fun uiTest002_Answers() {
        class TheAnswer (val s : String) {
            private fun findAnswer() = driver.findElements(By.className("answerEntity")).
                    find {
                        it.findElement(By.className("answerText")).text == s
                    }!!
            val menuLink get() = findAnswer().findElement(By.className("dropdown-toggle"))
            fun menuItem(x : Int) = findAnswer().findElements(By.className("dropdown-item"))[x]
        }
        with(tao) { doMvc = false;fillDB();currName = "Masha"}
        with(driver) {
            Thread.sleep(5000)
            get("http://localhost:$port")
            Thread.sleep(2000)
            findElement(By.name("username")).sendKeys("masha")
            findElement(By.name("password")).sendKeys("child")
            findElement(By.id("loginForm")).submit()
            Thread.sleep(2000)
            //Press Ответить
            val mindText = findElement(By.className("mindEntity")).findElement(By.className("mindText")).getAttribute("innerHTML")
            findElement(By.className("mindEntity")).findElement(By.className("dropdown-toggle")).click()
            findElement(By.className("mindEntity")).findElements(By.className("dropdown-item"))[0].click()
            //Type text in mindTextArea
            findElement(By.id("mindTextArea")).sendKeys("Хохохо")
            findElement(By.id("mindWindow")).findElement(By.className("btn-primary")).click()
            with(tao) { saveAnswer("Хохохо",mindText);tao.checkDB() }
            Thread.sleep(1000)

            //Press Редактировать
            TheAnswer("Хохохо").menuLink.click()
            TheAnswer("Хохохо").menuItem(0).click()
            //Type text in mindTextArea
            findElement(By.id("mindTextArea")).sendKeys(" - ого")
            findElement(By.id("mindWindow")).findElement(By.className("btn-primary")).click()
            with(tao) { saveAnswer("Хохохо - ого",mindText,"Хохохо");tao.checkDB() }
            Thread.sleep(1000)
            //Press удалить
            TheAnswer("Хохохо - ого").menuLink.click()
            TheAnswer("Хохохо - ого").menuItem(1).click()
            with (tao) {removeAnswer("Хохохо - ого",mindText);checkDB()}
        }
    }
}

