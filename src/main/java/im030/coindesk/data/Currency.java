package im030.coindesk.data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Currency {

    @Id
    private String code;
    private String chineseName;

    public String getCode() {
        return code;
    }

    public String getChineseName() {
        return chineseName;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setChineseName(String chineseName) {
        this.chineseName = chineseName;
    }
}
