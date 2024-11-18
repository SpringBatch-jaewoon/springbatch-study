package io.spring.batch.hello_world.chapter06_bank;

import io.spring.batch.hello_world.domain.Transaction;
import java.util.List;

public interface TransactionDao {

    List<Transaction> getTransactionsByAccountNumber(String accountNumber);

}
