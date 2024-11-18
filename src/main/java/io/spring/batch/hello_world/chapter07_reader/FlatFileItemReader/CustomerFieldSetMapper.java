package io.spring.batch.hello_world.chapter07_reader.FlatFileItemReader;

import io.spring.batch.hello_world.domain.Customer;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

public class CustomerFieldSetMapper implements FieldSetMapper {

    @Override
    public Customer mapFieldSet(FieldSet fieldSet) {
        Customer customer = new Customer();

        customer.setAddress(fieldSet.readString("addressNumber") +
                " " + fieldSet.readString("street"));
//        customer.setCity(fieldSet.readString("address"));
        customer.setCity(fieldSet.readString("city"));
        customer.setFirstName(fieldSet.readString("firstName"));
        customer.setLastName(fieldSet.readString("lastName"));
        customer.setMiddleInitial(fieldSet.readString("middleInitial"));
        customer.setState(fieldSet.readString("state"));
        customer.setZipCode(fieldSet.readString("zipCode"));

        return customer;
    }
}
