package it.adamf42.core.domain.ad;

import lombok.Data;

import java.util.Objects;

@Data
public class Ad {
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


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Ad ad = (Ad) o;
		return Objects.equals(city, ad.city) && Objects.equals(area, ad.area) && Objects.equals(street, ad.street) && Objects.equals(title, ad.title) && Objects.equals(price, ad.price) && Objects.equals(squareMeters, ad.squareMeters) && Objects.equals(floor, ad.floor) && Objects.equals(condominiumFees, ad.condominiumFees) && Objects.equals(energyRating, ad.energyRating) && Objects.equals(rooms, ad.rooms) && Objects.equals(bail, ad.bail) && Objects.equals(url, ad.url) && Objects.equals(publisher, ad.publisher);
	}

	@Override
	public int hashCode() {
		return Objects.hash(city, area, street, title, price, squareMeters, floor, condominiumFees, energyRating, rooms, bail, url, publisher);
	}
}