package im030.coindesk.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import im030.coindesk.utils.TimeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllRatesResponse {
    @JsonProperty("updated_at")
    private String updatedAt;

    public Map<String, ExchangeRateDto> bpi;

    public static AllRatesResponse of(CoinDeskResponse data, Map<String, String> currencyMap) {
        AllRatesResponse o = new AllRatesResponse();
        o.updatedAt = TimeUtils.formattedTimeFromJson(data.time);
        o.bpi = new HashMap<>();
        for (Map.Entry<String, ExchangeRate> e : data.bpi.entrySet()) {
            o.bpi.put(e.getKey(), ExchangeRateDto.of(e.getValue(), currencyMap));
        }
        return o;
    }
}
