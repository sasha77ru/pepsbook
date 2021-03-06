package ru.sasha77.spring.pepsbook

import org.junit.Assert
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.slf4j.LoggerFactory
import kotlin.random.Random

/**
 * WAO Class represents one web session for Monkey Test (see testClasses.svg, monkey.svg).
 * Has its state in set of global variables
 */
class WebApplicationObject (val tao : TestApplicationObject, val port : Int) : ObjWithDriver {
    val clk = tao.clk // get Clickers from TAO
    val chk = tao.chk // get Checkers from TAO
    override val driver : WebDriver
    val js : JavascriptExecutor

    // One field with several not-null getters and setters
    private var _currUser : TestApplicationObject.TstUser? = null
    private var currUser : TestApplicationObject.TstUser
        get() = _currUser!!
        set(value) {_currUser = value }
    var currName: String
        get() = currUser.name
        set(value) {currUser = tao.getUserByName(value)}
    var currUserName: String
        get() = currUser.username
        set(value) {currUser = tao.getUserByLogin(value)}

    //WAO state global variables
    var loginPassword : MonkeyTestClass.LoginPassword? = null
    var registerInformation : MonkeyTestClass.RegisterInformation? = null
    var what : String = ""
    fun changeWhat (x : String) {what = x;filter = ""}
    var filter : String = ""
    var mindWinWhich : String = ""
    var currMind : TstMind? = null
    var currAnswer : TstAnswer? = null
    var mindsPage : Int = 0

    init {
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe")
        driver = ChromeDriver(ChromeOptions().apply {
            if (tao.tstProps.headLess) addArguments("headless")
            addArguments("start-maximized")
            addArguments("window-size=1200x600")
        })
        js = driver
    }

    fun isFriendToCurr (x : TestApplicationObject.TstUser) = tao.isFriend(currUser,x)
    fun isMateToCurr (x : TestApplicationObject.TstUser) = tao.isMate(currUser,x)

    private val visibleUnpagedMinds
        get() = tao.actualMindsArray.filter { mind ->
            (currUser.friendsNames.contains(mind.user) || mind.user == currName) &&
                    (mind.text.contains(filter,true)
                    || mind.user.contains(filter,true)
                    ||  mind.answers.any { it.text.contains(filter,true) })
        }.sortedByDescending {it.time}
    val numberOfMindsPages
        get() = ((visibleUnpagedMinds.size-0.1) / tao.MINDS_PAGE_SIZE).toInt() + 1
    val visibleMinds
            get() = visibleUnpagedMinds
                .run { subList(mindsPage*tao.MINDS_PAGE_SIZE,minOf((mindsPage+1)*tao.MINDS_PAGE_SIZE,size)) }
    val ownersVisibleMinds
        get() = visibleMinds.filter { it.user == currName }
    val visibleAnswers
        get() = visibleMinds.flatMap { it.answers }
    val ownersVisibleAnswers
        get() = visibleAnswers.filter { it.user == currName }
    val visibleUsers
        get() = tao.actualUsersArray
                .asSequence()
                .filter { it.username != currUserName }
                .filter { user -> user.name.contains(filter,true) || user.country.contains(filter,true)}
                .filter (when (what) {
                    "users" -> tao::positive
                    "friends" -> ::isFriendToCurr
                    "mates" -> ::isMateToCurr
                    else -> throw RuntimeException("Impossible")
                })
                .toList()

