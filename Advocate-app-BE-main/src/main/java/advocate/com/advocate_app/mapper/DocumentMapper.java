package advocate.com.advocate_app.mapper;

import advocate.com.advocate_app.dto.DocumentResponseDTO;
import advocate.com.advocate_app.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentResponseDTO toResponseDTO(Document doc) {
        if (doc == null) return null;
        DocumentResponseDTO dto = new DocumentResponseDTO();
        dto.setId(doc.getId());
        dto.setDocumentName(doc.getDocumentName());
        dto.setOriginalName(doc.getOriginalName());
        dto.setStoredName(doc.getStoredName());
        dto.setFilePath(doc.getFilePath());
        dto.setFileSize(doc.getFileSize());
        dto.setFileType(doc.getFileType());
        dto.setCategory(doc.getCategory());
        dto.setDescription(doc.getDescription());
        dto.setVersion(doc.getVersion());
        dto.setDownloadCount(doc.getDownloadCount());
        dto.setStatus(doc.getStatus());
        dto.setUploadDate(doc.getUploadDate());
        dto.setUpdatedAt(doc.getUpdatedAt());

        if (doc.getCaseEntity() != null) {
            dto.setCaseEntity(new DocumentResponseDTO.CaseEntityInfo(
                    doc.getCaseEntity().getId(),
                    doc.getCaseEntity().getCaseNumber(),
                    doc.getCaseEntity().getCaseTitle()
            ));
        }
        if (doc.getClient() != null) {
            dto.setClient(new DocumentResponseDTO.ClientInfo(
                    doc.getClient().getId(),
                    doc.getClient().getName()
            ));
        }
        return dto;
    }
}
