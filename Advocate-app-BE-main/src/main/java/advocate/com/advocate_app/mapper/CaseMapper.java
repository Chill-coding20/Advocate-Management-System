package advocate.com.advocate_app.mapper;

import advocate.com.advocate_app.dto.CaseRequestDTO;
import advocate.com.advocate_app.dto.CaseResponseDTO;
import advocate.com.advocate_app.entity.CaseEntity;
import org.springframework.stereotype.Component;

@Component
public class CaseMapper {

    public CaseResponseDTO toResponseDTO(CaseEntity caseEntity) {
        if (caseEntity == null) return null;
        CaseResponseDTO dto = new CaseResponseDTO();
        dto.setId(caseEntity.getId());
        dto.setCaseNumber(caseEntity.getCaseNumber());
        dto.setCaseTitle(caseEntity.getCaseTitle());
        dto.setCaseType(caseEntity.getCaseType());
        dto.setCourtLevel(caseEntity.getCourtLevel());
        dto.setStatus(caseEntity.getStatus());
        dto.setAmount(caseEntity.getAmount());
        dto.setDescription(caseEntity.getDescription());
        dto.setTotalClientAgreedAmount(caseEntity.getTotalClientAgreedAmount());
        dto.setTotalPaidByClient(caseEntity.getTotalPaidByClient());
        dto.setTotalExpensesSoFar(caseEntity.getTotalExpensesSoFar());
        dto.setBalanceInAccount(caseEntity.getBalanceInAccount());
        dto.setPendingFromClient(caseEntity.getPendingFromClient());
        dto.setDeleted(caseEntity.isDeleted());
        if (caseEntity.getClient() != null) {
            dto.setClientId(caseEntity.getClient().getId());
            dto.setClientName(caseEntity.getClient().getName());
        }
        return dto;
    }

    public CaseEntity toEntity(CaseRequestDTO dto) {
        if (dto == null) return null;
        CaseEntity caseEntity = new CaseEntity();
        caseEntity.setCaseNumber(dto.getCaseNumber());
        caseEntity.setCaseTitle(dto.getCaseTitle());
        caseEntity.setCaseType(dto.getCaseType());
        caseEntity.setCourtLevel(dto.getCourtLevel());
        caseEntity.setStatus(dto.getStatus());
        caseEntity.setAmount(dto.getAmount());
        caseEntity.setDescription(dto.getDescription());
        caseEntity.setDeleted(false);
        return caseEntity;
    }

    public void updateEntityFromRequestDTO(CaseRequestDTO dto, CaseEntity caseEntity) {
        if (dto == null || caseEntity == null) return;
        caseEntity.setCaseNumber(dto.getCaseNumber());
        caseEntity.setCaseTitle(dto.getCaseTitle());
        caseEntity.setCaseType(dto.getCaseType());
        caseEntity.setCourtLevel(dto.getCourtLevel());
        caseEntity.setStatus(dto.getStatus());
        caseEntity.setAmount(dto.getAmount());
        caseEntity.setDescription(dto.getDescription());
    }
}
