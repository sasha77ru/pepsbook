package ru.sasha77.spring.pepsbook

fun getNewCookie() = Array(20) { ('A'..'Z').random() }.joinToString("")

