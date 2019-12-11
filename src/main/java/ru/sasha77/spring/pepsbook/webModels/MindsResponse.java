package ru.sasha77.spring.pepsbook.webModels;

import lombok.Data;
import ru.sasha77.spring.pepsbook.models.Answer;
import ru.sasha77.spring.pepsbook.models.Mind;
import ru.sasha77.spring.pepsbook.models.User;

import java.util.List;
import java.util.stream.Collectors;

import static ru.sasha77.spring.pepsbook.MyUtilities.myDate;

@Data
public class MindsResponse {
	@Data
	class AnswersResponse {
		private Integer id;
		private String text;
		private String author;
		private String time;
		private Boolean isAuthor;

		AnswersResponse (Answer answer, User currUser) {
			this.id = answer.getId();
			this.text = answer.getText();
			this.author = answer.getUser().getName();
			this.time = myDate(answer.getTime());
			this.isAuthor = answer.getUser() == currUser;
		}
		AnswersResponse (Answer answer, String currUserLogin) {
			this.id = answer.getId();
			this.text = answer.getText();
			this.author = answer.getUser().getName();
			this.time = myDate(answer.getTime());
			this.isAuthor = answer.getUser().getUsername().equals(currUserLogin);
		}
	}
    private Integer id;
    private String text;
	private String author;
	private String time;
	private Boolean isAuthor;
	private List<AnswersResponse> answers;

	public MindsResponse (Mind mind, User currUser) {
		this.id = mind.getId();
		this.text = mind.getText();
		this.author = mind.getUser().getName();
		this.time = myDate(mind.getTime());
		this.isAuthor = mind.getUser() == currUser;
		this.answers = mind.getAnswers().stream()
				.map(it -> new AnswersResponse(it,currUser))
				.collect(Collectors.toList());
	}
	public MindsResponse (Mind mind, String currUserLogin) {
		this.id = mind.getId();
		this.text = mind.getText();
		this.author = mind.getUser().getName();
		this.time = myDate(mind.getTime());
		this.isAuthor = mind.getUser().getUsername().equals(currUserLogin);
		this.answers = mind.getAnswers().stream()
				.map(it -> new AnswersResponse(it,currUserLogin))
				.collect(Collectors.toList());
	}
}

