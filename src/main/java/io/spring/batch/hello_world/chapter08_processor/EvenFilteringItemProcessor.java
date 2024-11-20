package io.spring.batch.hello_world.chapter08_processor;

import io.spring.batch.hello_world.domain.Customer;
import org.springframework.batch.item.ItemProcessor;

public class EvenFilteringItemProcessor implements ItemProcessor<Customer, Customer> {

    @Override
    public Customer process(Customer item)  {
        return Integer.parseInt(item.getZip()) % 2 == 0 ? null: item;
    }
}
