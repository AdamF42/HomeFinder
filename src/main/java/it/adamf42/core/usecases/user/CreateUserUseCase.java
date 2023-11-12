package it.adamf42.core.usecases.user;

import it.adamf42.core.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;

public interface CreateUserUseCase
{
	@Data
	class Request
	{
		private String chatId;
		private Integer maxPrice;
		private Integer minPrice;
		private String city;
	}

	@Data
	@AllArgsConstructor
	class Response {
		private User user;
	}

	class AlreadyPresentException extends Exception {

	}

	Response execute(Request requestData) throws AlreadyPresentException;

}
