package advocate.com.advocate_app.controller;

import advocate.com.advocate_app.dto.ClientRequestDTO;
import advocate.com.advocate_app.dto.ClientResponseDTO;
import advocate.com.advocate_app.entity.Client;
import advocate.com.advocate_app.exception.ResourceNotFoundException;
import advocate.com.advocate_app.mapper.ClientMapper;
import advocate.com.advocate_app.security.JwtUtil;
import advocate.com.advocate_app.security.RequirePermission;
import advocate.com.advocate_app.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    public ClientController(ClientService clientService, ClientMapper clientMapper) {
        this.clientService = clientService;
        this.clientMapper = clientMapper;
    }

    @GetMapping
    @RequirePermission("CLIENT_VIEW")
    public ResponseEntity<Map<String, Object>> getClientsPaged(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "false") boolean archived) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Client> clientPage;
        if (archived) {
            clientPage = keyword != null && !keyword.isBlank()
                    ? clientService.searchArchivedClientsPaged(email, keyword, pageable)
                    : clientService.getArchivedClientsPaged(email, pageable);
        } else {
            clientPage = keyword != null && !keyword.isBlank()
                    ? clientService.searchClientsPaged(email, keyword, pageable)
                    : clientService.getClientsPaged(email, pageable);
        }
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("content", clientPage.getContent().stream().map(clientMapper::toResponseDTO).collect(Collectors.toList()));
        response.put("page", clientPage.getNumber());
        response.put("size", clientPage.getSize());
        response.put("totalElements", clientPage.getTotalElements());
        response.put("totalPages", clientPage.getTotalPages());
        response.put("hasNext", clientPage.hasNext());
        response.put("hasPrevious", clientPage.hasPrevious());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-clients")
    @RequirePermission("CLIENT_VIEW")
    public ResponseEntity<List<ClientResponseDTO>> getMyClients(@RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<ClientResponseDTO> clients = clientService.getAllClients(email).stream()
                .map(clientMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/archived")
    @RequirePermission("CLIENT_VIEW")
    public ResponseEntity<List<ClientResponseDTO>> getArchivedClients(@RequestHeader("Authorization") String token) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<ClientResponseDTO> clients = clientService.getArchivedClients(email).stream()
                .map(clientMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    @RequirePermission("CLIENT_VIEW")
    public ResponseEntity<ClientResponseDTO> getClientById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Client client = clientService.getClientById(email, id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        return ResponseEntity.ok(clientMapper.toResponseDTO(client));
    }

    @PostMapping("/create")
    @RequirePermission("CLIENT_CREATE")
    public ResponseEntity<ClientResponseDTO> createClient(@RequestHeader("Authorization") String token, 
                                                          @Valid @RequestBody ClientRequestDTO clientDto) {
        String email = JwtUtil.extractEmail(token.substring(7));
        Client client = clientMapper.toEntity(clientDto);
        Client saved = clientService.addClient(email, client);
        return ResponseEntity.ok(clientMapper.toResponseDTO(saved));
    }

    @PutMapping("/update/{id}")
    @RequirePermission("CLIENT_EDIT")
    public ResponseEntity<ClientResponseDTO> updateClient(@RequestHeader("Authorization") String token,
                                                          @PathVariable Long id,
                                                          @Valid @RequestBody ClientRequestDTO clientDto) {
        String email = JwtUtil.extractEmail(token.substring(7));
        // Fetch existing entity
        Client existing = clientService.getClientById(email, id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        clientMapper.updateEntityFromRequestDTO(clientDto, existing);
        Client updated = clientService.updateClient(email, id, existing);
        return ResponseEntity.ok(clientMapper.toResponseDTO(updated));
    }

    @DeleteMapping("/delete/{id}")
    @RequirePermission("CLIENT_DELETE")
    public ResponseEntity<String> deleteClient(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String email = JwtUtil.extractEmail(token.substring(7));
        clientService.deleteClient(email, id);
        return ResponseEntity.ok("Client archived successfully (soft deleted).");
    }

    @PutMapping("/restore/{id}")
    @RequirePermission("CLIENT_EDIT")
    public ResponseEntity<String> restoreClient(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String email = JwtUtil.extractEmail(token.substring(7));
        clientService.restoreClient(email, id);
        return ResponseEntity.ok("Client restored successfully.");
    }

    @GetMapping("/search")
    @RequirePermission("CLIENT_VIEW")
    public ResponseEntity<List<ClientResponseDTO>> searchClients(@RequestHeader("Authorization") String token, 
                                                                 @RequestParam("keyword") String keyword) {
        String email = JwtUtil.extractEmail(token.substring(7));
        List<ClientResponseDTO> result = clientService.searchClients(email, keyword).stream()
                .map(clientMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
