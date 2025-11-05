package multitier.trans.application.finance.service;

import multitier.trans.application.finance.dto.*;
import multitier.trans.domain.finance.model.*;

import java.util.List;

public interface FinanceService {
    // UC1: Generate Client Invoice
    Invoice generateClientInvoice(CreateInvoiceRequest request);

    // UC2: Process Payment
    Payment processPayment(ProcessPaymentRequest request);

    // UC3: Track Operational Costs
    OperationalCost recordOperationalCost(RecordOperationalCostRequest request);

    // UC4: Review Financial Reports (simplified: returns invoices and payments in period)
    List<Invoice> getInvoicesForPeriod(FinancialReportQuery query);
    List<Payment> getPaymentsForPeriod(FinancialReportQuery query);

    // UC5: Manage Tax Rates
    TaxRate upsertTaxRate(UpsertTaxRateRequest request);
    List<TaxRate> listTaxRates();
}

