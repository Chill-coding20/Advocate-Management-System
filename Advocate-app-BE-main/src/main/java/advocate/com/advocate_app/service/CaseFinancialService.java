package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.CaseEntity;
import advocate.com.advocate_app.repository.CaseRepository;
import advocate.com.advocate_app.repository.ClientPaymentRepository;
import advocate.com.advocate_app.repository.ExpenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Centralized service responsible for ALL case-level financial calculations.
 *
 * <p>This service exists to eliminate duplication of financial calculation logic.
 * Previously, both {@link ClientPaymentService} and {@link ExpenseService} maintained
 * their own {@code updateCaseFinancials()} methods with duplicated and inconsistent logic.
 *
 * <p><b>Who should call this service:</b>
 * <ul>
 *   <li>{@link ClientPaymentService} — after creating a client payment</li>
 *   <li>{@link ExpenseService} — after creating, updating, or deleting a case-linked expense</li>
 * </ul>
 *
 * <p><b>Who must NEVER perform financial calculations themselves:</b>
 * <ul>
 *   <li>{@link ClientPaymentService}</li>
 *   <li>{@link ExpenseService}</li>
 *   <li>{@link InvoiceService}</li>
 *   <li>Any scheduler, listener, or controller</li>
 * </ul>
 *
 * <p>All derived financial fields ({@code totalPaidByClient}, {@code totalExpensesSoFar},
 * {@code pendingFromClient}, {@code balanceInAccount}) are computed exclusively here
 * by querying the actual payment and expense records from the database.
 */
@Service
public class CaseFinancialService {

    private static final Logger log = LoggerFactory.getLogger(CaseFinancialService.class);

    private final CaseRepository caseRepository;
    private final ClientPaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;

    public CaseFinancialService(CaseRepository caseRepository,
                                 ClientPaymentRepository paymentRepository,
                                 ExpenseRepository expenseRepository) {
        this.caseRepository = caseRepository;
        this.paymentRepository = paymentRepository;
        this.expenseRepository = expenseRepository;
    }

    /**
     * Recalculates all derived financial fields for the given case.
     *
     * <p>This method queries the database for the actual payment and expense records,
     * computes the totals, and updates the CaseEntity. It is the SINGLE source of truth
     * for case financial calculations.
     *
     * <p>Fields recalculated:
     * <ul>
     *   <li>{@code totalPaidByClient} — sum of all payments for this case</li>
     *   <li>{@code totalExpensesSoFar} — sum of all case-linked expenses</li>
     *   <li>{@code pendingFromClient} = {@code totalClientAgreedAmount} - {@code totalPaidByClient}</li>
     *   <li>{@code balanceInAccount} = {@code totalPaidByClient} - {@code totalExpensesSoFar}</li>
     * </ul>
     *
     * <p>Fields preserved as-is (not modified):
     * <ul>
     *   <li>{@code amount}</li>
     *   <li>{@code estimatedAmount}</li>
     *   <li>{@code totalClientAgreedAmount}</li>
     * </ul>
     *
     * @param caseId the ID of the case to recalculate
     */
    @Transactional
    public void recalculateCaseFinancials(Long caseId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found: " + caseId));

        double totalPaid = paymentRepository.findByCaseEntity(caseEntity).stream()
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                .sum();

        double totalExpenses = expenseRepository.findByCaseEntity(caseEntity).stream()
                .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                .sum();

        double agreed = Optional.ofNullable(caseEntity.getTotalClientAgreedAmount()).orElse(0.0);

        caseEntity.setTotalPaidByClient(totalPaid);
        caseEntity.setTotalExpensesSoFar(totalExpenses);
        caseEntity.setBalanceInAccount(totalPaid - totalExpenses);
        caseEntity.setPendingFromClient(agreed - totalPaid);

        caseRepository.save(caseEntity);

        log.info("Recalculated financials for Case {}: totalPaid={}, totalExpenses={}, balance={}, pending={}",
                caseId, totalPaid, totalExpenses, caseEntity.getBalanceInAccount(), caseEntity.getPendingFromClient());
    }
}
