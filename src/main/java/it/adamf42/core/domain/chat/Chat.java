package it.adamf42.core.domain.chat;

import lombok.Data;

@Data
public class Chat
{
	Long chatId;
	Integer maxPrice;
	Integer minPrice;
	String city;
	Boolean isActive;
}
