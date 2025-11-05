package multitier.trans.application.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class UpsertTaxRateRequest {
    @NotBlank
    private String name;
    @NotNull
    private BigDecimal rate;
    private Boolean active;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

