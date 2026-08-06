package im030.coindesk.service;

import im030.coindesk.data.Currency;
import im030.coindesk.repository.CurrencyRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }


    @Cacheable("currencies")
    public Map<String, String> getNames() {
        return currencyRepository.findAll().stream()
                .collect(Collectors.toMap(Currency::getCode, Currency::getChineseName));
    }

    public boolean exists(String code) {
        return currencyRepository.existsById(code);
    }

    public void delete(String code) {
        currencyRepository.deleteById(code);
    }

    public Currency save(Currency data) {
        return currencyRepository.save(data);
    }
}
