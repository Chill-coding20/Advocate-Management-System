package advocate.com.advocate_app.mapper;

import advocate.com.advocate_app.dto.InvoiceRequestDTO;
import advocate.com.advocate_app.dto.InvoiceResponseDTO;
import advocate.com.advocate_app.entity.Invoice;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public InvoiceResponseDTO toResponseDTO(Invoice invoice) {
        if (invoice == null) return null;
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setAmount(invoice.getAmount());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setStatus(invoice.getStatus());
        if (invoice.getCaseEntity() != null) {
            dto.setCaseId(invoice.getCaseEntity().getId());
            dto.setCaseTitle(invoice.getCaseEntity().getCaseTitle());
        }
        if (invoice.getClient() != null) {
            dto.setClientId(invoice.getClient().getId());
            dto.setClientName(invoice.getClient().getName());
        }
        return dto;
    }

    public Invoice toEntity(InvoiceRequestDTO dto) {
        if (dto == null) return null;
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(dto.getInvoiceNumber());
        invoice.setAmount(dto.getAmount());
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setDueDate(dto.getDueDate());
        return invoice;
    }

    public void updateEntityFromRequestDTO(InvoiceRequestDTO dto, Invoice invoice) {
        if (dto == null || invoice == null) return;
        invoice.setInvoiceNumber(dto.getInvoiceNumber());
        invoice.setAmount(dto.getAmount());
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setDueDate(dto.getDueDate());
    }
}
