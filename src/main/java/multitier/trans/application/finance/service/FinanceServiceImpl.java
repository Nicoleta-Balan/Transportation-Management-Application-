package multitier.trans.application.finance.service;

import multitier.trans.application.finance.dto.*;
import multitier.trans.domain.finance.model.*;
import multitier.trans.domain.finance.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FinanceServiceImpl implements FinanceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final OperationalCostRepository operationalCostRepository;
    private final TaxRateRepository taxRateRepository;

    @Autowired
    public FinanceServiceImpl(InvoiceRepository invoiceRepository,
                              PaymentRepository paymentRepository,
                              OperationalCostRepository operationalCostRepository,
                              TaxRateRepository taxRateRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.operationalCostRepository = operationalCostRepository;
        this.taxRateRepository = taxRateRepository;
    }

    @Override
    public Invoice generateClientInvoice(CreateInvoiceRequest request) {
        Invoice invoice = new Invoice();
        invoice.setClientName(request.getClientName());
        invoice.setAmount(request.getAmount());
        invoice.setReservationId(request.getReservationId());
        invoice.setIssueDate(LocalDateTime.now());
        invoice.setStatus("ISSUED");
        return invoiceRepository.save(invoice);
    }

    @Override
    public Payment processPayment(ProcessPaymentRequest request) {
        Payment payment = new Payment();
        payment.setInvoiceId(request.getInvoiceId());
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setPaidAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        // naive status update for invoice
        invoiceRepository.findById(request.getInvoiceId()).ifPresent(inv -> {
            inv.setStatus("PAID");
            invoiceRepository.save(inv);
        });

        return saved;
    }

    @Override
    public OperationalCost recordOperationalCost(RecordOperationalCostRequest request) {
        OperationalCost cost = new OperationalCost();
        cost.setCategory(request.getCategory());
        cost.setAmount(request.getAmount());
        cost.setOccurredAt(request.getOccurredAt());
        cost.setNotes(request.getNotes());
        return operationalCostRepository.save(cost);
    }

    @Override
    public List<Invoice> getInvoicesForPeriod(FinancialReportQuery query) {
        // simplified: fetch all then filter in-memory; ideally add derived queries by date
        return invoiceRepository.findAll().stream()
                .filter(i -> !i.getIssueDate().isBefore(query.getFrom()) && !i.getIssueDate().isAfter(query.getTo()))
                .toList();
    }

    @Override
    public List<Payment> getPaymentsForPeriod(FinancialReportQuery query) {
        return paymentRepository.findAll().stream()
                .filter(p -> !p.getPaidAt().isBefore(query.getFrom()) && !p.getPaidAt().isAfter(query.getTo()))
                .toList();
    }

    @Override
    public TaxRate upsertTaxRate(UpsertTaxRateRequest request) {
        TaxRate rate = taxRateRepository.findByName(request.getName()).orElseGet(TaxRate::new);
        rate.setName(request.getName());
        rate.setRate(request.getRate());
        if (request.getActive() != null) {
            rate.setActive(request.getActive());
        }
        return taxRateRepository.save(rate);
    }

    @Override
    public List<TaxRate> listTaxRates() {
        return taxRateRepository.findAll();
    }
}

