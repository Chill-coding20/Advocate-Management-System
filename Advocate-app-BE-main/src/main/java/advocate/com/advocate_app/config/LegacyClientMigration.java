package advocate.com.advocate_app.config;

import advocate.com.advocate_app.entity.Advocate;
import advocate.com.advocate_app.entity.CaseEntity;
import advocate.com.advocate_app.entity.Client;
import advocate.com.advocate_app.repository.AdvocateRepository;
import advocate.com.advocate_app.repository.CaseRepository;
import advocate.com.advocate_app.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev")
public class LegacyClientMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LegacyClientMigration.class);

    private final ClientRepository clientRepository;
    private final AdvocateRepository advocateRepository;
    private final CaseRepository caseRepository;

    public LegacyClientMigration(ClientRepository clientRepository,
                                 AdvocateRepository advocateRepository,
                                 CaseRepository caseRepository) {
        this.clientRepository = clientRepository;
        this.advocateRepository = advocateRepository;
        this.caseRepository = caseRepository;
    }

    @Override
    public void run(String... args) {
        List<Client> orphans = clientRepository.findByAdvocateIsNull();
        if (orphans.isEmpty()) {
            log.info("No legacy clients to migrate");
            return;
        }

        log.info("Found {} legacy clients without advocate assignment", orphans.size());
        Advocate defaultAdvocate = advocateRepository.findById(1L).orElse(null);
        if (defaultAdvocate == null) {
            log.warn("No default advocate found (ID=1), skipping migration");
            return;
        }

        for (Client client : orphans) {
            Advocate resolved = null;
            for (CaseEntity c : caseRepository.findAll()) {
                if (c.getClient() != null
                        && c.getClient().getId().equals(client.getId())
                        && c.getAdvocate() != null) {
                    resolved = c.getAdvocate();
                    break;
                }
            }
            if (resolved == null) {
                resolved = defaultAdvocate;
            }
            client.setAdvocate(resolved);
            clientRepository.save(client);
            log.info("Client '{}' (ID: {}) -> Advocate '{}'",
                    client.getName(), client.getId(), resolved.getFullName());
        }
        log.info("Legacy client migration completed");
    }
}
