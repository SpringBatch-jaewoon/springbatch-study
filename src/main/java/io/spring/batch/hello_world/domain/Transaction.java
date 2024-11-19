package io.spring.batch.hello_world.domain;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Transaction {
    private String accountNumber;
    private Date transactionDate;
    private double amount;
//    private DateFormat formatter = new SimpleDateFormat("MM/dd/YYYY");

    @Override
    public String toString() {
        return "Transaction{" +
                "accountNumber='" + accountNumber + '\'' +
                ", transactionDate=" + transactionDate +
                ", amount=" + amount +
                '}';
    }
}