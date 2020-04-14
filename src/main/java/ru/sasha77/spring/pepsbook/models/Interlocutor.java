package ru.sasha77.spring.pepsbook.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "interlocutors")
public class Interlocutor {
    @Id private String  _id;
    private String  userName;
    private Integer userId;
    private String whoseName;
    private Integer whoseId;
    private Integer numNewMessages;
    private Boolean hasPreMessages;
    private Date    time;

    @PersistenceConstructor
    public Interlocutor(String   _id,
                        String   userName,
                        Integer  userId,
                        String   whoseName,
                        Integer  whoseId,
                        Integer  numNewMessages,
                        Boolean  hasPreMessages,
                        Date     time) {
        this._id            = _id;
        this.userName       = userName;
        this.userId         = userId;
        this.whoseName      = whoseName;
        this.whoseId        = whoseId;
        this.numNewMessages = numNewMessages;
        this.hasPreMessages = hasPreMessages;
        this.time           = time;
    }

    public Interlocutor(String   userName,
                        Integer  userId,
                        String   whoseName,
                        Integer  whoseId,
                        Integer  numNewMessages,
                        Boolean  hasPreMessages,
                        Date     time) {
        this.userName       = userName;
        this.userId         = userId;
        this.whoseName      = whoseName;
        this.whoseId        = whoseId;
        this.numNewMessages = numNewMessages;
        this.hasPreMessages = hasPreMessages;
        this.time           = time;
    }
}