package multitier.trans.interfaces.finance;

import jakarta.validation.Valid;
import multitier.trans.application.finance.dto.*;
import multitier.trans.application.finance.service.FinanceService;
import multitier.trans.domain.finance.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final FinanceService financeService;

    @Autowired
    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    // UC1: Generate Client Invoice
    @PostMapping("/invoices")
    public ResponseEntity<Invoice> generateInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        Invoice invoice = financeService.generateClientInvoice(request);
        return ResponseEntity.status(201).body(invoice);
    }

    // UC2: Process Payment
    @PostMapping("/payments")
    public ResponseEntity<Payment> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        Payment payment = financeService.processPayment(request);
        return ResponseEntity.status(201).body(payment);
    }

    // UC3: Track Operational Costs
    @PostMapping("/costs")
    public ResponseEntity<OperationalCost> recordCost(@Valid @RequestBody RecordOperationalCostRequest request) {
        OperationalCost cost = financeService.recordOperationalCost(request);
        return ResponseEntity.status(201).body(cost);
    }

    // UC4: Review Financial Reports (simplified: lists invoices and payments for a period)
    @GetMapping("/reports")
    public ResponseEntity<FinancialReportResponse> getReport(@Valid FinancialReportQuery query) {
        List<Invoice> invoices = financeService.getInvoicesForPeriod(query);
        List<Payment> payments = financeService.getPaymentsForPeriod(query);
        FinancialReportResponse response = new FinancialReportResponse(invoices, payments);
        return ResponseEntity.ok(response);
    }

    // UC5: Manage Tax Rates
    @PutMapping("/tax-rates")
    public ResponseEntity<TaxRate> upsertTaxRate(@Valid @RequestBody UpsertTaxRateRequest request) {
        TaxRate rate = financeService.upsertTaxRate(request);
        return ResponseEntity.ok(rate);
    }

    @GetMapping("/tax-rates")
    public List<TaxRate> listTaxRates() {
        return financeService.listTaxRates();
    }

    public static class FinancialReportResponse {
        private List<Invoice> invoices;
        private List<Payment> payments;

        public FinancialReportResponse(List<Invoice> invoices, List<Payment> payments) {
            this.invoices = invoices;
            this.payments = payments;
        }

        public List<Invoice> getInvoices() { return invoices; }
        public void setInvoices(List<Invoice> invoices) { this.invoices = invoices; }
        public List<Payment> getPayments() { return payments; }
        public void setPayments(List<Payment> payments) { this.payments = payments; }
    }
}

