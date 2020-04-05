package ru.sasha77.spring.pepsbook.webModels;

import lombok.Data;
import ru.sasha77.spring.pepsbook.models.Message;

import static ru.sasha77.spring.pepsbook.MyUtilities.myDate;

@Data
public class MessagesResponse {
    private String  _id;
    private String  text;
    private String  userName;
    private String  userId;
    private String  whomName;
    private String  whomId;
    private String  time;
    private Boolean unReady;

    public MessagesResponse(Message message) {
        this._id        = message.get_id();
        this.text       = message.getText();
        this.userName   = message.getUserName();
        this.userId     = message.getUserId().toString();
        this.whomName   = message.getWhomName();
        this.whomId     = message.getWhomId().toString();
        this.time       = myDate(message.getTime());
        this.unReady    = message.getUnReady();
    }
}