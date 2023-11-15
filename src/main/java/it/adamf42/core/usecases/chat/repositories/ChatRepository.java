package it.adamf42.core.usecases.chat.repositories;

import lombok.AllArgsConstructor;
import lombok.Data;

public interface ChatRepository {

	DbChat save(DbChat DbChat);

	DbChat findByChatId(Long chatId);

	boolean existsByChatId(Long chatId);

	DbChat update(DbChat DbChat);

	@Data
	@AllArgsConstructor
	class DbChat {
		private Long chatId;
		private Integer maxPrice;
		private Integer minPrice;
		private String city;
	}
}