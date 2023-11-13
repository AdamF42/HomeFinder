package it.adamf42.core.domain.user;

import lombok.Data;

@Data
public class User
{
	Long chatId;
	Integer maxPrice;
	Integer minPrice;
	String city;
}
