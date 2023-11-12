package it.adamf42.core.usecases.ad;

import it.adamf42.core.usecases.ad.repositories.AdRepository;

public class DefaultCreateAdUseCase implements CreateAdUseCase
{

	private final AdRepository adRepository;

	public DefaultCreateAdUseCase(AdRepository adRepository)
	{
		this.adRepository = adRepository;
	}

	@Override
	public Response execute(Request requestData) throws AlreadyPresentException
	{
		AdRepository.DbAd ad = AdMapper.mapRequestToDbAd(requestData);
		if (this.adRepository.isPresent(ad)) {
			throw new AlreadyPresentException();
		}
		return new Response(AdMapper.mapDbAdToAd(this.adRepository.save(ad)));
	}

}
