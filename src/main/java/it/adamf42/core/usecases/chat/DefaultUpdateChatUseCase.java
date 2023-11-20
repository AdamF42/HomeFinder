package it.adamf42.core.usecases.chat;

import it.adamf42.core.domain.chat.Chat;
import it.adamf42.core.usecases.chat.repositories.ChatRepository;

public class DefaultUpdateChatUseCase implements UpdateChatUseCase
{

	private final ChatRepository ChatRepository;

	public DefaultUpdateChatUseCase(ChatRepository ChatRepository)
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
		ChatRepository.DbChat chat = mapRequestToDbChat(requestData);
		return new Response(mapDbChatToChat(this.ChatRepository.update(chat)));
	}

	private static ChatRepository.DbChat mapRequestToDbChat(Request requestData)
	{
        return new ChatRepository.DbChat(requestData.getChat().getChatId(), requestData.getChat().getMaxPrice(),
				requestData.getChat().getMinPrice(), requestData.getChat().getCity(), requestData.getChat().getIsActive());
	}

	private Chat mapDbChatToChat(ChatRepository.DbChat dbChat)
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
