package it.adamf42.core.usecases;

import it.adamf42.core.usecases.repositories.AdRepository;

public class DefaultCreateAdUseCase implements CreateAdUseCase
{

	private final AdRepository adRepository;

	DefaultCreateAdUseCase(AdRepository adRepository)
	{
		this.adRepository = adRepository;
	}

	@Override
	public Response execute(Request requestData)
	{
		return new Response(AdMapper.mapDbAdToAd(this.adRepository.save(AdMapper.mapRequestToDbAd(requestData))));
	}

}
