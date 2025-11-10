package multitier.trans.repository;

import multitier.trans.model.RevenueSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RevenueSummaryRepository extends JpaRepository<RevenueSummary, Long> {

    List<RevenueSummary> findBySummaryDate(LocalDate summaryDate);

    List<RevenueSummary> findBySummaryDateBetween(LocalDate startDate, LocalDate endDate);
}

