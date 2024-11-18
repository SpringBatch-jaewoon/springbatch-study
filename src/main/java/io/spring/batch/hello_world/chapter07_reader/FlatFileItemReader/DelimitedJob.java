package io.spring.batch.hello_world.chapter07_reader.FlatFileItemReader;

import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import io.spring.batch.hello_world.domain.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DelimitedJob {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("DelimitedJob", jobRepository)
                .start(copyFileStep())
                .listener(JobListenerFactoryBean.getListener(
                        new JobLoggerListener()))
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
    public FlatFileItemReader<Customer> customerItemReader(
            @Value("#{jobParameters['customerFile']}") ClassPathResource inputFile) {

        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .resource(inputFile)
                .lineTokenizer(new CustomerFileLineTokenizer())
                .targetType(Customer.class)
                .build();
    }

    @Bean
    public ItemWriter<Customer> itemWriter() {
        return (items) -> items.forEach(System.out::println);
    }
}
