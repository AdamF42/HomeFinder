package it.adamf42.core.usecases.chat.repositories;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

public interface ChatRepository {

	DbChat save(DbChat DbChat);

	DbChat findByChatId(Long chatId);

	boolean existsByChatId(Long chatId);

	DbChat update(DbChat DbChat);

	List<DbChat> getAll();

	@Data
	@AllArgsConstructor
	class DbChat {
		private Long chatId;
		private Integer maxPrice;
		private Integer minPrice;
		private String city;
	}
}