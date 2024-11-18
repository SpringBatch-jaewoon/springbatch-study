package io.spring.batch.hello_world.chapter06_bank;

import io.spring.batch.hello_world.chapter06_bank.domain.AccountSummary;
import io.spring.batch.hello_world.chapter06_bank.domain.Transaction;
import java.util.List;
import org.springframework.batch.item.ItemProcessor;

public class TransactionApplierProcessor implements ItemProcessor<AccountSummary, AccountSummary> {
    private TransactionDao transactionDao;

    public TransactionApplierProcessor(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    @Override
    public AccountSummary process(AccountSummary summary) throws Exception {
        List<Transaction> transactions = transactionDao
                .getTransactionsByAccountNumber(summary.getAccountNumber());

        for (Transaction transaction : transactions) {
            summary.setCurrentBalance(summary.getCurrentBalance()
                    + transaction.getAmount());
        }
        return summary;
    }
}
