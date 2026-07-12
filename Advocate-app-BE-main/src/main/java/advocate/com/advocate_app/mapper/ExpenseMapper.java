package advocate.com.advocate_app.mapper;

import advocate.com.advocate_app.dto.ExpenseRequestDTO;
import advocate.com.advocate_app.dto.ExpenseResponseDTO;
import advocate.com.advocate_app.entity.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public ExpenseResponseDTO toResponseDTO(Expense expense) {
        if (expense == null) return null;
        ExpenseResponseDTO dto = new ExpenseResponseDTO();
        dto.setId(expense.getId());
        dto.setTitle(expense.getTitle());
        dto.setAmount(expense.getAmount());
        dto.setCategory(expense.getCategory());
        dto.setDescription(expense.getDescription());
        dto.setPaymentMode(expense.getPaymentMode());
        dto.setPaymentStatus(expense.getPaymentStatus());
        dto.setReferenceNumber(expense.getReferenceNumber());
        dto.setPaymentDate(expense.getPaymentDate());
        dto.setExpenseType(expense.getExpenseType());
        if (expense.getCaseEntity() != null) {
            dto.setCaseId(expense.getCaseEntity().getId());
            dto.setCaseTitle(expense.getCaseEntity().getCaseTitle());
        }
        return dto;
    }

    public Expense toEntity(ExpenseRequestDTO dto) {
        if (dto == null) return null;
        Expense expense = new Expense();
        expense.setTitle(dto.getTitle());
        expense.setAmount(dto.getAmount());
        expense.setCategory(dto.getCategory());
        expense.setDescription(dto.getDescription());
        expense.setPaymentMode(dto.getPaymentMode());
        expense.setPaymentStatus(dto.getPaymentStatus());
        expense.setReferenceNumber(dto.getReferenceNumber());
        expense.setPaymentDate(dto.getPaymentDate());
        if (dto.getExpenseType() != null) {
            expense.setExpenseType(dto.getExpenseType());
        }
        return expense;
    }

    public void updateEntityFromRequestDTO(ExpenseRequestDTO dto, Expense expense) {
        if (dto == null || expense == null) return;
        expense.setTitle(dto.getTitle());
        expense.setAmount(dto.getAmount());
        expense.setCategory(dto.getCategory());
        expense.setDescription(dto.getDescription());
        expense.setPaymentMode(dto.getPaymentMode());
        expense.setPaymentStatus(dto.getPaymentStatus());
        expense.setReferenceNumber(dto.getReferenceNumber());
        expense.setPaymentDate(dto.getPaymentDate());
        if (dto.getExpenseType() != null) {
            expense.setExpenseType(dto.getExpenseType());
        }
    }
}
