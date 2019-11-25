package ru.sasha77.spring.pepsbook.webModels;

import lombok.Data;
import ru.sasha77.spring.pepsbook.models.User;

@Data
public class UsersResponse {
    private Integer id;
    private String name;
	private String country;
	private Boolean isFriend;
	private Boolean isMate;

	public UsersResponse (User user,User currUser) {
		this.id = user.getId();
		this.name = user.getName();
		this.country = user.getCountry();
		this.isFriend = currUser.getFriends().contains(user);
		this.isMate = currUser.getMates().contains(user);
	}
}

