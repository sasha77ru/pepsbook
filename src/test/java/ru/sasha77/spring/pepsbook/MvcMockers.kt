package ru.sasha77.spring.pepsbook

import org.hamcrest.Matchers
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import ru.sasha77.spring.pepsbook.MyUtilities.myDate
import ru.sasha77.spring.pepsbook.security.TokenProvider

/**
 * Set of methods that perform MockMvc actions with changing TAO (see testClasses.svg)
 * and MockMvc checker to compare TAO with result
 */
@Component
open class MvcMockers {
    @Autowired
    lateinit var mockMvc : MockMvc
    @Autowired
    lateinit var tao : TestApplicationObject
    @Autowired
    lateinit var tokenProvider: TokenProvider

    fun mvcToFriends (name : String, friendName : String) {
        val user = tao.getUserByName(name)
        val token = tokenProvider.createTestToken(user.username)
        val friend = tao.getUserByName(friendName)

        tao.doToFriends(name,friendName)

        mockMvc.perform(MockMvcRequestBuilders.patch("/rest/toFriends")
                .header("Authorization", "Bearer $token")
                .param("friend_id", friend.user.id.toString()))
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("error"))))
    }

    fun mvcFromFriends (name : String, friendName : String) {
        val user = tao.getUserByName(name)
        val token = tokenProvider.createTestToken(user.username)
        val friend = tao.getUserByName(friendName)

        tao.doFromFriends(name,friendName)

        mockMvc.perform(MockMvcRequestBuilders.patch("/rest/fromFriends")
                .header("Authorization", "Bearer $token")
                .param("friend_id", friend.user.id.toString()))
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("error"))))
    }

    fun mvcChangeMind (name : String, text : String, oldText : String?) {
        val user = tao.getUserByName(name)
        val token = tokenProvider.createTestToken(user.username)

        val dbMind = if (oldText == null) {
            tao.doAddMind(name, text)
            null
        } else {
            tao.doChangeMind(oldText,text)
            tao.getDBMindByText(oldText)
        }

        mockMvc.perform(MockMvcRequestBuilders.post("/rest/saveMind")
                .header("Authorization", "Bearer $token")
                .param("text",text)
                .apply {
                    if (dbMind != null) param("id",dbMind.id.toString())
                })
                    .andExpect(MockMvcResultMatchers.status().isOk)
    }

    fun mvcAddMind (name : String, text : String) {
        mvcChangeMind (name, text, null)
    }

    fun mvcRemoveMind (name : String, oldText : String) {
        val user = tao.getUserByName(name)
        val token = tokenProvider.createTestToken(user.username)
        val dbMind = tao.getDBMindByText(oldText)

        tao.doRemoveMind(oldText)

        mockMvc.perform(MockMvcRequestBuilders.delete("/rest/removeMind")
                .header("Authorization", "Bearer $token")
                .param("id",dbMind.id.toString()))
                    .andExpect(MockMvcResultMatchers.status().isOk)
    }

    fun mvcChangeAnswer (name : String, text : String, mindText : String, oldText : String? = null) {
        val user = tao.getUserByName(name)
        val token = tokenProvider.createTestToken(user.username)
        val parentDBMind = tao.getDBMindByText(mindText)

        val answerDB = if (oldText == null) {
            tao.doAddAnswer(name,text,mindText)
            null
        } else {
            tao.doChangeAnswer(text, mindText, oldText)
            tao.getDBAnswerByText(oldText)
        }
        mockMvc.perform(MockMvcRequestBuilders.post("/rest/saveAnswer")
                .header("Authorization", "Bearer $token")
                .param("text",text)
                .param("parentMind",parentDBMind.id.toString())
                .apply {
                    if (answerDB != null) param("id",answerDB.id.toString())
                })
                    .andExpect(MockMvcResultMatchers.status().isOk)
    }
    fun mvcAddAnswer (name : String, text : String, mindText : String) {
        mvcChangeAnswer (name, text, mindText, null)
    }
    fun mvcRemoveAnswer (name : String, oldText : String, mindText: String) {
        val user = tao.getUserByName(name)
        val token = tokenProvider.createTestToken(user.username)
        val dbAnswer = tao.getDBAnswerByText(oldText)

        tao.doRemoveAnswer(oldText,mindText)

        mockMvc.perform(MockMvcRequestBuilders.delete("/rest/removeAnswer")
                .header("Authorization", "Bearer $token")
                .param("id",dbAnswer.id.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk)
    }

    /**
     * Does checkDB for all users
     */
    open fun checkAllDB () {tao.actualUsersArray.forEach {checkDB(it.name)}}

    /**
     * Performs All GET MockMvc and compares result with TAO
     */
    fun checkDB (name: String, subs : String = "") {
        val currUser = tao.getUserByName(name)
        val token = tokenProvider.createTestToken(currUser.username)
        checkUsers(name, subs, token = token)
        checkMinds(name, subs, token = token)
        checkInterlocutors(name, token = token)
        tao.actualUsersArray.forEach { checkMessages(name, it.name, token = token) }
        checkInterlocutors(name, token = token)
    }
    fun checkUsers (name: String,
                    subs: String = "",
                    token: String = tokenProvider.createTestToken(tao.getUserByName(name).username)) {
        val currUser = tao.getUserByName(name)
        mockMvc.perform(MockMvcRequestBuilders.get("/rest/users")
                .param("subs", subs)
                .header("Authorization", "Bearer $token"))
                .andDo { mvcResult ->
                    @Suppress("UNCHECKED_CAST")
                    Assert.assertEquals("Different users",
                            tao.actualUsersArray
                                    .filter { it.user.username != currUser.user.username }
                                    .filter { it.name.contains(subs,ignoreCase = true) || it.country.contains(subs,ignoreCase = true) }
                                    .sortedBy { it.user.name }
                                    .joinToString("\n") { "${it.user.name} / ${it.user.country}" },
                            JSONArray(mvcResult.response.contentAsString)
                                    .let {
                                        // Parse response JSON to compare
                                        val list = mutableListOf<String>()
                                        for (i in 0 until it.length()) {
                                            list.add(JSONObject(it.getString(i)).run {
                                                getString("name") + " / " + getString("country")
                                            })
                                        }
                                        list
                                    }.joinToString("\n")
                    )
                }
        mockMvc.perform(MockMvcRequestBuilders.get("/rest/friends")
                .param("subs", "")
                .header("Authorization", "Bearer $token"))
                .andDo { mvcResult ->
                    @Suppress("UNCHECKED_CAST")
                    Assert.assertEquals("Different friends",
                            tao.actualUsersArray.filter { tstUser ->
                                tstUser.user.username != currUser.user.username
                                        && currUser.friendsNames.any { it == tstUser.user.name } }
                                    .sortedBy { it.user.name }
                                    .joinToString("\n") { "${it.user.name} / ${it.user.country}" },
                            JSONArray(mvcResult.response.contentAsString)
                                    .let {
                                        // Parse response JSON to compare
                                        val list = mutableListOf<String>()
                                        for (i in 0 until it.length()) {
                                            list.add(JSONObject(it.getString(i)).run {
                                                getString("name") + " / " + getString("country")
                                            })
                                        }
                                        list
                                    }.joinToString("\n")
                    )
                }
        mockMvc.perform(MockMvcRequestBuilders.get("/rest/mates").param("subs", "")
                .header("Authorization", "Bearer $token"))
                .andDo { mvcResult ->
                    @Suppress("UNCHECKED_CAST")
                    Assert.assertEquals("Different mates",
                            tao.actualUsersArray.filter { tstUser ->
                                tstUser.user.username != currUser.user.username
                                        && currUser.matesNames.any { it == tstUser.user.name } }
                                    .sortedBy { it.user.name }
                                    .joinToString("\n") { "${it.user.name} / ${it.user.country}" },
                            JSONArray(mvcResult.response.contentAsString)
                                    .let {
                                        // Parse response JSON to compare
                                        val list = mutableListOf<String>()
                                        for (i in 0 until it.length()) {
                                            list.add(JSONObject(it.getString(i)).run {
                                                getString("name") + " / " + getString("country")
                                            })
                                        }
                                        list
                                    }.joinToString("\n")
                    )
                }
    }
    fun checkMinds (name: String,
                    subs: String,
                    page: Int? = null,
                    size: Int? = null,
                    token: String = tokenProvider.createTestToken(tao.getUserByName(name).username),
                    deceivePage : Int? = page /*to try to get page that doesn't exist*/,
                    log : org.slf4j.Logger? = null) {
        val currUser = tao.getUserByName(name)
        val timeBefore = System.currentTimeMillis()
        mockMvc.perform(MockMvcRequestBuilders.get("/rest/minds")
                .param("subs", subs)
                .apply {
                    if (page!=null) param("page",deceivePage.toString())
                    if (size!=null) param("size",size.toString()) }
                .header("Authorization", "Bearer $token"))
                .andDo { mvcResult ->
                    log?.debug("${System.currentTimeMillis()-timeBefore}")
                    @Suppress("UNCHECKED_CAST")
                    Assert.assertEquals("Different lists",
                            tao.actualMindsArray
                                    .filter { (currUser.friendsNames.contains(it.user) || it.user == currUser.name) &&
                                        (it.text.contains(subs,true)
                                            || it.user.contains(subs,true)
                                            || it.answers.any { answer -> answer.text.contains(subs,true) }
                                            ) }
                                    .sortedByDescending { it.time }
                                    .let { if (page == null || size == null)
                                        it else it.subList(page*size, minOf((page+1) *size,it.size))}
                                    .joinToString("\n") {mind ->
                                        with(mind) {"$text / $user / ${myDate(time)}"} + " : " +
                                                mind.answers.joinToString(" | ") {
                                                    with(it) {"$text / $user / ${myDate(time)}"} } }
                            ,
                            JSONObject(mvcResult.response.contentAsString).getJSONArray("content")
                                    .let { response ->
                                        // Parse response JSON to compare
                                        val list = mutableListOf<String>()
                                        for (i in 0 until response.length()) {
                                            list.add(JSONObject(response.getString(i)).run {
                                                val answers = JSONArray(getString("answers")).let {
                                                    val answersList = mutableListOf<String>()
                                                    for (j in 0 until it.length()) {
                                                        answersList.add(JSONObject(it.getString(j)).run {
                                                            getString("text") + " / " + getString("author") + " / " + getString("time")
                                                        })
                                                    }
                                                    answersList
                                                }.joinToString(" | ")
                                                getString("text") + " / " +
                                                        getString("author") + " / " +
                                                        getString("time") + " : " +
                                                        answers
                                            })
                                        }
                                        list
                                    }.joinToString("\n")
                    )
                }
    }

    fun checkMessages (name: String,
                       whomName: String,
                       subs: String = "",
                       page: Int? = null,
                       size: Int? = null,
                       token: String = tokenProvider.createTestToken(tao.getUserByName(name).username),
                       deceivePage : Int? = page /*to try to get page that doesn't exist*/,
                       log : org.slf4j.Logger? = null) {
        val currUser = tao.getUserByName(name)
        val whomUser = tao.getUserByName(whomName)
        val timeBefore = System.currentTimeMillis()

        if (page==0) tao.doClearInterlocutorState(whomName,currUser.name)

        mockMvc.perform(MockMvcRequestBuilders.get("/rest/messages")
                .param("whomId", whomUser.user.id.toString())
                .apply {
                    if (subs!="") param("subs", subs)
                }
                .apply {
                    if (page!=null) param("page",deceivePage.toString())
                    if (size!=null) param("size",size.toString()) }
                .header("Authorization", "Bearer $token"))
                .andDo { mvcResult ->
                    log?.debug("${System.currentTimeMillis()-timeBefore}")
                    @Suppress("UNCHECKED_CAST")
                    Assert.assertEquals("Different lists",
                            tao.actualMessagesArray
                                    .filter { (it.user == currUser.name && it.whom == whomUser.name
                                                    || it.user == whomUser.name && it.whom == currUser.name )
                                            &&
                                              (it.text.contains(subs,true)
                                                    || it.whom.contains(subs,true)) }
                                    .sortedByDescending { it.time }
                                    .let { if (page == null || size == null)
                                        it else it.subList(page*size, minOf((page+1) *size,it.size))}
                                    .joinToString("\n") {message ->
                                        with(message) {"$text / $user / $whom / ${myDate(time)}"}}
                            ,
                            JSONObject(mvcResult.response.contentAsString).getJSONArray("content")
                                    .let { response ->
                                        // Parse response JSON to compare
                                        val list = mutableListOf<String>()
                                        for (i in 0 until response.length()) {
                                            list.add(JSONObject(response.getString(i)).run {
                                                getString("text") + " / " +
                                                        getString("userName") + " / " +
                                                        getString("whomName") + " / " +
                                                        getString("time")
                                            })
                                        }
                                        list
                                    }.joinToString("\n")
                    )
                }
    }


    fun checkInterlocutors (name: String,
                       subs: String = "",
                       token: String = tokenProvider.createTestToken(tao.getUserByName(name).username),
                       log : org.slf4j.Logger? = null) {
        val currUser = tao.getUserByName(name)
        val timeBefore = System.currentTimeMillis()
        mockMvc.perform(MockMvcRequestBuilders.get("/rest/interlocutors")
                .apply {
                    if (subs!="") param("subs", subs)
                }
                .header("Authorization", "Bearer $token"))
                .andDo { mvcResult ->
                    log?.debug("${System.currentTimeMillis()-timeBefore}")
                    @Suppress("UNCHECKED_CAST")
                    Assert.assertEquals("Different lists",
                            tao.actualInterlocutorsArray
                                    .filter { it.whose == currUser.name && it.user.contains(subs,true) }
                                    .sortedByDescending { it.time }
                                    .joinToString("\n") {interlocutor ->
                                        with(interlocutor) {"$user / ${currUser.user.id} / $numNewMessages / $hasPreMessages / ${myDate(time)}"}}
                            ,
                            JSONArray(mvcResult.response.contentAsString)
                                    .let { response ->
                                        // Parse response JSON to compare
                                        val list = mutableListOf<String>()
                                        for (i in 0 until response.length()) {
                                            list.add(JSONObject(response.getString(i)).run {
                                                getString("userName") + " / " +
                                                        getString("whoseId") + " / " +
                                                        getString("numNewMessages") + " / " +
                                                        getString("hasPreMessages") + " / " +
                                                        getString("time")
                                            })
                                        }
                                        list
                                    }.joinToString("\n")
                    )
                }
    }

    fun mvcNewMessage (name : String, whomName : String, text : String) {
        val user = tao.getUserByName(name)
        val whom = tao.getUserByName(whomName)
        val token = tokenProvider.createTestToken(user.username)

        tao.doAddMessage(name,whomName,text)

        mockMvc.perform(MockMvcRequestBuilders.post("/rest/newMessage")
                .header("Authorization", "Bearer $token")
                .param("whomId",whom.user.id.toString())
                .param("text",text))
                    .andExpect(MockMvcResultMatchers.status().isOk)
    }

    fun mvcRemoveMessage (name : String, oldText : String) {
        val user = tao.getUserByName(name)
        val token = tokenProvider.createTestToken(user.username)
        val dbMessage = tao.getDBMessageByText(oldText)

        tao.doRemoveMessage(user.name,oldText)

        mockMvc.perform(MockMvcRequestBuilders.delete("/rest/removeMessage")
                .header("Authorization", "Bearer $token")
                .param("messageId",dbMessage._id.toString()))
                    .andExpect(MockMvcResultMatchers.status().isOk)
    }

    fun mvcStartMessaging (name : String, whomName : String) {
        val user = tao.getUserByName(name)
        val whom = tao.getUserByName(whomName)
        val token = tokenProvider.createTestToken(user.username)

        tao.doStartMessaging(name,whomName)

        mockMvc.perform(MockMvcRequestBuilders.post("/rest/startMessaging")
                .header("Authorization", "Bearer $token")
                .param("whomId",whom.user.id.toString()))
                    .andExpect(MockMvcResultMatchers.status().isOk)
    }
}