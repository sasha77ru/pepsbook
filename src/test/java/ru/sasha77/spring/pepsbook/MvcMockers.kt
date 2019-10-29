package ru.sasha77.spring.pepsbook

import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@Component
class MvcMockers {
    @Autowired
    lateinit var mockMvc : MockMvc
    @Autowired
    lateinit var tao : TestApplicationObject

    fun mvcToFriends (name : String, friendName : String) {
        val user = tao.getUserByName(name)
        val friend = tao.getUserByName(friendName)

        tao.doToFriends(name,friendName)

        mockMvc.perform(MockMvcRequestBuilders.patch("/rest/toFriends")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.username, user.password))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("friend_id", friend.user.id.toString()))
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("error"))))
    }

    fun mvcFromFriends (name : String, friendName : String) {
        val user = tao.getUserByName(name)
        val friend = tao.getUserByName(friendName)

        tao.doFromFriends(name,friendName)

        mockMvc.perform(MockMvcRequestBuilders.patch("/rest/fromFriends")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.username, user.password))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("friend_id", friend.user.id.toString()))
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("error"))))
    }

    fun mvcChangeMind (name : String, text : String, oldText : String?) {
        val user = tao.getUserByName(name)

        val dbMind = if (oldText == null) {
            tao.doAddMind(name, text)
            null
        } else {
            tao.doChangeMind(oldText,text)
            tao.getDBMindByText(oldText)
        }

        mockMvc.perform(MockMvcRequestBuilders.post("/rest/saveMind")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.username, user.password))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
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
        val dbMind = tao.getDBMindByText(oldText)

        tao.doRemoveMind(oldText)

        mockMvc.perform(MockMvcRequestBuilders.delete("/rest/removeMind")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.username, user.password))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("id",dbMind.id.toString()))
                    .andExpect(MockMvcResultMatchers.status().isOk)
    }

    fun mvcChangeAnswer (name : String, text : String, mindText : String, oldText : String? = null) {
        val user = tao.getUserByName(name)
        val parentDBMind = tao.getDBMindByText(mindText)

        val answerDB = if (oldText == null) {
            tao.doAddAnswer(name,text,mindText)
            null
        } else {
            tao.doChangeAnswer(text, mindText, oldText)
            tao.getDBAnswerByText(oldText)
        }
        mockMvc.perform(MockMvcRequestBuilders.post("/rest/saveAnswer")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.username, user.password))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
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
        val dbAnswer = tao.getDBAnswerByText(oldText)

        tao.doRemoveAnswer(oldText,mindText)

        mockMvc.perform(MockMvcRequestBuilders.delete("/rest/removeAnswer")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic(user.username, user.password)).with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("id",dbAnswer.id.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk)
    }
}