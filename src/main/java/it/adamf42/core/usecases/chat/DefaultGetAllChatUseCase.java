package it.adamf42.core.usecases.chat;

import it.adamf42.core.domain.chat.Chat;
import it.adamf42.core.usecases.chat.repositories.ChatRepository;

import java.util.stream.Collectors;

public class DefaultGetAllChatUseCase implements GetAllChatUseCase
{

	private final ChatRepository ChatRepository;

	public DefaultGetAllChatUseCase(ChatRepository ChatRepository)
	{
		this.ChatRepository = ChatRepository;
	}

	@Override
	public Response execute(Request requestData)
	{
		return new Response(this.ChatRepository.getAll().stream().map(this::mapDbUserToUser).collect(Collectors.toList()));
	}



	private Chat mapDbUserToUser(ChatRepository.DbChat dbChat)
	{
		if (dbChat == null)
		{
			return null;
		}

		Chat chat = new Chat();
		chat.setChatId(dbChat.getChatId());
		chat.setMaxPrice(dbChat.getMaxPrice());
		chat.setMinPrice(dbChat.getMinPrice());
		chat.setCity(dbChat.getCity());
		chat.setIsActive(dbChat.getIsActive());

		return chat;
	}

}
