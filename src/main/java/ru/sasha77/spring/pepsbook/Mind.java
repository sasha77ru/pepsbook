package ru.sasha77.spring.pepsbook;


import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name="minds")
public class Mind {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    private String text;

    @Temporal(TemporalType.TIMESTAMP)
    private Date time = new Date();

	@ManyToOne
	@JoinColumn(name = "user_id",referencedColumnName = "id", nullable = false)
    private User user;

	public Mind(String text, User user) {
		this.text = text;
		this.user = user;
	}

}

