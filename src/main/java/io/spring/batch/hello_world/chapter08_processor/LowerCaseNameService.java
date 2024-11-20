package io.spring.batch.hello_world.chapter08_processor;

import io.spring.batch.hello_world.domain.Customer;
import org.springframework.stereotype.Service;

@Service
public class LowerCaseNameService {

    public Customer lowerCase(Customer customer){
        Customer newCustomer = new Customer(customer);
        newCustomer.setFirstName(newCustomer.getFirstName().toLowerCase());
        return newCustomer;
    }

}
