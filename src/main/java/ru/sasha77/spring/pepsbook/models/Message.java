package ru.sasha77.spring.pepsbook.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Document(collection = "messages")
public class Message {
    @Id
    private String  _id;
    private String  userName;
    private Integer userId;
    private String  whomName;
    private Integer whomId;
    private Date    time;
    private String  text;
    private Boolean unReady;

    @PersistenceConstructor
    public Message(String   _id,
                   String   userName,
                   Integer  userId,
                   String   whomName,
                   Integer  whomId,
                   Date     time,
                   String   text,
                   Boolean  unReady) {
        this._id        = _id;
        this.userName   = userName;
        this.userId     = userId;
        this.whomName   = whomName;
        this.whomId     = whomId;
        this.time       = time;
        this.text       = text;
        this.unReady    = unReady;
    }

    public Message(String   userName,
                   Integer  userId,
                   String   whomName,
                   Integer  whomId,
                   Date     time,
                   String   text,
                   Boolean  unReady) {
        this.userName   = userName;
        this.userId     = userId;
        this.whomName   = whomName;
        this.whomId     = whomId;
        this.time       = time;
        this.text       = text;
        this.unReady    = unReady;
    }

}