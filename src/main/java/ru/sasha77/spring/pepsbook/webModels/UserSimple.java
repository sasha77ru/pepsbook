package ru.sasha77.spring.pepsbook.webModels;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.sasha77.spring.pepsbook.models.User;

@Data
@AllArgsConstructor
public class UserSimple {
    private Integer id;
    private String name;

    public UserSimple(User user) {
    	this.id = user.getId();
    	this.name = user.getName();
	}
}

