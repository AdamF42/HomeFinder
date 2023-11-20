package it.adamf42.core.usecases.chat;

import it.adamf42.core.domain.chat.Chat;
import it.adamf42.core.usecases.chat.repositories.ChatRepository;

public class DefaultGetChatUseCase implements GetChatUseCase
{

	private final ChatRepository ChatRepository;

	public DefaultGetChatUseCase(ChatRepository ChatRepository)
	{
		this.ChatRepository = ChatRepository;
	}

	@Override
	public Response execute(Request requestData) throws NotPresentException
	{
		if (!this.ChatRepository.existsByChatId(requestData.getChatId()))
		{
			throw new NotPresentException();
		}
		return new Response(mapDbUserToUser(this.ChatRepository.findByChatId(requestData.getChatId())));
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
