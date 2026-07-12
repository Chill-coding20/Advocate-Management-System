package advocate.com.advocate_app.service;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.Client;
import advocate.com.advocate_app.communication.service.CommunicationDispatcher;
import advocate.com.advocate_app.communication.enums.NotificationType;
import advocate.com.advocate_app.communication.dto.NotificationPayload;
import advocate.com.advocate_app.communication.service.EmailTemplateService;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;

    @Autowired
    private AdvocateRepository advocateRepository;

    @Autowired
    private CommunicationDispatcher notificationDispatcher;

    @Autowired
    private EmailTemplateService templateService;

    @Autowired
    private AuditLogService auditLogService;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    private Advocate getAdvocate(String email) {
        return advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found with email: " + email));
    }

    // ✅ Fetch active clients for advocate (paginated)
    public Page<Client> getClientsPaged(String email, Pageable pageable) {
        Advocate advocate = getAdvocate(email);
        return clientRepository.findByAdvocateAndDeletedFalse(advocate, pageable);
    }

    // ✅ Fetch archived clients for advocate (paginated)
    public Page<Client> getArchivedClientsPaged(String email, Pageable pageable) {
        Advocate advocate = getAdvocate(email);
        return clientRepository.findByAdvocateAndDeletedTrue(advocate, pageable);
    }

    // ✅ Search active clients by advocate (paginated)
    public Page<Client> searchClientsPaged(String email, String keyword, Pageable pageable) {
        Advocate advocate = getAdvocate(email);
        if (keyword == null || keyword.isBlank()) {
            return clientRepository.findByAdvocateAndDeletedFalse(advocate, pageable);
        }
        return clientRepository.searchActiveClientsByAdvocatePaged(advocate, keyword, pageable);
    }

    // ✅ Fetch active clients for advocate
    public List<Client> getAllClients(String email) {
        Advocate advocate = getAdvocate(email);
        return clientRepository.findAllActiveByAdvocate(advocate);
    }

    public Optional<Client> getClientById(String email, Long id) {
        Advocate advocate = getAdvocate(email);
        return clientRepository.findById(id)
                .filter(c -> !c.isDeleted() && c.getAdvocate() != null && c.getAdvocate().getId().equals(advocate.getId()));
    }

    public Client addClient(String email, Client client) {
        Advocate advocate = getAdvocate(email);
        client.setAdvocate(advocate);
        client.setDeleted(false);
        Client saved = clientRepository.save(client);

        log.info("CLIENT SAVED — id={}, email={}", saved.getId(), saved.getEmail());

        try {
            auditLogService.recordAction(
                    advocate.getId(), advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail(),
                    AuditLogService.CLIENT_CREATED, AuditLogService.MODULE_CLIENTS,
                    "Client Created", "Client " + saved.getName() + " registered",
                    "Client", saved.getId(), "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }

        // Send client registration notifications
        try {
            Map<String, String> templateParams = new HashMap<>();
            templateParams.put("1", saved.getName());
            templateParams.put("2", advocate.getFullName());

            log.info("CREATING PAYLOAD for client: {}", saved.getEmail());

            NotificationPayload payload = new NotificationPayload();
            payload.setType(NotificationType.CLIENT_REGISTERED);
            payload.setRecipientName(saved.getName());
            payload.setRecipientEmail(saved.getEmail());
            payload.setRecipientPhone(saved.getPhone());
            payload.setAdvocate(advocate);
            payload.setClient(saved);
            payload.setSubject("Welcome — Client Registration Confirmed");
            payload.setEmailBody(templateService.clientRegisteredEmail(saved.getName(), advocate.getFullName(), "/clients/" + saved.getId()));
            payload.setWhatsappMessage(templateService.clientRegisteredWhatsApp(saved.getName(), advocate.getFullName()));
            payload.setWhatsappTemplateName(EmailTemplateService.TEMPLATE_HELLO_WORLD);
            payload.setWhatsappTemplateParameters(templateParams);

            log.info("CALLING DISPATCHER for client: {}", saved.getEmail());
            notificationDispatcher.dispatchSafely(payload);
            log.info("DISPATCH FINISHED for client: {}", saved.getEmail());
        } catch (Exception e) {
            log.warn("Could not dispatch CLIENT_REGISTERED notification: {}", e.getMessage());
        }

        return saved;
    }

    public Client updateClient(String email, Long id, Client clientDetails) {
        Advocate advocate = getAdvocate(email);
        return clientRepository.findById(id)
                .map(client -> {
                    if (client.getAdvocate() == null || !client.getAdvocate().getId().equals(advocate.getId())) {
                        throw new RuntimeException("Unauthorized to update this client.");
                    }
                    client.setName(clientDetails.getName());
                    client.setEmail(clientDetails.getEmail());
                    client.setPhone(clientDetails.getPhone());
                    client.setAddress(clientDetails.getAddress());
                    Client saved = clientRepository.save(client);
                    try {
                        auditLogService.recordAction(
                                advocate.getId(), advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail(),
                                AuditLogService.CLIENT_UPDATED, AuditLogService.MODULE_CLIENTS,
                                "Client Updated", "Client " + saved.getName() + " details updated",
                                "Client", saved.getId(), "SUCCESS"
                        );
                    } catch (Exception e) {
                        log.warn("Could not record audit log: {}", e.getMessage());
                    }
                    return saved;
                })
                .orElseThrow(() -> new RuntimeException("Client not found with id " + id));
    }

    // ✅ Soft delete instead of physical delete
    public void deleteClient(String email, Long id) {
        Advocate advocate = getAdvocate(email);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        if (client.getAdvocate() == null || !client.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("Unauthorized to delete this client.");
        }
        client.setDeleted(true);
        clientRepository.save(client);
        try {
            auditLogService.recordAction(
                    advocate.getId(), advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail(),
                    AuditLogService.CLIENT_DELETED, AuditLogService.MODULE_CLIENTS,
                    "Client Archived", "Client " + client.getName() + " archived (soft deleted)",
                    "Client", id, "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }
    }

    // ✅ Restore a previously deleted client
    public void restoreClient(String email, Long id) {
        Advocate advocate = getAdvocate(email);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        if (client.getAdvocate() == null || !client.getAdvocate().getId().equals(advocate.getId())) {
            throw new RuntimeException("Unauthorized to restore this client.");
        }
        client.setDeleted(false);
        clientRepository.save(client);
        try {
            auditLogService.recordAction(
                    advocate.getId(), advocate.getFullName() != null ? advocate.getFullName() : advocate.getEmail(),
                    AuditLogService.CLIENT_RESTORED, AuditLogService.MODULE_CLIENTS,
                    "Client Restored", "Client " + client.getName() + " restored from archive",
                    "Client", id, "SUCCESS"
            );
        } catch (Exception e) {
            log.warn("Could not record audit log: {}", e.getMessage());
        }
    }

    // ✅ Search archived clients by advocate (paginated)
    public Page<Client> searchArchivedClientsPaged(String email, String keyword, Pageable pageable) {
        Advocate advocate = getAdvocate(email);
        if (keyword == null || keyword.isBlank()) {
            return clientRepository.findByAdvocateAndDeletedTrue(advocate, pageable);
        }
        return clientRepository.searchArchivedClientsByAdvocatePaged(advocate, keyword, pageable);
    }

    // ✅ Search active clients for advocate
    public List<Client> searchClients(String email, String keyword) {
        Advocate advocate = getAdvocate(email);
        if (keyword == null || keyword.trim().isEmpty()) {
            return clientRepository.findAllActiveByAdvocate(advocate);
        }
        return clientRepository.searchActiveClientsByAdvocate(advocate, keyword.trim());
    }

    // ✅ Fetch archived (soft-deleted) clients for advocate
    public List<Client> getArchivedClients(String email) {
        Advocate advocate = getAdvocate(email);
        return clientRepository.findAllArchivedByAdvocate(advocate);
    }
}
