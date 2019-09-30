package ru.sasha77.spring.pepsbook

import junit.framework.Assert.assertEquals
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
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
class ZTest {

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
    }

    @Test fun XXXXXX() {
        tao.fillDB()
        with(driver) {
            get("http://localhost:$port")
            findElement(By.name("username")).sendKeys("porky")
            findElement(By.name("password")).sendKeys("pig")
            findElement(By.id("loginForm")).submit()
            findElement(By.id("mainMenuOthers")).click()
            Thread.sleep(10000000)
        }
    }
}

