package im030.coindesk.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import im030.coindesk.utils.TimeUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataConversionUnitTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private String rawCoindeskJson() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("coindesk.json")) {
            assertNotNull(in, "coindesk.json not found on test classpath");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void test1_convertsUpdatedIsoIntoOffsetDateTime() throws Exception {
        System.out.println("=== 1. 轉換 CoinDesk API 回傳時間  ===");
        CoinDeskResponse resp = mapper.readValue(rawCoindeskJson(), CoinDeskResponse.class);

        OffsetDateTime updatedAt = OffsetDateTime.parse(resp.time.get("updatedISO").asText());
        assertEquals(OffsetDateTime.parse("2024-09-02T07:07:20+00:00"), updatedAt);
        assertEquals(0, updatedAt.getHour() - 7);

        System.out.println("  測試內容:");
        System.out.println("    - 解析 coindesk.json 中的 updatedISO");
        System.out.println("    - 驗證可轉換為 OffsetDateTime，並與預期值 2024-09-02T07:07:20+00:00 一致");
        System.out.println("    - 驗證 updatedAt.getHour() - 7 == 0 (GMT+8 換算)");
        System.out.println("  測試結果: OK");
        System.out.println("    updatedISO = " + resp.time.get("updatedISO").asText());
        System.out.println("    OffsetDateTime = " + updatedAt);
        System.out.println("    getHour() - 7 = " + (updatedAt.getHour() - 7));
    }

    @Test
    void test2_convertsRawCoinDeskJsonIntoResponse() throws Exception {
        System.out.println("=== 2. 轉換 CoinDesk API 格式  ===");
        CoinDeskResponse resp = mapper.readValue(rawCoindeskJson(), CoinDeskResponse.class);

        assertNotNull(resp.time);
        assertEquals("2024-09-02T07:07:20+00:00", resp.time.get("updatedISO").asText());
        assertNotNull(resp.bpi);
        assertEquals(3, resp.bpi.size());
        for (ExchangeRate r : resp.bpi.values()) {
            r.setUpdatedAt(TimeUtils.updatedAtFrom(resp.time));
        }
        AllRatesResponse resp2 = AllRatesResponse.of(resp, Collections.<String, String>emptyMap());
        for (Map.Entry<String, ExchangeRateDto> entry : resp2.bpi.entrySet()) {
            ExchangeRateDto rate = entry.getValue();
            assertEquals(entry.getKey(), rate.getCode());
            assertTrue(rate.getSymbol().startsWith("&"));
            assertNotNull(rate.getDescription());
            assertNotNull(rate.getRate());
            assertNotNull(rate.getRateFloat());
        }
        ExchangeRateDto eur = resp2.bpi.get("EUR");
        assertEquals(new BigDecimal("52243.2865"), eur.getRateFloat());
        assertEquals("52,243.287", eur.getRate());
        assertEquals("&euro;", eur.getSymbol());
        assertEquals("Euro", eur.getDescription());
        assertEquals("2024/09/02 15:07:20", resp2.bpi.get("EUR").getUpdatedAt());

        System.out.println("  測試內容:");
        System.out.println("    - 解析 coindesk.json 為 CoinDeskResponse (time/bpi)");
        System.out.println("    - 以 TimeUtils.updatedAtFrom 填入各幣別 updatedAt");
        System.out.println("    - 經 AllRatesResponse.of 轉換為 DTO 格式，驗證 code/symbol/description/rate/rateFloat");
        System.out.println("    - 驗證 EUR 欄位數值與 updatedAt 格式化結果");
        System.out.println("  測試結果: OK");
        System.out.println("    updatedISO = " + resp.time.get("updatedISO").asText());
        for (Map.Entry<String, ExchangeRateDto> entry : resp2.bpi.entrySet()) {
            ExchangeRateDto rate = entry.getValue();
            System.out.printf("    %s => code=%s, symbol=%s, description=%s, rate=%s, rateFloat=%s, updatedAt=%s%n",
                    entry.getKey(), rate.getCode(), rate.getSymbol(), rate.getDescription(), rate.getRate(), rate.getRateFloat(), rate.getUpdatedAt());
        }
        System.out.printf("    EUR rateFloat=%s, rate=%s, symbol=%s, description=%s, updatedAt=%s%n",
                eur.getRateFloat(), eur.getRate(), eur.getSymbol(), eur.getDescription(), eur.getUpdatedAt());
    }
}