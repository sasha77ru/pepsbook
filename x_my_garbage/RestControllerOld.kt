package ru.sasha77.spring.pepsbook

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.transaction.Transactional

fun getNewCookie() = Array(20) { ('A'..'Z').random() }.joinToString("")

@Controller
@RequestMapping(path = ["/rest"])
@EnableTransactionManagement
open class RestControllerOld {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var mindRepository: MindRepository

    @RequestMapping(path = ["/alluserssimple"], produces = ["application/json"])
    @ResponseBody
    @Transactional
    open fun users(): Iterable<UserSimple> = userRepository.findAll().map { UserSimple(it.id,it.name,it.email) }

    @RequestMapping(path = ["/checkuser"])
    @ResponseBody
    @Transactional
    open fun checkUser(@RequestParam login : String, response: HttpServletResponse): String {
        response.addCookie(Cookie("keyCookie",(userRepository.findUserByEmail(login) ?: return "Wrong user").keyCookie)
                .apply { path = "/" })
        return "ok"
    }

    @RequestMapping(path = ["/userByCookie"], produces = ["application/json"])
    @ResponseBody
    @Transactional
    open fun userByCookie(@CookieValue("keyCookie") keyCookie : String, request : HttpServletRequest): UserSimple? =
            userRepository.findUserByKeyCookie(keyCookie)?.run { UserSimple(id,name,email) }// ?: throw IllegalArgumentException("Wrong keyCookie")

    @RequestMapping(path = ["/logOff"])
    @ResponseBody
    @Transactional
    open fun logOff(all : Boolean, @CookieValue("keyCookie") keyCookie : String, response: HttpServletResponse): String {
        response.addCookie(Cookie("keyCookie",null).apply { maxAge = -1;path="/" })
        if (all) {
            val currUser = userRepository.findUserByKeyCookie(keyCookie) ?: return "error: Wrong keyCookie"
            currUser.keyCookie = getNewCookie()
            userRepository.save(currUser)
        }
        return "ok"
    }

    @RequestMapping(path = ["/toFriends"])
    @ResponseBody
    @Transactional
    open fun toFriends(@CookieValue("keyCookie") keyCookie : String, @RequestParam friend_id : Int) : String {
        val currUser = userRepository.findUserByKeyCookie(keyCookie) ?: return "error: Wrong keyCookie"
        val friend = userRepository.findByIdOrNull(friend_id) ?: return "error: Friend isn't found"
        currUser.friends.add(friend)
        userRepository.save(currUser)
        return if (currUser.mates.contains(friend)) "mutual" else "halfFriend"
    }

    @RequestMapping(path = ["/fromFriends"])
    @ResponseBody
    @Transactional
    open fun fromFriends(@CookieValue("keyCookie") keyCookie : String, @RequestParam friend_id : Int) : String {
        val currUser = userRepository.findUserByKeyCookie(keyCookie) ?: return "error: Wrong keyCookie"
        val friend = userRepository.findByIdOrNull(friend_id) ?: return "error: Friend isn't found"
        currUser.friends.remove(friend)
        userRepository.save(currUser)
        return if (currUser.mates.contains(friend)) "mutual" else "nobody"
    }

    @RequestMapping(path = ["/saveMind"])
    @ResponseBody
    @Transactional
    open fun saveMind(@CookieValue("keyCookie") keyCookie : String, text : String, id : Int?) : String {
        val currUser = userRepository.findUserByKeyCookie(keyCookie) ?: return "error: Wrong keyCookie"
        mindRepository.save(
                if (id == null || id == 0) Mind(text,currUser)
                else mindRepository.findByIdOrNull(id)?.apply {
                    if (user.id == currUser.id) setText(text) else return "error: Attempt to edit a foreign mind"
                } ?: return "error: Mind doesn't exist")
        return "ok"
    }

    @RequestMapping(path = ["/removeMind"])
    @ResponseBody
    @Transactional
    open fun removeMind(@CookieValue("keyCookie") keyCookie : String, id : Int) : String {
        val currUser = userRepository.findUserByKeyCookie(keyCookie) ?: return "error: Wrong keyCookie"
        mindRepository.delete(
                mindRepository.findByIdOrNull(id)?.apply {
                    if (user.id != currUser.id) return "error: Attempt to remove a foreign mind"
                } ?: return "error: Mind doesn't exist")
        return "ok"
    }

    @RequestMapping(path = ["/addUser"])
    @Transactional
    open fun addUser(@CookieValue("keyCookie") keyCookie : String,
                     name : String, email : String, country : String,
                     response: HttpServletResponse) : String {
        val currUser = User(name, email, country)
        userRepository.save(currUser)
        response.addCookie(Cookie("keyCookie",currUser.keyCookie).apply { path = "/" })
        return "redirect:/"
    }
}
