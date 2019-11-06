package ru.sasha77.spring.pepsbook

import org.aspectj.lang.JoinPoint
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.ImportResource
import org.springframework.stereotype.Component

/**
 * The aspect launched before Clickers methods
 */
@Component("logAop")
open class LogAspect {
    private var log = LoggerFactory.getLogger(this::class.java)!!
    @Suppress("unused")
    fun beforeAdvice(joinPoint : JoinPoint) {
        log.info("> ${joinPoint.signature.name} (${joinPoint.args.drop(1).joinToString(",")})")
    }
}

/**
 * Set of methods that perform browser actions on objects having driver var (see testClasses.svg)
 */
@Component
@ImportResource("LogAop.xml")
open class Clickers {
    open fun ObjWithDriver.clickLogo () {
        driver.findElement(By.className("navbar-brand")).click()
    }
    open fun ObjWithDriver.clickMainMinds () {
        driver.findElement(By.id("mainMinds")).click()
    }
    open fun ObjWithDriver.clickMainUsers () {
        driver.findElement(By.id("mainUsers")).click()
    }
    open fun ObjWithDriver.clickMainFriends () {
        driver.findElement(By.id("mainFriends")).click()
    }
    open fun ObjWithDriver.clickMainMates () {
        driver.findElement(By.id("mainMates")).click()
    }
    open fun ObjWithDriver.clickUserToFriends (userName: String) {
        driver.findElements(By.className("userEntity"))
                .find {
                    it.findElement(By.className("userName")).text
                            .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"") ==
                            userName.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                }!!.run {
            findElement(By.className("dropdown-toggle")).click()
            findElement(By.className("toFriends")).click()
        }
    }
    open fun ObjWithDriver.clickUserFromFriends (userName: String) {
        driver.findElements(By.className("userEntity"))
                .find {
                    it.findElement(By.className("userName")).text
                            .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"") ==
                            userName.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                }!!.run {
            findElement(By.className("dropdown-toggle")).click()
            findElement(By.className("fromFriends")).click()
        }
    }
    open fun ObjWithDriver.clickEditMind (mindText: String) {
        driver.findElements(By.className("mindEntity"))
                .find {
                    it.findElement(By.className("mindText")).text
                            .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"") ==
                            mindText.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                }!!.run {
            findElement(By.className("dropdown-toggle")).click()
            findElement(By.className("editMind")).click()
        }
    }
    open fun ObjWithDriver.clickDelMind (mindText: String) {
        driver.findElements(By.className("mindEntity"))
                .find {
                    it.findElement(By.className("mindText")).text
                            .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"") ==
                            mindText.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                }!!.run {
            findElement(By.className("dropdown-toggle")).click()
            findElement(By.className("delMind")).click()
        }
    }
    open fun ObjWithDriver.clickAnswerMind (mindText: String) {
        driver.findElements(By.className("mindEntity"))
                .find {
                    it.findElement(By.className("mindText")).text
                            .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"") ==
                            mindText.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                }!!.run {
            findElement(By.className("dropdown-toggle")).click()
            findElement(By.className("answerMind")).click()
        }
    }
    open fun ObjWithDriver.clickEditAnswer (answerText: String) {
        driver.findElements(By.className("answerEntity"))
                .find {
                    it.findElement(By.className("answerText")).text
                            .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"") ==
                            answerText.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                }!!.run {
            findElement(By.className("dropdown-toggle")).click()
            findElement(By.className("editAnswer")).click()
        }
    }
    open fun ObjWithDriver.clickDelAnswer (answerText: String) {
        driver.findElements(By.className("answerEntity"))
                .find {
                    it.findElement(By.className("answerText")).text
                            .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"") ==
                            answerText.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                }!!.run {
            findElement(By.className("dropdown-toggle")).click()
            findElement(By.className("delAnswer")).click()
        }
    }
    open fun ObjWithDriver.clickAnswerAnswer (answerText: String) {
        driver.findElements(By.className("answerEntity"))
                .find {
                    it.findElement(By.className("answerText")).text
                            .replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"") ==
                            answerText.replace(Regex("^ +| +(?= )| +$",RegexOption.MULTILINE),"")
                }!!.run {
            findElement(By.className("dropdown-toggle")).click()
            findElement(By.className("answerAnswer")).click()
        }
    }
    open fun ObjWithDriver.clickLogout () {
        (driver as JavascriptExecutor).executeScript("logOff();")
    }
    open fun ObjWithDriver.typeFilter (filterText: String, clear : Boolean = true) {
        driver.findElement(By.id("mainFilter")).run {
            if (clear) clear()
            filterText.forEach { sendKeys(it.toString()) }
        }
    }
    open fun ObjWithDriver.clickNewMind () {
        driver.findElement(By.id("newMind")).click()
    }
    open fun ObjWithDriver.clickCloseMind () {
        driver.findElement(By.id("closeMind")).click()
    }
    open fun ObjWithDriver.typeMindText (mindText: String, clear : Boolean = true) {
        driver.findElement(By.id("mindTextArea")).run { if (clear) clear();sendKeys(mindText) }
    }
    open fun ObjWithDriver.submitMind () {
        driver.findElement(By.id("mindWindow")).findElement(By.className("btn-primary")).click() }
}