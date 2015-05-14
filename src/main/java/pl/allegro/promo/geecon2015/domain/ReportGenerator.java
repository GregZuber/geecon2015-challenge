package pl.allegro.promo.geecon2015.domain;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pl.allegro.promo.geecon2015.domain.stats.FinancialStatisticsRepository;
import pl.allegro.promo.geecon2015.domain.stats.FinancialStats;
import pl.allegro.promo.geecon2015.domain.transaction.TransactionRepository;
import pl.allegro.promo.geecon2015.domain.transaction.UserTransaction;
import pl.allegro.promo.geecon2015.domain.transaction.UserTransactions;
import pl.allegro.promo.geecon2015.domain.user.User;
import pl.allegro.promo.geecon2015.domain.user.UserRepository;

@Component
public class ReportGenerator {
    
    private final FinancialStatisticsRepository financialStatisticsRepository;
    
    private final UserRepository userRepository;
    
    private final TransactionRepository transactionRepository;

    @Autowired
    public ReportGenerator(FinancialStatisticsRepository financialStatisticsRepository,
                           UserRepository userRepository,
                           TransactionRepository transactionRepository) {
        this.financialStatisticsRepository = financialStatisticsRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public Report generate(ReportRequest request) {
        FinancialStats financialStats = getFinancialStats(request);
        return generateReport(financialStats);
    }

    private FinancialStats getFinancialStats(ReportRequest request) {
        return financialStatisticsRepository.listUsersWithMinimalIncome(request.getMinimalIncome(), 
                request.getUsersToCheck());
    }
    
    private Report generateReport(FinancialStats financialStats) {
        Report report = new Report();
        for (UUID uuid : financialStats){
            ReportedUser reportedUser = getReportedUser(uuid);
            report.add(reportedUser);
        }
        return report;
    }

    private ReportedUser getReportedUser(UUID uuid) {
        User user = userRepository.detailsOf(uuid);
        UserTransactions transactions = transactionRepository.transactionsOf(uuid);
        BigDecimal transactionsNumber = getTransactionsNumber(transactions);
        
        return new ReportedUser(uuid, user.getName(), transactionsNumber);
    }

    private BigDecimal getTransactionsNumber(UserTransactions transactions) {
        if (transactions == null) {
            return null;
        }
        
        BigDecimal transactionsNumber = new BigDecimal(0);
        for (UserTransaction transaction : transactions.getTransactions()) {
            transactionsNumber = transactionsNumber.add(transaction.getAmount());
        }
        return transactionsNumber;
    }
    
}
