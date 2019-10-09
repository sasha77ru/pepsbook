package ru.sasha77.spring.pepsbook

import org.junit.Assert.*
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.lang.RuntimeException
import java.lang.StringBuilder
import kotlin.random.Random

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [PepsbookApplication::class])
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-integrationtest.properties"])
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MonkeyTests {

    @LocalServerPort
    private val port: Int = 0

    @Autowired
    lateinit var tao : TestApplicationObject

    @Autowired
    lateinit var wao : WebApplicationObject

    companion object {
        lateinit var driver : WebDriver
        lateinit var js : JavascriptExecutor

        @BeforeClass @JvmStatic fun startDriver () {
            System.setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe")
            driver = ChromeDriver(ChromeOptions().apply {
                addArguments("headless")
                addArguments("window-size=1200x600")
            })
            js = driver as JavascriptExecutor
        }
        @AfterClass @JvmStatic fun closeDriver () {
//            driver.close() /moved to monkeyTest001
        }
    }

    @Before
    fun initTao () {tao.doMvc = false}

    @Test fun monkeyTest001() {
        MonkeyTestClass(1921767833437235130).go()
        for (round in 1..100) MonkeyTestClass(Random.nextLong().also { println("$round ====================== seed = $it") }).go()
//        MonkeyTestClass(9125321307777107275).go() // on 532 findByText(it) must not be null
        driver.close()
    }

    inner class MonkeyTestClass (seed : Long) {
        var step = 0
        private var loginPassword : LoginPassword? = null
        private var registerInformation : RegisterInformation? = null
        var what : String = ""
        fun changeWhat (x : String) {what = x;filter = ""}
        var filter : String = ""
        var mindWinWhich : String = ""
        var currMindText : String? = null
        var currAnswerText : String? = null

        val randomer = Random(seed)
        fun rand(x : Int = 100) = randomer.nextInt(x)

        fun validFirstChar () = (('a'..'z') + ('A'..'Z')).random(randomer)
        fun validChar () = (('a'..'z') + ('A'..'Z') + ('0'..'9') + '_' + '-').random(randomer)
        fun invalidChar () = "`~!@#№$%^&*()+=[]{};:'\"\\|,.<>/? ".random(randomer)
        fun feignUsername (len : Int = 8, charFrom : () -> Char = ::validChar) = String(mutableListOf(validFirstChar()).
                apply { repeat(len-2) { add(charFrom()) };add(validFirstChar()) }.toCharArray()) //in emails last char also can't be - or _
//        val SyLIST = listOf(" а","Ва","ло","Но","uv"," d"," W")
//        val SyLIST = listOf(" а","Ва","ло","Но","uv"," d","W ")
        val SyLIST = listOf(" А","ва","ло","ем","ок"," У","ы ")
        val usedStrings = mutableSetOf<String>() //to eliminate dups
        /**
         * Feign uniq string (because minds and answers in test must be uniq)
         */
        fun feignString (len : Int = 12) : String = StringBuilder().apply {
            repeat(len/2) { 
                append((SyLIST+listOf(feignUsername(2,::invalidChar))).random(randomer))
            } }.toString()
                .let { if (it in usedStrings) feignString(len) else {usedStrings.add(it);it} } //to eliminate dups
        inner class LoginPassword {
            val login : String
            val password : String
            val ok : Boolean
            init {
                val x = rand()
                when {
                    (x < 90 && tao.actualUsersArray.size > 0) -> {
                        // Valid LoginPassword
                        tao.actualUsersArray.random(randomer).run {
                            this@LoginPassword.login = username
                            this@LoginPassword.password = password
                        }
                        ok = true
                    }
                    else -> { // Invalid LoginPassword
                        this@LoginPassword.login = feignUsername()
                        this@LoginPassword.password = feignUsername()
                        ok = false
                    }
                }
            }
        }
        inner class RegisterInformation {
            var username : String = feignUsername()
            var password : String = feignUsername()
            var repeatPassword : String = password
            var name : String = feignString()
            var email : String = "${feignUsername()}@${feignUsername()}.${feignUsername(3)}"
            var country : String = feignString()
            var ok : Boolean = true
            val errors = mutableListOf<String>()
            init {
                val w = 5
                if (rand()<w) {username = feignUsername(charFrom = ::invalidChar);errors += "usernameErrors"}
                if (rand()<w) {password = feignUsername(charFrom = ::invalidChar);errors += "passwordErrors";repeatPassword = password}
                if (rand()<w) {repeatPassword = feignUsername(charFrom = ::invalidChar);errors += "repeatPasswordErrors"}
                if (rand()<w) {name = feignString(102);errors += "nameErrors"}
                if (rand()<w) {email = feignUsername(charFrom = ::invalidChar);errors += "emailErrors"}
                if (rand()<w) {country = feignString(102);errors += "countryErrors"}
                ok = errors.size == 0
            }
        }
        val visibleMinds
            get() = tao.actualMindsArray.filter { mind ->
                mind.text.contains(filter,true) || mind.user.contains(filter,true)
                        ||  mind.answers.any { it.text.contains(filter,true) }}.sortedByDescending {it.time}
        val ownersVisibleMinds
            get() = visibleMinds.filter { it.user == tao.currName }
        val visibleUsers
            get() = tao.actualUsersArray
                    .asSequence()
                    .filter { it.username != tao.currUserName }
                    .filter { user -> user.name.contains(filter,true) || user.country.contains(filter,true)}
                    .filter (when (what) {
                        "users" -> tao::positive
                        "friends" -> tao::isFriendToCurr
                        "mates" -> tao::isMateToCurr
                        else -> throw RuntimeException("Impossible")})
                    .toList()
        val whatLambda get () = when (what) {
            "minds" -> ::minds
            "users","friends","mates" -> ::users
            else ->  throw RuntimeException("Impossible")
        }

        @Suppress("UNCHECKED_CAST")
        fun go () {
            tao.fillDB()
            driver.get("http://localhost:$port")
            var f = loginForm() as? () -> Any
            while (step++ < 100) {
                print("$step ")
                f = (f?.invoke() ?: break) as () -> Any
                pause(For.SEE)
            }
            pause(For.LONG_LOAD)
            wao.runCatching {driver.clickLogout()}
        }

        fun loginForm () : Any? {
            println("loginForm")
            pause {driver.title == "Pepsbook Login"}
            loginPassword?.run { driver.findElement(By.id("loginErrors")) } // Check the page for error message presence
            if (rand() > 90 && tao.actualUsersArray.size > 0) {
                LoginPassword().run {
                    driver.findElement(By.name("username")).run { clear();sendKeys(login) }
                    driver.findElement(By.name("password")).run { clear();sendKeys(password) }
                    pause(For.SEE)
                    driver.findElement(By.id("loginForm")).submit()
                    return if (ok) {
                        tao.currUserName = login
                        ::minds
                    } else ::loginForm
                }
            } else {
                driver.findElement(By.tagName("a")).click()
                return ::registerForm
            }
        }

        fun registerForm () : Any? {
            println("registerForm")
            pause {driver.title=="Pepsbook registration"}
            registerInformation?.run { // Check the page for error messages presence
                registerInformation!!.errors.forEach { driver.findElement(By.id(it)) }
            }
            RegisterInformation().run {
                driver.findElement(By.name("username")).run { clear();sendKeys(username) }
                driver.findElement(By.name("password")).run { clear();sendKeys(password) }
                driver.findElement(By.name("repeatPassword")).run { clear();sendKeys(repeatPassword) }
                driver.findElement(By.name("name")).run { clear();sendKeys(name) }
                driver.findElement(By.name("email")).run { clear();sendKeys(email) }
                driver.findElement(By.name("country")).run { clear();sendKeys(country) }
                pause(For.SEE)
                driver.findElement(By.id("signInForm")).submit()
                return if (ok) {
                    tao.actualUsersArray.add(tao.TstUser(name,email,country,username,password))
                    tao.currUserName = username
                    ::minds
                } else ::registerForm
            }
        }

        fun minds () : Any? {
            println("minds")
            pause {runCatching {js.executeScript("return subMainReady;") as Boolean}.getOrDefault(false)}
            what = js.executeScript("return nowInMain;") as String
            if (what == "minds") { // If minds in subMain, check page about minds
                assertEquals("Different Minds",
                visibleMinds.joinToString("\n") {it.toString()}
                        .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                        .replace(Regex("\\[ "),"[")
                ,
                driver.findElements(By.className("mindEntity"))
                        .joinToString("\n") { mindEntity ->
                            mindEntity.findElement(By.className("mindText")).text +
                            " / " + mindEntity.findElement(By.className("mindUser")).text +
                            " / " + mindEntity.findElement(By.className("mindTime")).text +
                            " / " + mindEntity.findElements(By.className("answerEntity")).map { answerEntity ->
                                answerEntity.findElement(By.className("answerText")).text +
                                " / " + mindEntity.findElement(By.className("mindText")).text +
                                " / " + answerEntity.findElement(By.className("answerUser")).text +
                                " / " + answerEntity.findElement(By.className("answerTime")).text
                            }.toString()
                        }
                        .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                        .replace(Regex("\\[ "),"[")
                )
            }
            return ::mainPage
        }

        fun users () : Any? {
            println("users what=$what")
            pause {runCatching {js.executeScript("return subMainReady;") as Boolean}.getOrDefault(false)}
            what = js.executeScript("return nowInMain;") as String
            if (what in listOf("users","friends","mates")) { // If "users","friends","mates" in subMain, check page about users
                assertEquals("Different Users",
                    visibleUsers
                        .sortedBy {it.name}
                        .joinToString("\n") {"${it.name} / ${it.country} / ${tao.isFriendToCurr(it)} / ${tao.isMateToCurr(it)}"}
                        .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                    ,
                    driver.findElements(By.className("userEntity"))
                            .joinToString("\n") { userEntity ->
                                userEntity.findElement(By.className("userName")).text +
                                " / " + userEntity.findElement(By.className("userCountry")).text +
                                " / " + (userEntity.findElements(By.className("badge-success")).size > 0
                                        || userEntity.findElements(By.className("badge-primary")).size > 0) +
                                " / " + (userEntity.findElements(By.className("badge-success")).size > 0
                                        || userEntity.findElements(By.className("badge-secondary")).size > 0)
                            }.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                )
            }
            return ::mainPage
        }

        fun mindWin () : Any? {
            println("mindWin which=$mindWinWhich")
            wao.run {driver.run {
                if (rand() < 5) { clickCloseMind();return ::minds }
                if (rand() < 5) {
                    typeMindText(feignString(4002)) //Invalid length
                    submitMind()
                    pause(For.LOAD)
                    assertEquals(1,findElements(By.id("mindErrSign")).size)
                    return ::mindWin
                }
                feignString(rand(400)).let { newText ->
                    typeMindText(newText)
                    submitMind()
                    pause(For.LOAD)
                    if (mindWinWhich == "mind") {
                        if (currMindText != null) {
                            tao.actualMindsArray.find { it.text == currMindText }!!.text = newText
                        } else tao.actualMindsArray.add(TstMind(newText, tao.currName, tao.mindsRepo.findByText(newText).time))
                    } else {
                        val currMind = tao.actualMindsArray.find { it.text == currMindText }!!
                        if (currAnswerText != null) {
                            currMind.answers.find { it.text == currAnswerText }!!.text = newText
                        } else {
                            currMind.answers.add(TstAnswer(newText, currMind, tao.currName, tao.answersRepo.findByText(newText).time))
                        }
                    }
                    return ::minds
                }
            } }
        }

        fun mainPage () : Any? {
            println("mainPage what=$what")
            wao.run {driver.run {
                val caseMatrix = mutableListOf({ clickLogo();changeWhat("minds");::minds});
                { clickMainMinds()  ;changeWhat("minds")    ;::minds    }.also { repeat(1)  {_ -> caseMatrix.add(it)} };
                { clickMainUsers()  ;changeWhat("users")    ;::users    }.also { repeat(3)  {_ -> caseMatrix.add(it)} };
                { clickMainFriends();changeWhat("friends")  ;::users    }.also { repeat(3)  {_ -> caseMatrix.add(it)} };
                { clickMainMates()  ;changeWhat("mates")    ;::users    }.also { repeat(3)  {_ -> caseMatrix.add(it)} };
                { clickLogout()     ;changeWhat("")         ;::loginForm}.also { repeat(3)  {_ -> caseMatrix.add(it)} };
                if (what == "minds" || what == "users") {
                    { typeFilter(SyLIST.random(randomer).also { filter = it });whatLambda}
                            .also { repeat(5)  { _ -> caseMatrix.add(it)} } };
                if (what == "minds" && visibleMinds.isNotEmpty()) {
                    { currMindText = null;mindWinWhich = "mind"
                        clickNewMind();::mindWin }.also { repeat(10)  { _ -> caseMatrix.add(it)} };
                    { currMindText = visibleMinds.random(randomer).text;currAnswerText = null;mindWinWhich = "answer"
                        clickAnswerMind(currMindText!!);::mindWin}.also { repeat(10)  { _ -> caseMatrix.add(it)} };
                    if (ownersVisibleMinds.isNotEmpty()) {
                        { currMindText = ownersVisibleMinds.random(randomer).text;mindWinWhich = "mind"
                            clickEditMind(currMindText!!);::mindWin }.also { repeat(10)  { _ -> caseMatrix.add(it)} };
                        { currMindText = ownersVisibleMinds.random(randomer).text
                            clickDelMind(currMindText!!)
                            tao.actualMindsArray.removeAll { it.text == currMindText };::minds }
                                .also { repeat(10)  { _ -> caseMatrix.add(it)} }
                    }
                } else {
                    //TODO("USERS EDITING")
                }
                return caseMatrix.random(randomer).invoke()
            } }
        }
    }
}

