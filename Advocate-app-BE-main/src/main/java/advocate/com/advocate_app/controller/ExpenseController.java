package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.ExpenseRequestDTO;
import advocate.com.advocate_app.dto.ExpenseResponseDTO;
import advocate.com.advocate_app.entity.CaseEntity;
import advocate.com.advocate_app.entity.Expense;
import advocate.com.advocate_app.exception.ResourceNotFoundException;
import advocate.com.advocate_app.mapper.ExpenseMapper;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.security.RequirePermission;
import advocate.com.advocate_app.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private static final Logger log = LoggerFactory.getLogger(ExpenseController.class);

    private final ExpenseService expenseService;
    private final ExpenseMapper expenseMapper;

    @Autowired
    public ExpenseController(ExpenseService expenseService, ExpenseMapper expenseMapper) {
        this.expenseService = expenseService;
        this.expenseMapper = expenseMapper;
    }

    @PostMapping("/create")
    @RequirePermission("EXPENSE_CREATE")
    public ResponseEntity<ExpenseResponseDTO> createExpense(@RequestHeader("Authorization") String token,
                                                            @Valid @RequestBody ExpenseRequestDTO expenseDto) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Expense expense = expenseMapper.toEntity(expenseDto);
        if (expenseDto.getCaseId() != null) {
            CaseEntity caseEntity = new CaseEntity();
            caseEntity.setId(expenseDto.getCaseId());
            expense.setCaseEntity(caseEntity);
        }
        Expense saved = expenseService.createExpense(email, expense);
        return ResponseEntity.ok(expenseMapper.toResponseDTO(saved));
    }

    @PutMapping("/update/{id}")
    @RequirePermission("EXPENSE_EDIT")
    public ResponseEntity<ExpenseResponseDTO> updateExpense(@RequestHeader("Authorization") String token,
                                                            @PathVariable Long id,
                                                            @Valid @RequestBody ExpenseRequestDTO expenseDto) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Expense expense = expenseMapper.toEntity(expenseDto);
        if (expenseDto.getCaseId() != null) {
            CaseEntity caseEntity = new CaseEntity();
            caseEntity.setId(expenseDto.getCaseId());
            expense.setCaseEntity(caseEntity);
        }
        Expense updated = expenseService.updateExpense(email, id, expense);
        return ResponseEntity.ok(expenseMapper.toResponseDTO(updated));
    }

    @GetMapping("/my-expenses")
    @RequirePermission("EXPENSE_VIEW")
    public ResponseEntity<List<ExpenseResponseDTO>> getMyExpenses(@RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<ExpenseResponseDTO> expenses = expenseService.getMyExpenses(email).stream()
                .map(expenseMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/case/{caseId}")
    @RequirePermission("EXPENSE_VIEW")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesByCase(@RequestHeader("Authorization") String token,
                                                                       @PathVariable Long caseId) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<ExpenseResponseDTO> expenses = expenseService.getExpensesByCase(email, caseId).stream()
                .map(expenseMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/search")
    @RequirePermission("EXPENSE_VIEW")
    public ResponseEntity<List<ExpenseResponseDTO>> searchExpenses(@RequestHeader("Authorization") String token,
                                                                    @RequestParam(required = false) String keyword) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<ExpenseResponseDTO> expenses = expenseService.searchExpenses(email, keyword).stream()
                .map(expenseMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(expenses);
    }


    @DeleteMapping("/delete/{id}")
    @RequirePermission("EXPENSE_DELETE")
    public ResponseEntity<String> deleteExpense(@RequestHeader("Authorization") String token,
                                                 @PathVariable Long id) {
        String email = JwtUtil.extractEmail(token.substring(7));
        expenseService.deleteExpense(email, id);
        return ResponseEntity.ok("Expense deleted successfully");
    }

    @GetMapping
    @RequirePermission("EXPENSE_VIEW")
    public ResponseEntity<Map<String, Object>> getExpensesPaged(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Expense> expensePage = expenseService.getExpensesPaged(email, pageable);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("content", expensePage.getContent().stream().map(expenseMapper::toResponseDTO).collect(Collectors.toList()));
        response.put("page", expensePage.getNumber());
        response.put("size", expensePage.getSize());
        response.put("totalElements", expensePage.getTotalElements());
        response.put("totalPages", expensePage.getTotalPages());
        response.put("hasNext", expensePage.hasNext());
        response.put("hasPrevious", expensePage.hasPrevious());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    @RequirePermission("EXPENSE_VIEW")
    public ResponseEntity<Map<String, Object>> getTodayExpenses(@RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Map<String, Object> report = expenseService.getTodayExpenses(email);
        if (report.containsKey("expenses")) {
            List<?> expensesList = (List<?>) report.get("expenses");
            List<ExpenseResponseDTO> dtos = expensesList.stream()
                    .map(e -> expenseMapper.toResponseDTO((Expense) e))
                    .collect(Collectors.toList());
            Map<String, Object> cleanReport = new HashMap<>(report);
            cleanReport.put("expenses", dtos);
            return ResponseEntity.ok(cleanReport);
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/monthly")
    @RequirePermission("EXPENSE_VIEW")
    public ResponseEntity<Map<String, Object>> getMonthlyReport(@RequestHeader("Authorization") String token,
                                                                    @RequestParam int year,
                                                                    @RequestParam int month) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Map<String, Object> report = expenseService.getMonthlyReport(email, year, month);
        boolean hasExpenses = report.containsKey("expenses");
        int expSize = hasExpenses ? ((List<?>) report.get("expenses")).size() : -1;
        log.debug("Monthly report: hasExpenses={}, expensesSize={}, total={}", hasExpenses, expSize, report.get("totalExpenses"));
        if (hasExpenses) {
            List<?> expensesList = (List<?>) report.get("expenses");
            List<ExpenseResponseDTO> dtos = expensesList.stream()
                    .map(e -> expenseMapper.toResponseDTO((Expense) e))
                    .collect(Collectors.toList());
            log.debug("Monthly controller: converted DTOs count={}", dtos.size());
            Map<String, Object> cleanReport = new HashMap<>(report);
            cleanReport.put("expenses", dtos);
            return ResponseEntity.ok(cleanReport);
        }
        log.warn("Monthly report missing 'expenses' key");
        return ResponseEntity.ok(report);
    }
}
