package it.adamf42.core.usecases.user.repositories;

import lombok.AllArgsConstructor;
import lombok.Data;

public interface UserRepository {

	DbUser save(DbUser DbUser);

	DbUser findByChatId(String chatId);

	boolean existsByChatId(Long chatId);

	DbUser update(DbUser DbUser);

	@Data
	@AllArgsConstructor
	class DbUser {
		private Long chatId;
		private Integer maxPrice;
		private Integer minPrice;
		private String city;
	}
}