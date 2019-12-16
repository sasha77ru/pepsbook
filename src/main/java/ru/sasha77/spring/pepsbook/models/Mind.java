package ru.sasha77.spring.pepsbook.models;


import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name="minds")
public class Mind implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    private String text;

    @OneToMany(mappedBy = "mind", fetch = FetchType.EAGER)
    @OrderBy("time")
    @ToString.Exclude
    private List<Answer> answers = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    private Date time = new Date();

	@ManyToOne
	@JoinColumn(name = "user_id",referencedColumnName = "id", nullable = false)
    private User user;

	public Mind(String text, User user) {
		this.text = text;
		this.user = user;
	}

    public Mind(String text, User user, Date time) {
		this.text = text;
		this.user = user;
		this.time = time;
	}

}

