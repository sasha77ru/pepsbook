export const restPrefix = "/rest/"
export const WAIT_BEFORE_SUBMIT_FILTER = 500;

export const mainMenuIds = {
    minds   : "mainMinds",
    users   : "mainUsers",
    friends : "mainFriends",
    mates   : "mainMates",
}

export var loc = {
    mainMenuTexts: {
        minds   : "Мысли",
        users   : "Другие",
        friends : "Друзья",
        mates   : "Почитатели",
        messages: "Сообщения"
    }
}
loc = {...loc,
    filterPlaceHolder   : "фильтр",
    toLogOut            : "Выдти",
    toAnswer            : "Ответить",
    toEdit              : "Редактировать",
    toDelete            : "Удалить",
    newMind             : "Новая мысль",
    publish             : "Опубликовать",
    mindIsTooLong       : "Слишком длинная мысль. Максимальная длинна 4000",
    newMindWhat         : {mind : "Мысль", answer : "Ответ"},
    loading             : "Загрузка",
    name                : "Имя",
    country             : "Страна",
    startMessaging      : "Написать сообщение",
    toFriends           : "В друзья",
    fromFriends         : "Из друзей",
    mutualFriend        : "Взаимный Друг",
    halfFriend          : "ПолуДруг",
    mate                : "Почитатель",
    messageIsTooLong    : "Слишком длинное сообщение. Максимальная длинна 4000",
    sendMessage         : "Отправить",
    noInterlocutors     : "Нет собеседников",
    addInterlocutors    : `Выберете собеседника в разделах `+
                          `${loc.mainMenuTexts.users}, ${loc.mainMenuTexts.friends}, ${loc.mainMenuTexts.mates} `+
                          ` и нажмите "Написать сообщение" в его всплывающем меню"`,
}

