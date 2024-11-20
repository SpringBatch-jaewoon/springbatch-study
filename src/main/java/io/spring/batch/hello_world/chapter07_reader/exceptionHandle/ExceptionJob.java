package io.spring.batch.hello_world.chapter07_reader.exceptionHandle;

import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import io.spring.batch.hello_world.chapter07_reader.ItemStreamReader.CustomerItemReader;
import io.spring.batch.hello_world.domain.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ExceptionJob {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("ExceptionJob", jobRepository)
                .start(copyFileStep())
                .listener(JobListenerFactoryBean.getListener(
                        new JobLoggerListener()))
                .build();
    }

    @Bean
    public Step copyFileStep() {
        return new StepBuilder("copyFileStep", jobRepository)
                .<Customer, Customer>chunk(10, transactionManager)
                .reader(customerItemReader())
                .writer(itemWriter())
                .faultTolerant()
//                .skip(Exception.class)
//                .noSkip(ParseException.class)
//                .skipLimit(10)
//                .skipPolicy(new JobSkipPolicy())
                .skip(Exception.class)
                .skipLimit(100)
//                .listener(customerItemListener())
                .listener(emptyInputStepFailer())
                .build();
    }
    @Bean
    public CustomerItemReader customerItemReader(){
        CustomerItemReader customerItemReader= new CustomerItemReader();
        customerItemReader.setName("customerItemReader");
        return customerItemReader;
    }
    @Bean
    public CustomerItemListener customerItemListener(){
        return new CustomerItemListener();
    }
    @Bean
    public EmptyInputStepFailer emptyInputStepFailer(){
        return new EmptyInputStepFailer();
    }
    @Bean
    public ItemWriter<Customer> itemWriter() {
        return (items) -> items.forEach(System.out::println);
    }
}
