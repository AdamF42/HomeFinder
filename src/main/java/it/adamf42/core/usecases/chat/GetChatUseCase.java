package it.adamf42.core.usecases.chat;

import it.adamf42.core.domain.chat.Chat;
import lombok.AllArgsConstructor;
import lombok.Data;

public interface GetChatUseCase
{
	@Data
	class Request
	{
		private Chat chat;
	}

	@Data
	@AllArgsConstructor
	class Response {
		private Chat chat;
	}

	class AlreadyPresentException extends Exception {

	}

	Response execute(Request requestData) throws AlreadyPresentException;

}
