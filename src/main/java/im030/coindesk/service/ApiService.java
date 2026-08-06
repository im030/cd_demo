package im030.coindesk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import im030.coindesk.data.*;
import im030.coindesk.repository.ExchangeRateRepository;
import im030.coindesk.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class ApiService {

    private static final Logger log = LoggerFactory.getLogger(ApiService.class);

    private final RestTemplate restTemplate;
    private final ExchangeRateRepository rateRepository;
    private final CurrencyService currencyService;
    private final Environment env;
    private final ObjectMapper objMapper;

    public ApiService(RestTemplate restTemplate,
                      ExchangeRateRepository rateRepository,
                      Environment env,
                      ObjectMapper objMapper,
                      CurrencyService currencyService
    ) {
        this.restTemplate = restTemplate;
        this.rateRepository = rateRepository;
        this.env = env;
        this.objMapper = objMapper;
        this.currencyService = currencyService;
    }

    public AllRatesResponse getCoinDeskInfo() throws RestClientException, NullPointerException {
        final String url = env.getProperty("coindesk.api-url");
        if (!StringUtils.hasText(url)) throw new NullPointerException("API URL not set");
        CoinDeskResponse resp = restTemplate.getForObject(url, CoinDeskResponse.class);


        try {
            log.info("obtained data from coindesk API: {}", objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resp));
        } catch (JsonProcessingException ignored) {}

        if (resp != null && resp.bpi != null) {
            OffsetDateTime updatedAt = TimeUtils.updatedAtFrom(resp.time);
            for (ExchangeRate rate : resp.bpi.values()) {
                if (rate.getUpdatedAt() == null) {
                    rate.setUpdatedAt(updatedAt);
                }
            }
            rateRepository.saveAll(resp.bpi.values());
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Unexpected response");
        }

        return AllRatesResponse.of(resp, currencyService.getNames());
    }

    public ExchangeRateDto get(String code) throws ResponseStatusException {
        Optional<ExchangeRate> ret = rateRepository.findById(code);
        if (ret.isPresent()) {
            log.info("API resp: return cached value from DB");
            return ExchangeRateDto.of(ret.orElse(null), currencyService.getNames());
        }

        AllRatesResponse resp = getCoinDeskInfo();
        if (resp.bpi == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Currency not found");
        }
        ExchangeRateDto rate = resp.bpi.get(code);
        if (rate != null) {
            log.info("API resp: sending freshly fetched data for {}", code);
            return rate;
        }

        log.error("requested rate data({}) not found", code);
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Currency not found");
    }

    @CacheEvict(value = "currencies", allEntries = true)
    public void delete(String code) throws ResponseStatusException {
        if (!currencyService.exists(code)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Currency not found");
        }
        currencyService.delete(code);
    }

    @CacheEvict(value = "currencies", allEntries = true)
    public void create(Currency data) {
        if (currencyService.exists(data.getCode())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Currency exists");
        }
        currencyService.save(data);
    }

    @CacheEvict(value = "currencies", allEntries = true)
    public void update(Currency data) {
        if (!currencyService.exists(data.getCode())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Currency not found");
        }
        currencyService.save(data);
    }
}
