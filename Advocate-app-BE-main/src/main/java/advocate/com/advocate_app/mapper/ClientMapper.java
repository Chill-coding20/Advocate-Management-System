package advocate.com.advocate_app.mapper;

import advocate.com.advocate_app.dto.ClientRequestDTO;
import advocate.com.advocate_app.dto.ClientResponseDTO;
import advocate.com.advocate_app.entity.Client;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public ClientResponseDTO toResponseDTO(Client client) {
        if (client == null) return null;
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setEmail(client.getEmail());
        dto.setPhone(client.getPhone());
        dto.setAddress(client.getAddress());
        dto.setDeleted(client.isDeleted());
        return dto;
    }

    public Client toEntity(ClientRequestDTO dto) {
        if (dto == null) return null;
        Client client = new Client();
        client.setName(dto.getName());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        client.setAddress(dto.getAddress());
        client.setDeleted(false);
        return client;
    }

    public void updateEntityFromRequestDTO(ClientRequestDTO dto, Client client) {
        if (dto == null || client == null) return;
        client.setName(dto.getName());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        client.setAddress(dto.getAddress());
    }
}
