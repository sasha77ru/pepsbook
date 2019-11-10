package ru.sasha77.spring.pepsbook.webModels;

import lombok.Data;

@Data
public class UserSimple {
    private Integer id;
    private String name;
	@SuppressWarnings("unused")
	private String email;

	public UserSimple(Integer id, String name, String email) {
		this.id = id;
		this.name = name;
		this.email = email;
	}
}

