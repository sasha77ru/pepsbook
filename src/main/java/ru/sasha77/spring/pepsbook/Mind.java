package ru.sasha77.spring.pepsbook;


import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="minds")
public class Mind {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    private String text;

    @Temporal(TemporalType.TIMESTAMP)
    private Date time = new Date();

	public Date getTime() {
		return time;
	}

	@ManyToOne
	@JoinColumn(name = "user_id",referencedColumnName = "id", nullable = false)
    private User user;

    @SuppressWarnings("unused")
	public Mind() {}

	public Mind(String text, User user) {
		this.text = text;
		this.user = user;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Mind{" +
				"id=" + id +
				", text='" + text + '\'' +
				", user=" + user +
				'}';
	}
}

