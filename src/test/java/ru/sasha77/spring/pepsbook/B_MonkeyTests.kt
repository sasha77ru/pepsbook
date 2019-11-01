package ru.sasha77.spring.pepsbook

import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.stereotype.Component
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import kotlin.random.Random

@Component
@ConfigurationProperties(prefix = "my.tst")
class TstProps {
    var headLess : Boolean = false
    var closeBrowser : Boolean = true
    var monkey : MonkeyTestProps = MonkeyTestProps()
    open class MonkeyTestProps {
        var seed : Long = 0
        var rounds : Int = 0
        var steps : Int = 0
    }
}

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [PepsbookApplication::class])
@AutoConfigureMockMvc
//@TestPropertySource(locations = ["classpath:application-integrationtest.properties"])
@ActiveProfiles("dev,tst")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MonkeyTests {

    @LocalServerPort
    val port: Int = 0

    @Autowired
    lateinit var tstProps : TstProps

    @Autowired
    lateinit var tao : TestApplicationObject

    lateinit var wao : WebApplicationObject

    var initialized : Boolean = false

    @Before
    fun initialize () {
        if (!initialized) {
            wao = WebApplicationObject(this.tao,port)
            initialized = true
        }
    }

    @Test fun monkeyTest001() {
        if (tstProps.monkey.seed != 0L) wao.MonkeyTestClass(tstProps.monkey.seed,tstProps).go()
        for (round in 1..tstProps.monkey.rounds)
            wao.MonkeyTestClass(Random.nextLong().also { println("$round ====================== seed = $it") },tstProps).go()
    }

    @Test fun xClose () {
        if (tao.tstProps.closeBrowser) wao.driver.close()
    }
}