    /**
     * Set defaults on enter the site
     */
    fun onEnterSite () {
        mindsPage = 0
    }
    private val log = LoggerFactory.getLogger(this::class.java)!!
    fun vlog (s : String) {log.debug(s)}
    /**
     * Performs one round of Monkey Test with certain seed
     * @param seed
     * Invokes lambdas one by one. Every lambda returns next one depend on random and scheme monkey.svg.
     * State of the process is in global variables of the outer WAO. Lambdas change them.
     */
    inner class MonkeyTestClass (private val seed : Long,
                                 private val round : Int,
                                 private val steps : Int,
                                 private val failImmediately : Boolean) : FeignMixin() {
        override val randomer = Random(seed) // MAIN Monkey Test randomer
        override val usedStrings = mutableSetOf<String>()

        fun go () : Triple<Long,Int,Throwable>? {
            var exception : Triple<Long,Int,Throwable>? = null
            log.info("$round ====================== seed = $seed")
            if (tao.tstProps.monkey.feignDB.enabled=="yes"
                    || tao.tstProps.monkey.feignDB.enabled=="random" && rand(5)==0) {
                vlog("FILL DB WITH DATA")
                tao.fillDB(randomer = randomer, usedStrings = usedStrings)
            } else tao.clearDB() // filled or empty DB at seed's start
            driver.get("http://localhost:$port")
            @Suppress("UNCHECKED_CAST")
            var f = loginForm() as? () -> Any
            try {
                while (step++ < steps) {
                if (step >= 10000) {
                    println("JOPA") // place for a breakpoint on a certain step
//                    Thread.sleep(10000)
                }

                    @Suppress("UNCHECKED_CAST")
                    f = (f?.invoke() ?: break) as () -> Any
                    pause(For.SEE)
                }
            } catch (e : Throwable) {
                log.info("!!!!!! PROBLEM !!!!!! Round; $round Seed: $seed Step: $step")
                if (failImmediately) throw e
                exception = Triple(seed,step,e)
            } finally {
                log.info("Seed $seed getMinds PERFORMANCE: "+tao.performanceCounter.getAndReset("getMinds").toString())
            }
            pause(For.LONG_LOAD)
            clk.runCatching {clickLogout()}
            return exception
        }
        private var step = 1
        private fun stepVlog(s : String) {vlog("$step ! $s")}
        var existingUserName = "" // for check existing username via registration
        var existingEmail = "" // for check existing Email via registration

        /**
         * LoginPassword entity can be valid or invalid
         */
        inner class LoginPassword {
            val login : String
            val password : String
            val ok : Boolean
            init {
                val x = rand()
                when {
                    (x < 90 && tao.actualUsersArray.size > 0) -> { // Valid LoginPassword
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

        /**
         * RegisterInformation entity. Can be ok or contain errors
         */
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
                val w = 3 // Probability of a wrong thing in a field
                if (rand()<w) {username = feignUsername(charFrom = ::invalidChar);errors += "usernameErrors"} // invalid chars in a username
                if (rand()<w && existingUserName!="") {username = existingUserName;errors += "usernameErrors"} // existing username
                if (rand()<w) {password = feignUsername(charFrom = ::invalidChar);errors += "passwordErrors";repeatPassword = password} // invalid chars in a password
                if (rand()<w) {repeatPassword = feignUsername(charFrom = ::invalidChar);errors += "repeatPasswordErrors"} // password and repeatPassword are different
                if (rand()<w) {name = feignString(102);errors += "nameErrors"} // name is too long
                if (rand()<w) {email = feignUsername(charFrom = ::invalidChar);errors += "emailErrors"} //  existing email
                if (rand()<w && existingEmail!="") {email = existingEmail;errors += "emailErrors"} // invalid chars in an email
                if (rand()<w) {country = feignString(102);errors += "countryErrors"}  // country is too long
                ok = errors.size == 0
                if (ok) {
                    existingUserName = username
                    existingEmail = email
                }
            }
        }
        private val whatLambda get () = when (what) {
            "minds" -> ::minds
            "users","friends","mates" -> ::users
            else ->  throw RuntimeException("Impossible")
        }

        /**
         * Fun tries to login. If LoginPassword isn't ok returns itself, so tries again
         * But with 10% probability goes to ::registerForm.
         * At first 10 steps this probability is 90% to gain more users for MonkeyTest round
         */
        private fun loginForm () : Any? {
            stepVlog("loginForm")
            pause {driver.title == "Pepsbook Login"}
            onEnterSite()
            loginPassword?.run { driver.findElement(By.id("loginErrors")) } // Check the page for error message presence
            val probabilityOfExistingUserLogin = if (step < 10) 10 else 90
            if (rand() < probabilityOfExistingUserLogin && tao.actualUsersArray.size > 0) {
                LoginPassword().run {
                    driver.findElement(By.name("username")).run { clear();sendKeys(login) }
                    driver.findElement(By.name("password")).run { clear();sendKeys(password) }
                    pause(For.SEE)
                    driver.findElement(By.id("loginForm")).submit()
                    return if (ok) {
                        currUserName = login
                        ::minds
                    } else ::loginForm
                }
            } else {
                driver.findElement(By.tagName("a")).click()
                return ::registerForm
            }
        }

        /**
         * Fun generates RegisterInformation, that can be ok or not, and submit it
         * if RI is ok
         * @return ::minds
         * else
         * @return ::registerForm (itself) - so tries to register again
         */
        private fun registerForm () : Any? {
            stepVlog("registerForm")
            pause {driver.title=="Pepsbook registration"}
            onEnterSite()
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
//                vlog ("Entered: username = $username password = $password repeatPassword=$repeatPassword name=$name email=$email country=$country")
                return if (ok) {
                    tao.actualUsersArray.add(tao.TstUser(name,email,country,username,password))
                    currUserName = username
                    ::minds
                } else ::registerForm
            }
        }

        /**
         * Supposed that minds page is active.
         * Fun checks minds information
         * @return ::mainPage
         */
        private fun minds () : Any? {
            stepVlog("minds filter=$filter mindsPage=$mindsPage currUser={${currUser.name}}")
            //if mindsPage doesn't exist anymore (e.g. deleted last mind) it should be the last one
            if (mindsPage != 0 && numberOfMindsPages <= mindsPage) mindsPage = numberOfMindsPages - 1
            pause {runCatching {js.executeScript("return subMainReady;") as Boolean}.getOrDefault(false)}
            with (chk) {checkMinds()}
            return ::mainPage
        }

        /**
         * Supposed that some of the users pages ("users","friends","mates") is active.
         * Fun checks that user information is right and
         * @return ::mainPage
         */
        private fun users () : Any? {
            stepVlog("users what=$what filter=$filter currUser=${currUser.name}")
            pause {runCatching {js.executeScript("return subMainReady;") as Boolean}.getOrDefault(false)}
            with (chk) {checkUsers()}
            return ::mainPage
        }

        /**
         * Supposed that mindWin is opened, fun enters mind or answer and press submit.
         * But with probability of 5% mindWin will be just closed
         * Probability of invalid (too long input) also is 5%
         * @return ::minds
         */
        private fun mindWin () : Any? {
            stepVlog("mindWin which=$mindWinWhich")
            clk.run {
                if (rand() < 5) { clickCloseMind();pause(For.LOAD);return ::mainPage }
                if (rand() < 2) {
                    typeMindText(feignString(4002)) //Invalid length
                    submitMind()
                    Assert.assertEquals(1, driver.findElements(By.id("mindErrSign")).size)
                    return ::mindWin
                }
                feignString(rand(400)).let { newText ->
                    typeMindText(newText)
                    submitMind()
                    if (mindWinWhich == "mind") {
                        if (currMind != null) tao.doChangeMind(currMind!!.text,newText)
                        else tao.doAddMind(currName,newText)
                    } else {
                        if (currAnswer != null) tao.doChangeAnswer(newText,currAnswer!!.mind.text,currAnswer!!.text)
                        else tao.doAddAnswer(currName,newText,currMind!!.text)
                    }
                    return ::minds
                }
            }
        }

        private fun checkEmptyPage () {
            if (numberOfMindsPages <= mindsPage) mindsPage = numberOfMindsPages - 1
        }


        private fun mainPage () : Any? {
            stepVlog("mainPage what=$what filter=")
            clk.run {
                //caseMatrix contains lambdas to random run. Lambda can be added many times to increase its probability weight
                val caseMatrix = mutableListOf({ clickLogo();changeWhat("minds");onEnterSite();::minds});
                { clickMainMinds()  ;changeWhat("minds");mindsPage=0; ::minds    }.also { repeat(16)  {_ -> caseMatrix.add(it)} };
                { clickMainUsers()  ;changeWhat("users")    ;::users    }.also { repeat(10)  {_ -> caseMatrix.add(it)} };
                { clickMainFriends();changeWhat("friends")  ;::users    }.also { repeat(3)   {_ -> caseMatrix.add(it)} };
                { clickMainMates()  ;changeWhat("mates")    ;::users    }.also { repeat(3)   {_ -> caseMatrix.add(it)} };
                { clickLogout()     ;changeWhat("")         ;::loginForm}.also { repeat(if (step < 10) 60 else 10)  {_ -> caseMatrix.add(it)} }
                if (what == "minds" || what == "users") {
                    { typeFilter(syLIST.random(randomer).also { filter = it });pause(For.LOAD)
                        if (what == "minds") mindsPage=0;whatLambda}
                            .also { repeat(5)  { _ -> caseMatrix.add(it)} } }
                if (what == "minds") {
                    { currMind = null;mindWinWhich = "mind";mindsPage=0
                        clickNewMind();::mindWin }.also { repeat(10)  { _ -> caseMatrix.add(it)} }
                }
                if (what == "minds" && visibleMinds.isNotEmpty()) {
                    { currMind = visibleMinds.random(randomer);currAnswer = null;mindWinWhich = "answer"
                        clickAnswerMind(currMind!!.text);::mindWin}.also { repeat(5)  { _ -> caseMatrix.add(it)} }
                    if (ownersVisibleMinds.isNotEmpty()) {
                        { currMind = ownersVisibleMinds.random(randomer);mindWinWhich = "mind"
                            clickEditMind(currMind!!.text);::mindWin }.also { repeat(3)  { _ -> caseMatrix.add(it)} };
                        { currMind = ownersVisibleMinds.random(randomer)
                            clickDelMind(currMind!!.text)
                            tao.doRemoveMind(currMind!!.text);checkEmptyPage();::minds }
                                .also { repeat(3)  { _ -> caseMatrix.add(it)} }
                    }
                    if (visibleAnswers.isNotEmpty()) {
                        { val clickAnswer = visibleAnswers.random(randomer)
                            currMind = clickAnswer.mind;currAnswer = null;mindWinWhich = "answer"
                            clickAnswerAnswer(clickAnswer.text);::mindWin}.also { repeat(3)  { _ -> caseMatrix.add(it)} }
                        if (ownersVisibleAnswers.isNotEmpty()) {
                            { currAnswer = ownersVisibleAnswers.random(randomer);mindWinWhich = "answer"
                                clickEditAnswer(currAnswer!!.text);::mindWin }.also { repeat(3)  { _ -> caseMatrix.add(it)} };
                            { currAnswer = ownersVisibleAnswers.random(randomer)
                                clickDelAnswer(currAnswer!!.text)
                                tao.doRemoveAnswer(currAnswer!!.text,currAnswer!!.mind.text);::minds }
                                    .also { repeat(3)  { _ -> caseMatrix.add(it)} }
                        }
                    }
                    //<editor-fold desc="Pagination">
                    if (numberOfMindsPages > 1) {
                        { currMind = null;mindWinWhich = "mind";var fired = false
                            if (numberOfMindsPages > tao.PAGINATOR_MAX_SIZE)
                                // boiled mode of paginator. we test only pressings to prev,first,last,next
                                when (val r = rand(4)-3) {
                                     0 -> {mindsPage=0;clickPaginator(r);fired=true}
                                    -1 -> if (mindsPage > 0) {mindsPage--;clickPaginator(r);fired=true}
                                    -2 -> if (mindsPage < numberOfMindsPages - 1) {mindsPage++;clickPaginator(r);fired=true}
                                    -3 -> {mindsPage = numberOfMindsPages - 1;clickPaginator(r);fired=true}
                                }
                            else
                                // not-boiled mode of paginator (when number of pages < PAGINATOR_MAX_SIZE)
                                when (val r = rand(numberOfMindsPages+2)-2) {
                                    -1 -> if (mindsPage > 0) {mindsPage--;clickPaginator(r);fired=true}
                                    -2 -> if (mindsPage < numberOfMindsPages-1) {mindsPage++;clickPaginator(r);fired=true}
                                    else -> {mindsPage = r;clickPaginator(r);fired=true}
                                }
                            if (fired) ::minds else ::mainPage
                        }.also { repeat(10)  { _ -> caseMatrix.add(it)} }
                    }
                    //</editor-fold>
                }
                if (what == "users" && visibleUsers.isNotEmpty()) {
                    { val clickUser = visibleUsers.random(randomer)
                        if (isFriendToCurr(clickUser)) {
                            clickUserFromFriends(clickUser.name)
                            tao.doFromFriends(currName,clickUser.name)
                        } else {
                            clickUserToFriends(clickUser.name)
                            tao.doToFriends(currName,clickUser.name)
                        }
                        ::users}.also { repeat(10)  { _ -> caseMatrix.add(it)} }
                }
                return caseMatrix.random(randomer).invoke()
            }
        }
    }
}