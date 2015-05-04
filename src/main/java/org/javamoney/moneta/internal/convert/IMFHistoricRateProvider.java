package org.javamoney.moneta.internal.convert;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.money.CurrencyUnit;
import javax.money.convert.ConversionQuery;
import javax.money.convert.ExchangeRate;
import javax.money.convert.ProviderContext;
import javax.money.convert.ProviderContextBuilder;
import javax.money.convert.RateType;
import javax.money.spi.Bootstrap;

import org.javamoney.moneta.internal.convert.RateIMFReadingHandler.RateIMFResult;
import org.javamoney.moneta.spi.LoaderService;

public class IMFHistoricRateProvider extends AbstractIMFRateProvider {

    private static final Logger LOG = Logger.getLogger(IMFHistoricRateProvider.class.getName());

    private static final String DATA_ID = IMFHistoricRateProvider.class.getSimpleName();

	private static final ProviderContext CONTEXT = ProviderContextBuilder.of("IMF-HIST", RateType.HISTORIC)
	            .set("providerDescription", "Historic International Monetary Fond").set("days", 0).build();


	private final List<YearMonth> cachedHistoric = new ArrayList<>();
	public IMFHistoricRateProvider() {
		super(CONTEXT);
		 LoaderService loader = Bootstrap.getService(LoaderService.class);
	        loader.addLoaderListener(this, DATA_ID);
	        try {
	            loader.loadData(DATA_ID);
	        } catch (IOException e) {
	        	LOG.log(Level.WARNING, "Error loading initial data from IMF provider...", e);
	        }
	}

	@Override
	public ExchangeRate getExchangeRate(ConversionQuery conversionQuery) {
		LocalDate[] times = getQueryDates(conversionQuery);
		if(Objects.isNull(times)) {
			return super.getExchangeRate(conversionQuery);
		}

		for (YearMonth yearMonth : Stream.of(times).map(YearMonth::from)
				.collect(Collectors.toSet())) {

			if(!cachedHistoric.contains(yearMonth)){
				Map<IMFHistoricalType, InputStream> resources = IMFRemoteSearch.INSTANCE.getResources(yearMonth);
				loadFromRemote(resources);
				cachedHistoric.add(yearMonth);
			}
		}
		return super.getExchangeRate(conversionQuery);
	}

	private void loadFromRemote(Map<IMFHistoricalType, InputStream> resources) {
		try {
			for(IMFHistoricalType type: resources.keySet()) {
				RateIMFResult result = handler.read(resources.get(type));
				combine(result.getSdrToCurrency(), this.sdrToCurrency);
				combine(result.getCurrencyToSdr(), this.currencyToSdr);
			}
	    } catch (Exception e) {
	    	LOG.log(Level.SEVERE, "Error", e);
	    }
	}

	private Map<CurrencyUnit, List<ExchangeRate>> combine(Map<CurrencyUnit, List<ExchangeRate>> source, Map<CurrencyUnit, List<ExchangeRate>> destination) {
		for(CurrencyUnit currency: source.keySet()) {
			destination.putIfAbsent(currency, new ArrayList<>());
			List<ExchangeRate> rates = source.get(currency);
			destination.merge(currency, rates, IMFHistoricRateProvider::merge);
		}
		return destination;
	}
	private static List<ExchangeRate> merge(List<ExchangeRate> ratesA, List<ExchangeRate> ratesB) {
		ratesA.addAll(ratesB);
		ratesA.sort(COMPARATOR_EXCHANGE_BY_LOCAL_DATE.reversed());
		return ratesA;
	}
}
