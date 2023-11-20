package it.adamf42.core.domain.ad;

import lombok.Data;

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
}