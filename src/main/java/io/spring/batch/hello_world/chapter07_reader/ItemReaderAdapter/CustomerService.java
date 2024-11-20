package io.spring.batch.hello_world.chapter07_reader.ItemReaderAdapter;

import io.spring.batch.hello_world.domain.Customer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class CustomerService {
    private List<Customer> customers;
    private int curIndex;
    private Random generator = new Random();

    public CustomerService(){
        curIndex=0;
        customers = new ArrayList<>();
        for(int i=0; i<100; i++){
            customers.add(buildCustomer());
        }
    }

    private Customer buildCustomer(){
        Customer customer = new Customer();
//        customer.setId((long) generator.nextInt(Integer.MAX_VALUE));
        return customer;
    }

    public Customer getCustomer(){
        Customer cust =null;
        if(curIndex < customers.size()){
            cust = customers.get(curIndex);
            curIndex++;
        }
        return cust;
    }
}
