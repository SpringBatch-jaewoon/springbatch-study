package io.spring.batch.hello_world.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@XStreamAlias("transaction")
public class Transaction {
    @XStreamAlias("accountNumber")
    private String accountNumber;
    @XStreamAlias("transactionDate")
    private Date transactionDate;
    @XStreamAlias("amount")
    private double amount;

    @Override
    public String toString() {
        return "Transaction{" +
                "accountNumber='" + accountNumber + '\'' +
                ", transactionDate=" + transactionDate +
                ", amount=" + amount +
                '}';
    }
}