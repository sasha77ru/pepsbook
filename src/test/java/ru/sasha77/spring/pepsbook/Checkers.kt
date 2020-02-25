package ru.sasha77.spring.pepsbook

import org.junit.Assert
import org.openqa.selenium.By
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Set of methods that check browser page on WAO (see testClasses.svg)
 */
@Component
open class Checkers {
    private fun String.dbRegExp() : String = dbRegExp(this)
    private fun String.pageRegExp() : String = pageRegExp(this)
    @Value("\${my.tst.strict}") var STRICT = true
    @Value("\${my.tst.checkRenders}") var CHECK_RENDERS = true
    open fun WebApplicationObject.checkMinds() {
        if (CHECK_RENDERS) {
            Assert.assertEquals((1..visibleMinds.size).fold("") { acc: String, _ -> "${acc}Mind\n" }, checkRenderLog(js))
        }
        
        Assert.assertEquals("Different Minds",
                visibleMinds.joinToString ("\n") { mind ->
                    "${mind.text} / ${mind.user} / ${if (STRICT) MyUtilities.myDate(mind.time) else ""} / ${
                    mind.answers.sortedBy { it.time }.map {answer -> 
                        "${answer.text} / ${mind.text} / ${answer.user} / ${if (STRICT) MyUtilities.myDate(answer.time) else ""}"}
                    }"
                }.dbRegExp()
                ,
                driver.findElements(By.className("mindEntity"))
                        .joinToString("\n") { mindEntity ->
                            mindEntity.findElement(By.className("mindText")).text +
                                    " / " + mindEntity.findElement(By.className("mindUser")).text +
                                    " / " + if (STRICT) mindEntity.findElement(By.className("mindTime")).text else "" +
                                    " / " + mindEntity.findElements(By.className("answerEntity")).map { answerEntity ->
                                answerEntity.findElement(By.className("answerText")).text +
                                        " / " + mindEntity.findElement(By.className("mindText")).text +
                                        " / " + answerEntity.findElement(By.className("answerUser")).text +
                                        " / " + if (STRICT) answerEntity.findElement(By.className("answerTime")).text else ""
                            }.toString()
                        }.pageRegExp()
        )
    }
    open fun WebApplicationObject.checkUsers() {
        if (CHECK_RENDERS) {
            Assert.assertEquals((1..visibleUsers.size).fold("") { acc: String, _ -> "${acc}User\n" }, checkRenderLog(js))
        }

        Assert.assertEquals("Different Users",
                visibleUsers
                        .sortedBy { it.name }
                        .joinToString("\n") { "${it.name} / ${it.country} / ${isFriendToCurr(it)} / ${isMateToCurr(it)}" }
                        .dbRegExp()
                ,
                driver.findElements(By.className("userEntity"))
                        .joinToString("\n") { userEntity ->
                            userEntity.findElement(By.className("userName")).text +
                                    " / " + userEntity.findElement(By.className("userCountry")).text +
                                    " / " + (userEntity.findElements(By.className("badge-success")).size > 0
                                    || userEntity.findElements(By.className("badge-primary")).size > 0) +
                                    " / " + (userEntity.findElements(By.className("badge-success")).size > 0
                                    || userEntity.findElements(By.className("badge-secondary")).size > 0)
                        }.pageRegExp()
        )
    }
}