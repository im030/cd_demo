CREATE TABLE exchange_rate
(
    code        VARCHAR(5) PRIMARY KEY,
    description VARCHAR(100),
    symbol      VARCHAR(20),
    rate        VARCHAR(50),
    rate_float  DECIMAL(20, 8),
    updated_at  TIMESTAMP
);

CREATE TABLE currency
(
    code         VARCHAR(3) PRIMARY KEY,
    chinese_name VARCHAR(20)
);

INSERT INTO currency
VALUES ('USD', '美元'),
       ('GBP', '英鎊'),
       ('EUR', '歐元');