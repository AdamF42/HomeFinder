package it.adamf42.core.usecases;

import it.adamf42.core.domain.ad.Ad;
import it.adamf42.core.usecases.repositories.AdRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

public interface CreateAdUseCase
{
	@Data
	class Request
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

	@Data
	@AllArgsConstructor
	class Response {
		private Ad ad;
	}

	Response execute(Request requestData);

}

class AdMapper {
	public static AdRepository.DbAd mapRequestToDbAd(CreateAdUseCase.Request request) {
		AdRepository.DbAd dbAd = new AdRepository.DbAd();
		dbAd.setCity(request.getCity());
		dbAd.setArea(request.getArea());
		dbAd.setStreet(request.getStreet());
		dbAd.setTitle(request.getTitle());
		dbAd.setPrice(request.getPrice());
		dbAd.setSquareMeters(request.getSquareMeters());
		dbAd.setFloor(request.getFloor());
		dbAd.setCondominiumFees(request.getCondominiumFees());
		dbAd.setEnergyRating(request.getEnergyRating());
		dbAd.setRooms(request.getRooms());
		dbAd.setBail(request.getBail());
		dbAd.setUrl(request.getUrl());
		dbAd.setPublisher(request.getPublisher());
		return dbAd;
	}

	public static Ad mapDbAdToAd(AdRepository.DbAd dbAd) {
		Ad ad = new Ad();
		ad.setCity(dbAd.getCity());
		ad.setArea(dbAd.getArea());
		ad.setStreet(dbAd.getStreet());
		ad.setTitle(dbAd.getTitle());
		ad.setPrice(dbAd.getPrice());
		ad.setSquareMeters(dbAd.getSquareMeters());
		ad.setFloor(dbAd.getFloor());
		ad.setCondominiumFees(dbAd.getCondominiumFees());
		ad.setEnergyRating(dbAd.getEnergyRating());
		ad.setRooms(dbAd.getRooms());
		ad.setBail(dbAd.getBail());
		ad.setUrl(dbAd.getUrl());
		ad.setPublisher(dbAd.getPublisher());
		return ad;
	}
}
