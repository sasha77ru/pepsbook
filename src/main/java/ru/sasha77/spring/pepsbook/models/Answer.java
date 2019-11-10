package ru.sasha77.spring.pepsbook.models;


import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name="answers")
public class Answer {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    private String text;

    @ManyToOne
    @JoinColumn(name = "mind_id",referencedColumnName = "id", nullable = false)
    private Mind mind;

	@ManyToOne
	@JoinColumn(name = "user_id",referencedColumnName = "id", nullable = false)
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    private Date time = new Date();

	public Answer(String text, Mind mind, User user) {
		this.text = text;
		this.mind = mind;
		this.user = user;
	}
	public Answer(String text, Mind mind, User user, Date time) {
		this.text = text;
		this.mind = mind;
		this.user = user;
		this.time = time;
	}
}

