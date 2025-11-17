package multitier.trans.dto;

import java.math.BigDecimal;

/**
 * DTO for fare calculation response.
 * Used to display fare breakdown in the UI before saving a reservation.
 */
public class FareCalculationResponse {
    private BigDecimal baseFare;
    private BigDecimal vatAmount;
    private BigDecimal totalFare;
    private BigDecimal vatRate;

    public FareCalculationResponse() {
    }

    public FareCalculationResponse(BigDecimal baseFare, BigDecimal vatAmount, BigDecimal totalFare, BigDecimal vatRate) {
        this.baseFare = baseFare;
        this.vatAmount = vatAmount;
        this.totalFare = totalFare;
        this.vatRate = vatRate;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(BigDecimal totalFare) {
        this.totalFare = totalFare;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public void setVatRate(BigDecimal vatRate) {
        this.vatRate = vatRate;
    }
}

