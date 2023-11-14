package it.adamf42.core.usecases.user;

import it.adamf42.core.domain.user.User;
import it.adamf42.core.usecases.user.repositories.UserRepository;

public class DefaultCreateUserUseCase implements CreateUserUseCase {

    private final UserRepository UserRepository;

    public DefaultCreateUserUseCase(UserRepository UserRepository) {
        this.UserRepository = UserRepository;
    }

    @Override
    public Response execute(Request requestData) throws AlreadyPresentException {
        UserRepository.DbUser user = mapRequestToDbUser(requestData);
        if (this.UserRepository.existsByChatId(requestData.getUser().getChatId())) {
            throw new AlreadyPresentException();
        }
        return new Response(mapDbUserToUser(this.UserRepository.save(user)));
    }

    private static UserRepository.DbUser mapRequestToDbUser(Request requestData) {
        return new UserRepository.DbUser(requestData.getUser().getChatId(), requestData.getUser().getMaxPrice(),
                requestData.getUser().getMinPrice(), requestData.getUser().getCity());
    }

    private User mapDbUserToUser(UserRepository.DbUser dbUser) {
        if (dbUser == null) {
            return null;
        }

        User user = new User();
        user.setChatId(dbUser.getChatId());
        user.setMaxPrice(dbUser.getMaxPrice());
        user.setMinPrice(dbUser.getMinPrice());
        user.setCity(dbUser.getCity());

        return user;
    }

}