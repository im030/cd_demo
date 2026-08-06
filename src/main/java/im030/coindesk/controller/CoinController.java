package im030.coindesk.controller;

import im030.coindesk.data.*;
import im030.coindesk.service.ApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CoinController {

    private static final Logger log = LoggerFactory.getLogger(CoinController.class);
    private final ApiService apiService;

    public CoinController(ApiService svc) {
        apiService = svc;
    }


    @GetMapping("/get")
    public AllRatesResponse get() {
        return apiService.getCoinDeskInfo();
    }

    @GetMapping("/get/{code}")
    public ExchangeRateDto get(@PathVariable String code) {
        ExchangeRateDto rate = apiService.get(code);

        if (rate == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Currency not found");
        }

        return rate;
    }

    @GetMapping("/refresh")
    public String refresh() {
        try {
            apiService.getCoinDeskInfo();
        } catch (RestClientException e) {
            log.error("failed to fetch data from CoinDesk API", e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    e.getMessage());
        } catch (NullPointerException e) {
            log.error("", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage());
        }

        return "OK";
    }

    @GetMapping("/del/{code}")
    public String delete(@PathVariable String code) {
        apiService.delete(code);
        return "OK";
    }

    @PostMapping("/update")
    public String update(@RequestBody Currency name) {
        apiService.update(name);
        return "OK";
    }

    @PostMapping("/create")
    public String create(@RequestBody Currency name) {
        apiService.create(name);
        return "OK";
    }


}
