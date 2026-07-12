package advocate.com.advocate_app.mapper;

import advocate.com.advocate_app.dto.ClientPaymentRequestDTO;
import advocate.com.advocate_app.dto.ClientPaymentResponseDTO;
import advocate.com.advocate_app.entity.ClientPayment;
import org.springframework.stereotype.Component;

@Component
public class ClientPaymentMapper {

    public ClientPaymentResponseDTO toResponseDTO(ClientPayment payment) {
        if (payment == null) return null;
        ClientPaymentResponseDTO dto = new ClientPaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMode(payment.getPaymentMode());
        dto.setReferenceNumber(payment.getReferenceNumber());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setDescription(payment.getDescription());
        if (payment.getCaseEntity() != null) {
            dto.setCaseId(payment.getCaseEntity().getId());
            dto.setCaseTitle(payment.getCaseEntity().getCaseTitle());
        }
        if (payment.getClient() != null) {
            dto.setClientId(payment.getClient().getId());
            dto.setClientName(payment.getClient().getName());
        }
        return dto;
    }

    public ClientPayment toEntity(ClientPaymentRequestDTO dto) {
        if (dto == null) return null;
        ClientPayment payment = new ClientPayment();
        payment.setAmount(dto.getAmount());
        payment.setPaymentMode(dto.getPaymentMode());
        payment.setReferenceNumber(dto.getReferenceNumber());
        payment.setPaymentDate(dto.getPaymentDate());
        payment.setDescription(dto.getDescription());
        return payment;
    }

    public void updateEntityFromRequestDTO(ClientPaymentRequestDTO dto, ClientPayment payment) {
        if (dto == null || payment == null) return;
        payment.setAmount(dto.getAmount());
        payment.setPaymentMode(dto.getPaymentMode());
        payment.setReferenceNumber(dto.getReferenceNumber());
        payment.setPaymentDate(dto.getPaymentDate());
        payment.setDescription(dto.getDescription());
    }
}
