package im030.coindesk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import im030.coindesk.data.CoinDeskResponse;
import im030.coindesk.service.ApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.MethodName.class)
class CoinDeskApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ApiService apiService;

    // for testing without actually poking the real API
//    @MockBean
//    RestTemplate restTemplate;
//
//    @BeforeEach
//    void stubCoinDeskApi() throws Exception {
//        CoinDeskResponse resp = objectMapper.readValue(readResource("coindesk.json"), CoinDeskResponse.class);
//        when(restTemplate.getForObject(anyString(), eq(CoinDeskResponse.class)))
//                .thenReturn(resp);
//    }

//    private String readResource(String name) throws Exception {
//        try (InputStream in = getClass().getClassLoader().getResourceAsStream(name)) {
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            byte[] buf = new byte[1024];
//            int n;
//            while ((n = in.read(buf)) != -1) {
//                out.write(buf, 0, n);
//            }
//            return new String(out.toByteArray(), StandardCharsets.UTF_8);
//        }
//    }

    private String pretty(MvcResult result) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper.readTree(result.getResponse().getContentAsString()));
    }

    @Test
    void test1_crud() throws Exception {
        System.out.println("=== 1. 幣別對應表(Currency) CRUD API ===");
        System.out.println("  測試內容: 依序驗證 refresh / get / update / delete / create 等增刪改查流程");

        System.out.println("--- Fetch & Read ALL ---");
        MvcResult refresh = mockMvc.perform(get("/refresh"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"))
                .andReturn();
        System.out.println("  refresh => " + refresh.getResponse().getContentAsString() + " (預期 OK)");

        MvcResult all = mockMvc.perform(get("/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bpi.USD").exists())
                .andExpect(jsonPath("$.bpi.EUR").exists())
                .andExpect(jsonPath("$.bpi.GBP").exists())
                .andExpect(jsonPath("$.bpi.USD.chinese_name").value("美元"))
                .andExpect(jsonPath("$.bpi.EUR.chinese_name").value("歐元"))
                .andExpect(jsonPath("$.bpi.GBP.chinese_name").value("英鎊"))
                .andReturn();
        System.out.println("  /get =>");
        System.out.println(pretty(all));

        System.out.println("--- Update EUR Chinese name to 歐元二 ---");
        String updateBody = "{\"code\":\"EUR\",\"chineseName\":\"歐元二\"}";
        mockMvc.perform(post("/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"))
                .andReturn();
        System.out.println("  update結果 => OK (預期 200 OK)");

        System.out.println("--- Verify Update ---");
        MvcResult updated = mockMvc.perform(get("/get/EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("EUR"))
                .andExpect(jsonPath("$.chinese_name").value("歐元二"))
                .andReturn();
        System.out.println(pretty(updated));

        System.out.println("--- Delete EUR ---");
        MvcResult del = mockMvc.perform(get("/del/EUR"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"))
                .andReturn();
        System.out.println("  del結果 => " + del.getResponse().getContentAsString() + " (預期 OK)");

        System.out.println("--- Update EUR Chinese name to 歐元二 when it doesn't exist ---");
        mockMvc.perform(post("/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isNotFound())
                .andReturn();
        System.out.println("  update不存在結果 => 404 (預期 Not Found)");

        System.out.println("--- Verify EUR Deleted ---");
        MvcResult deleted = mockMvc.perform(get("/get/EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("EUR"))
                .andExpect(jsonPath("$.chinese_name").doesNotExist())
                .andReturn();
        System.out.println(pretty(deleted));

        System.out.println("--- Create EUR name entry ---");
        String createBody = "{\"code\":\"EUR\",\"chineseName\":\"歐元\"}";
        mockMvc.perform(post("/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
        System.out.println("  create結果 => OK (預期 200 OK)");

        MvcResult created = mockMvc.perform(get("/get/EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chinese_name").value("歐元"))
                .andReturn();
        System.out.println("  verify create => " + created.getResponse().getContentAsString());

        System.out.println("--- Repeatedly Create EUR name entry ---");
        mockMvc.perform(post("/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isBadRequest());
        System.out.println("  重複 create結果 => 400 (預期 Bad Request)");

        System.out.println("--- Delete non-existing => 404 ---");
        mockMvc.perform(get("/del/ZZZ"))
                .andExpect(status().isNotFound());
        System.out.println("  del不存在 => 404 (預期 Not Found)");
    }

    @Test
    void test2_convertedDataFromApi() throws Exception {
        System.out.println("=== 2. 資料轉換 API /get/{code} ===");
        System.out.println("  測試內容: 驗證 /get/{code} 回傳的轉換欄位(code/symbol/description/rate/rate_float/updated_at)");

        MvcResult eur = mockMvc.perform(get("/get/EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("EUR"))
                .andExpect(jsonPath("$.symbol").value("&euro;"))
                .andExpect(jsonPath("$.description").value("Euro"))
                .andExpect(jsonPath("$.rate").value("52,243.287"))
                .andExpect(jsonPath("$.rate_float").value(52243.2865))
                .andExpect(jsonPath("$.updated_at").exists())
                .andReturn();
        System.out.println("  EUR =>");
        System.out.println(pretty(eur));

        MvcResult usd = mockMvc.perform(get("/get/USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("USD"))
                .andExpect(jsonPath("$.rate").value("57,756.298"))
                .andExpect(jsonPath("$.rate_float").value(57756.2984))
                .andReturn();
        System.out.println("  USD =>");
        System.out.println(pretty(usd));

        mockMvc.perform(get("/get/JPY"))
                .andExpect(status().isNotFound());
        System.out.println("  JPY => 404 (不在 coindesk bpi 中, 預期 Not Found)");
    }
}