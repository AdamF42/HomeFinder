package it.adamf42.core.usecases.chat;

import it.adamf42.core.domain.chat.Chat;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

public interface GetAllChatUseCase
{
	@Data
	class Request
	{
	}

	@Data
	@AllArgsConstructor
	class Response {
		private final List<Chat> chats;
	}

	Response execute(Request requestData);

}
