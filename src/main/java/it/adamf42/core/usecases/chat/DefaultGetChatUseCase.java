package it.adamf42.core.usecases.chat;

import it.adamf42.core.domain.chat.Chat;
import it.adamf42.core.usecases.chat.repositories.ChatRepository;

public class DefaultGetChatUseCase implements UpdateChatUseCase
{

	private final ChatRepository ChatRepository;

	public DefaultGetChatUseCase(ChatRepository ChatRepository)
	{
		this.ChatRepository = ChatRepository;
	}

	@Override
	public Response execute(Request requestData) throws NotPresentException
	{
		if (!this.ChatRepository.existsByChatId(requestData.getChat().getChatId()))
		{
			throw new NotPresentException();
		}
		return new Response(mapDbUserToUser(this.ChatRepository.findByChatId(requestData.getChat().getChatId())));
	}



	private Chat mapDbUserToUser(it.adamf42.core.usecases.chat.repositories.ChatRepository.DbChat dbChat)
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

		return chat;
	}

}
