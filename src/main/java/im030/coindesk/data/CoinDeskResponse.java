package im030.coindesk.data;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public class CoinDeskResponse {
    public final JsonNode time;
    public final String disclaimer;
    public final String chartName;
    public final Map<String, ExchangeRate> bpi;

    public CoinDeskResponse() {
        time = null;
        bpi = null;
        disclaimer = null;
        chartName = null;
    }
    public CoinDeskResponse(JsonNode time, Map<String, ExchangeRate> bpi, String disclaimer, String chartName) {
        this.time = time;
        this.bpi = bpi;
        this.disclaimer = disclaimer;
        this.chartName = chartName;
    }
}
