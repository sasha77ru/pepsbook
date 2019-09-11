package ru.sasha77.spring.pepsbook

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
open class TestTemplateAuth {
    @Autowired
    lateinit var template : TestRestTemplate

    @Test
    fun test0001 () {
        val result = template.withBasicAuth("buzz", "infinity")
          .getForEntity("/rest/allUsersSimple", String::class.java)
        print(result.body)
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("[{\"id\":820,\"name\":\"Porky\",\"email\":\"porky@pig.com\"},{\"id\":821,\"name\":\"Pluto\",\"email\":\"pluto@dog.com\"},{\"id\":822,\"name\":\"Masha\",\"email\":\"masha@child.com\"},{\"id\":823,\"name\":\"Luntik\",\"email\":\"luntik@alien.com\"}]"
                ,result.body)
    }
}