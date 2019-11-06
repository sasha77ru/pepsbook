package ru.sasha77.spring.pepsbook

import org.aspectj.lang.JoinPoint
import org.junit.Assert
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.ImportResource
import org.springframework.stereotype.Component

/**
 * Set of methods that check browser page on WAO (see testClasses.svg)
 */
@Component
open class Checkers {
    open fun WebApplicationObject.checkMinds() {
        Assert.assertEquals("Different Minds",
                visibleMinds.joinToString("\n") { it.toString() }
                        .replace(Regex("^ +| +(?= )| +$", RegexOption.MULTILINE), "")
                        .replace(Regex("\\[ "), "[")
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
                        .replace(Regex("^ +| +(?= )| +$", RegexOption.MULTILINE), "")
                        .replace(Regex("\\[ "), "[")
        )
    }
    open fun WebApplicationObject.checkUsers() {
        Assert.assertEquals("Different Users",
                visibleUsers
                        .sortedBy { it.name }
                        .joinToString("\n") { "${it.name} / ${it.country} / ${isFriendToCurr(it)} / ${isMateToCurr(it)}" }
                        .replace(Regex("^ +| +(?= )| +$", RegexOption.MULTILINE), "")
                ,
                driver.findElements(By.className("userEntity"))
                        .joinToString("\n") { userEntity ->
                            userEntity.findElement(By.className("userName")).text +
                                    " / " + userEntity.findElement(By.className("userCountry")).text +
                                    " / " + (userEntity.findElements(By.className("badge-success")).size > 0
                                    || userEntity.findElements(By.className("badge-primary")).size > 0) +
                                    " / " + (userEntity.findElements(By.className("badge-success")).size > 0
                                    || userEntity.findElements(By.className("badge-secondary")).size > 0)
                        }.replace(Regex("^ +| +(?= )| +$", RegexOption.MULTILINE), "")
        )
    }
}