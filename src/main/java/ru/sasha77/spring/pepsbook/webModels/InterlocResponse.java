package ru.sasha77.spring.pepsbook.webModels;

import lombok.Data;
import ru.sasha77.spring.pepsbook.models.Interlocutor;
import static ru.sasha77.spring.pepsbook.MyUtilities.myDate;

@Data
public class InterlocResponse {
    private String  _id;
    private String  userName;
    private Integer userId;
    private String  whoseName;
    private Integer whoseId;
    private Integer numNewMessages;
    private Boolean hasPreMessages;
    private String  time;

    public InterlocResponse(Interlocutor interlocutor) {
        this._id            = interlocutor.get_id();
        this.userName       = interlocutor.getUserName();
        this.userId         = interlocutor.getUserId();
        this.whoseName      = interlocutor.getWhoseName();
        this.whoseId        = interlocutor.getWhoseId();
        this.numNewMessages = interlocutor.getNumNewMessages();
        this.hasPreMessages = interlocutor.getHasPreMessages();
        this.time           = myDate(interlocutor.getTime());
    }
}