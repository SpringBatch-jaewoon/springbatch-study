package io.spring.batch.hello_world.chapter07_reader.ItemReaderAdapter;

import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import io.spring.batch.hello_world.chapter07_reader.JPAItemReader.CustomerRepository;
import io.spring.batch.hello_world.domain.Customer;
import java.util.Collections;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ItemReaderAdapterJob {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("ItemReaderAdapterJob", jobRepository)
                .start(copyFileStep())
                .listener(JobListenerFactoryBean.getListener(new JobLoggerListener()))
                .build();
    }

    @Bean
    public Step copyFileStep() {
        return new StepBuilder("copyFileStep", jobRepository)
                .<Customer, Customer>chunk(10, transactionManager)
                .reader(customerItemReader(null))
                .writer(itemWriter())
                .build();
    }


    @Bean
    @StepScope
    public ItemReaderAdapter<Customer> customerItemReader(CustomerService customerService){
        ItemReaderAdapter adapter = new ItemReaderAdapter();
        adapter.setTargetObject(customerService);
        adapter.setTargetMethod("getCustomer");
        return adapter;
    }

    @Bean
    public ItemWriter itemWriter() {
        return (items) -> items.forEach(System.out::println);
    }
}