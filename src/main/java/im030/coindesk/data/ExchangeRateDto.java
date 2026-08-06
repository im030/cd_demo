package im030.coindesk.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import im030.coindesk.service.ApiService;
import im030.coindesk.utils.TimeUtils;

import javax.persistence.Column;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public class ExchangeRateDto {

    @JsonProperty("code")
    private String code;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("chinese_name")
    private String chineseName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("rate_float")
    private BigDecimal rateFloat;

    @JsonProperty("rate")
    private String rate;

    @JsonProperty("updated_at")
    private String updatedAt;

    public String getCode() {
        return code;
    }

    public String getRate() {
        return rate;
    }

    public BigDecimal getRateFloat() {
        return rateFloat;
    }

    public String getDescription() {
        return description;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public static ExchangeRateDto of(ExchangeRate rate, Map<String, String> currencyMap) {
        ExchangeRateDto o = new ExchangeRateDto();
        o.code = rate.getCode();
        o.symbol = rate.getSymbol();
        o.description = rate.getDescription();
        o.rateFloat = rate.getRateFloat();
        o.rate = rate.getRate();
        o.chineseName = currencyMap.get(o.code);
        o.updatedAt = rate.getUpdatedAt()
                .atZoneSameInstant(ZoneId.of("Asia/Taipei"))
                .format(TimeUtils.timeFormatter);
        return o;
    }
}
