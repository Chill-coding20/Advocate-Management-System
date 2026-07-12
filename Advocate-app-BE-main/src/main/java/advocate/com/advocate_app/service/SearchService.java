package advocate.com.advocate_app.service;

import advocate.com.advocate_app.dto.*;
import advocate.com.advocate_app.entity.*;
import advocate.com.advocate_app.mapper.*;
import advocate.com.advocate_app.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);
    private static final int MAX_RESULTS = 5;

    private final AdvocateRepository advocateRepository;
    private final ClientRepository clientRepository;
    private final CaseRepository caseRepository;
    private final DocumentRepository documentRepository;
    private final InvoiceRepository invoiceRepository;
    private final ExpenseRepository expenseRepository;
    private final TaskRepository taskRepository;
    private final CaseEventRepository caseEventRepository;
    private final ClientPaymentRepository clientPaymentRepository;
    private final ClientMapper clientMapper;
    private final CaseMapper caseMapper;
    private final DocumentMapper documentMapper;
    private final InvoiceMapper invoiceMapper;
    private final ExpenseMapper expenseMapper;
    private final TaskMapper taskMapper;
    private final CaseEventMapper caseEventMapper;
    private final ClientPaymentMapper clientPaymentMapper;

    public SearchService(AdvocateRepository advocateRepository,
                         ClientRepository clientRepository,
                         CaseRepository caseRepository,
                         DocumentRepository documentRepository,
                         InvoiceRepository invoiceRepository,
                         ExpenseRepository expenseRepository,
                         TaskRepository taskRepository,
                         CaseEventRepository caseEventRepository,
                         ClientPaymentRepository clientPaymentRepository,
                         ClientMapper clientMapper,
                         CaseMapper caseMapper,
                         DocumentMapper documentMapper,
                         InvoiceMapper invoiceMapper,
                         ExpenseMapper expenseMapper,
                         TaskMapper taskMapper,
                         CaseEventMapper caseEventMapper,
                         ClientPaymentMapper clientPaymentMapper) {
        this.advocateRepository = advocateRepository;
        this.clientRepository = clientRepository;
        this.caseRepository = caseRepository;
        this.documentRepository = documentRepository;
        this.invoiceRepository = invoiceRepository;
        this.expenseRepository = expenseRepository;
        this.taskRepository = taskRepository;
        this.caseEventRepository = caseEventRepository;
        this.clientPaymentRepository = clientPaymentRepository;
        this.clientMapper = clientMapper;
        this.caseMapper = caseMapper;
        this.documentMapper = documentMapper;
        this.invoiceMapper = invoiceMapper;
        this.expenseMapper = expenseMapper;
        this.taskMapper = taskMapper;
        this.caseEventMapper = caseEventMapper;
        this.clientPaymentMapper = clientPaymentMapper;
    }

    public SearchResponseDTO search(String email, String keyword) {
        log.info("Search keyword: \"{}\" for advocate: {}", keyword, email);

        Advocate advocate = advocateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Advocate not found: " + email));

        PageRequest limit = PageRequest.of(0, MAX_RESULTS);

        List<ClientResponseDTO> clients = clientRepository.globalSearch(advocate, keyword, limit)
                .stream().map(clientMapper::toResponseDTO).collect(Collectors.toList());

        List<CaseResponseDTO> cases = caseRepository.globalSearch(advocate, keyword, limit)
                .stream().map(caseMapper::toResponseDTO).collect(Collectors.toList());

        List<DocumentResponseDTO> documents = documentRepository.globalSearch(advocate, keyword, limit)
                .stream().map(documentMapper::toResponseDTO).collect(Collectors.toList());

        List<InvoiceResponseDTO> invoices = invoiceRepository.globalSearch(advocate, keyword, limit)
                .stream().map(invoiceMapper::toResponseDTO).collect(Collectors.toList());

        List<ExpenseResponseDTO> expenses = expenseRepository.globalSearch(advocate, keyword, limit)
                .stream().map(expenseMapper::toResponseDTO).collect(Collectors.toList());

        List<TaskResponseDTO> tasks = taskRepository.globalSearch(advocate, keyword, limit)
                .stream().map(taskMapper::toResponseDTO).collect(Collectors.toList());

        List<CaseEventResponseDTO> events = caseEventRepository.globalSearch(advocate, keyword, limit)
                .stream().map(caseEventMapper::toResponseDTO).collect(Collectors.toList());

        List<ClientPaymentResponseDTO> payments = clientPaymentRepository.globalSearch(advocate, keyword, limit)
                .stream().map(clientPaymentMapper::toResponseDTO).collect(Collectors.toList());

        log.info("Search results - clients: {}, cases: {}, documents: {}, invoices: {}, expenses: {}, tasks: {}, events: {}, payments: {}",
                clients.size(), cases.size(), documents.size(), invoices.size(), expenses.size(), tasks.size(), events.size(), payments.size());

        return new SearchResponseDTO(clients, cases, documents, invoices, expenses, tasks, events, payments);
    }
}
