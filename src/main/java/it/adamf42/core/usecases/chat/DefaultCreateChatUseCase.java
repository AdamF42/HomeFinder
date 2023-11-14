package it.adamf42.core.usecases.chat;

import it.adamf42.core.domain.chat.Chat;
import it.adamf42.core.usecases.chat.repositories.ChatRepository;

public class DefaultCreateChatUseCase implements CreateChatUseCase {

    private final ChatRepository ChatRepository;

    public DefaultCreateChatUseCase(ChatRepository ChatRepository) {
        this.ChatRepository = ChatRepository;
    }

    @Override
    public Response execute(Request requestData) throws AlreadyPresentException {
        it.adamf42.core.usecases.chat.repositories.ChatRepository.DbChat user = mapRequestToDbUser(requestData);
        if (this.ChatRepository.existsByChatId(requestData.getChat().getChatId())) {
            throw new AlreadyPresentException();
        }
        return new Response(mapDbUserToUser(this.ChatRepository.save(user)));
    }

    private static it.adamf42.core.usecases.chat.repositories.ChatRepository.DbChat mapRequestToDbUser(Request requestData) {
        return new ChatRepository.DbChat(requestData.getChat().getChatId(), requestData.getChat().getMaxPrice(),
                requestData.getChat().getMinPrice(), requestData.getChat().getCity());
    }

    private Chat mapDbUserToUser(it.adamf42.core.usecases.chat.repositories.ChatRepository.DbChat dbChat) {
        if (dbChat == null) {
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
