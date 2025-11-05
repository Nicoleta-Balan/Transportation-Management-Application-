package multitier.trans.application.finance.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class FinancialReportQuery {
    @NotNull
    private LocalDateTime from;
    @NotNull
    private LocalDateTime to;

    public LocalDateTime getFrom() { return from; }
    public void setFrom(LocalDateTime from) { this.from = from; }
    public LocalDateTime getTo() { return to; }
    public void setTo(LocalDateTime to) { this.to = to; }
}

