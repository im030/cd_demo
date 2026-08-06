package im030.coindesk.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
public class ExchangeRate {
    @Id
    private String code;

    private String symbol;

    private String description;

    @JsonProperty("rate_float")
    @Column(name = "rate_float")
    private BigDecimal rateFloat;

    @Column(name = "rate")
    private String rate;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public String getCode() {
        return code;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getRateFloat() {
        return rateFloat;
    }

    public String getRate() {
        return rate;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public void setRateFloat(BigDecimal rate) {
        rateFloat = rate;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
