package ru.sasha77.spring.pepsbook;

public class UserSimple {
    private Integer id;
    private String name;
    private String email;

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@SuppressWarnings("unused")
	public String getEmail() {
		return email;
	}

	public UserSimple(Integer id, String name, String email) {
		this.id = id;
		this.name = name;
		this.email = email;
	}
}

