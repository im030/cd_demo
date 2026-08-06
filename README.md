## API spec

### 1. 抓取並寫入 CoinDesk 資料
```
GET /refresh
```
- 成功: `200 OK`，body 為字串 `OK`

### 2. 讀取全部匯率（含中文名） Read ALL
```
GET /get
```
- 成功: `200 OK`

### 3. 讀取單一幣別 Read Single
```
GET /get/{code}
```
- 成功: `200 OK`，`ExchangeRateDto`
- 查無該幣別: `404 NOT_FOUND`

### 4. 建立幣別中文對照 Create
```
POST /create
Content-Type: application/json
```
Body:
```json
{ "code": "EUR", "chineseName": "歐元" }
```
- 成功: `200 OK`，body `OK`
- 已存在: `404 NOT_FOUND`（訊息 "Currency exists"）

### 5. 更新幣別中文對照 Update
```
POST /update
Content-Type: application/json
```
Body:
```json
{ "code": "EUR", "chineseName": "歐元二" }
```
- 成功: `200 OK`
- 不存在: `404 NOT_FOUND`

### 6. 刪除幣別中文對照 Delete
```
GET /del/{code}
```
- 成功: `200 OK`
- 不存在: `404 NOT_FOUND`

---
- SQL schema @ `src/main/resources/schema.sql`
- tests @ at `src/test/java/im030/coindesk`