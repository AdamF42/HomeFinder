package it.adamf42.core.usecases.user;

import it.adamf42.core.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;

public interface UpdateUserUseCase
{
	@Data
	class Request
	{
		private User user;
	}

	@Data
	@AllArgsConstructor
	class Response {
		private User user;
	}

	class NotPresentException extends Exception {

	}

	Response execute(Request requestData) throws NotPresentException;

}
