package ru.sasha77.spring.pepsbook;


import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static ru.sasha77.spring.pepsbook.ToolsKt.getNewCookie;

@Entity // This tells Hibernate to make a table out of this class
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    private String name;

	@Column(unique = true)
	private String email;

	private String country;

	@Column(unique = true)
	private String keyCookie;

	public Set<User> getFriends() {
		return friends;
	}

	public Set<User> getMates() {
		return mates;
	}

	@ManyToMany(/*cascade = {CascadeType.ALL},*/ fetch = FetchType.EAGER)
	@JoinTable(name="friendship",
			joinColumns = @JoinColumn(name="user_id"),
			inverseJoinColumns = @JoinColumn(name="friend_id"))
	private Set<User> friends = new HashSet<>();

	@ManyToMany(mappedBy = "friends", fetch = FetchType.EAGER)
	private Set<User> mates = new HashSet<>();

	@SuppressWarnings("unused")
	public User() {}

	public User(String name, String email, String country) {
		this.name = name;
		this.email = email;
		this.country = country;
		this.keyCookie = getNewCookie();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	@SuppressWarnings("unused")
	public void setEmail(String email) {
		this.email = email;
	}

	public String getCountry() {
		return country;
	}

	@SuppressWarnings("unused")
	public void setCountry(String country) {
		this.country = country;
	}

	public String getKeyCookie() {
		return keyCookie;
	}

	void setKeyCookie(String keyCookie) {
		this.keyCookie = keyCookie;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", name='" + name + '\'' +
				", email='" + email + '\'' +
				", country='" + country + '\'' +
				'}';
	}
}

