package im030.coindesk.repository;

import im030.coindesk.data.ExchangeRate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeRateRepository extends CrudRepository<ExchangeRate, String> {
    ExchangeRate findByCode(String code);
}
