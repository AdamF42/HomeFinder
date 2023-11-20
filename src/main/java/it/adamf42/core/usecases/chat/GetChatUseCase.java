package it.adamf42.core.usecases.chat;

import it.adamf42.core.domain.chat.Chat;
import lombok.AllArgsConstructor;
import lombok.Data;

public interface GetChatUseCase
{
	@Data
	class Request
	{
		private Long chatId;
	}

	@Data
	@AllArgsConstructor
	class Response {
		private Chat chat;
	}

	class NotPresentException extends Exception {

	}

	Response execute(Request requestData) throws NotPresentException;

}
