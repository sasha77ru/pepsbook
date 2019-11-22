package ru.sasha77.spring.pepsbook

import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.lang.RuntimeException
import kotlin.random.Random

/**
 * MONKEY TEST simulates random user clicking ang typing (according to monkey.svg)
 * changing TAO and comparing results with TAO (see testClasses.svg)
 * Monkey test runs for my.tst.monkey.rounds times called rounds
 * Every round consists of my.tst.monkey.steps steps
 * Every round starts with its own randomer seed. Value of the seed put to a log
 * If something wrong in a round you can repeat it setting my.tst.monkey.seed properties,
 * so next time zero round of Monkey Test will repeat problem round
 */
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [PepsbookApplication::class])
@AutoConfigureMockMvc
//@TestPropertySource(locations = ["classpath:application-integrationtest.properties"])
@ActiveProfiles("dev,tst")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class BMonkeyTests {

    @LocalServerPort
    val port: Int = 0

    @Autowired
    lateinit var tstProps : TstProps

    @Autowired
    lateinit var tao : TestApplicationObject

    private lateinit var wao : WebApplicationObject

    private var initialized : Boolean = false

    @Before
    fun initialize () {
        if (!initialized) {
            wao = WebApplicationObject(this.tao,port) // Create WAO for Monkey Test
            initialized = true
        }
    }

    /**
     * Repeat the zero round with certain seed if it exists
     */
    @Test fun monkeyTest0Round() {
        if (tstProps.monkey.seed != 0L) wao.MonkeyTestClass(tstProps.monkey.seed, 0, tstProps).go()
    }

    /**
     * Perform random rounds
     */
    @Test fun monkeyTestOthers() {
        val badRoundSeeds = mutableListOf<Long>()
        for (round in (1 .. tstProps.monkey.rounds)) {
            val seed = Random.nextLong()
            if (!wao.MonkeyTestClass(seed, round, tstProps).go()) {
                badRoundSeeds.add(seed)
            }
        }
        assertEquals("!!! SOME SEEDS ARE BAD !!!",listOf<Long>(), badRoundSeeds)
    }

    @Test fun xClose () {
        if (tao.tstProps.closeBrowser) wao.driver.close()
    }
}

