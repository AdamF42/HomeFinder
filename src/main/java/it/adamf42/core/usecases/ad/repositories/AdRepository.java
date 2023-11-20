package it.adamf42.core.usecases.ad.repositories;

import lombok.Data;

public interface AdRepository
{
	DbAd save(DbAd dbAd);

	boolean isPresent(DbAd dbAd);

	@Data
	class DbAd
	{
		private String city;
		private String area;
		private String street;
		private String title;
		private Double price;
		private Double squareMeters;
		private Integer floor;
		private Double condominiumFees;
		private String energyRating;
		private Integer rooms;
		private Double bail;
		private String url;
		private String publisher;
	}
}

