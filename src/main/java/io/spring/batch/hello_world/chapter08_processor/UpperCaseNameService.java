package io.spring.batch.hello_world.chapter08_processor;

import io.spring.batch.hello_world.domain.Customer;
import org.springframework.stereotype.Service;

@Service
public class UpperCaseNameService {

    public Customer upperCase(Customer customer){
        Customer newCustomer = new Customer(customer);
        newCustomer.setFirstName(newCustomer.getFirstName().toUpperCase());
        return newCustomer;
    }

}
