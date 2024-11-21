package io.spring.batch.hello_world.chapter09_writer;

import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import io.spring.batch.hello_world.domain.Customer;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JdbcBatchItemWriterJob {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() throws Exception {
        return new JobBuilder("job", jobRepository)
                .start(copyFileStep())
                .listener(JobListenerFactoryBean.getListener(
                        new JobLoggerListener()))
                .build();
    }

    @Bean
    public Step copyFileStep() throws Exception {
        return new StepBuilder("copyFileStep", jobRepository)
                .<Customer, Customer>chunk(10, transactionManager)
                .reader(customerItemReader(null))
                .writer(jdbcCustomerWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> customerItemReader(
            @Value("#{jobParameters['customerFile']}") ClassPathResource inputFile) {
        //customer.csv

        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .delimited()
                .names(new String[]{"firstName",
                        "middleInitial",
                        "lastName",
                        "address",
                        "city",
                        "state",
                        "zip"})
                .targetType(Customer.class)
                .resource(inputFile)
                .build();
    }


	@Bean
	public JdbcBatchItemWriter<Customer> jdbcCustomerWriter(DataSource dataSource) throws Exception {
		return new JdbcBatchItemWriterBuilder<Customer>()
				.dataSource(dataSource)
				.sql("INSERT INTO CUSTOMER (firstName, " +
						"middleInitial, " +
						"lastName, " +
						"address, " +
						"city, " +
						"state, " +
						"zip) VALUES (:firstName, " +
						":middleInitial, " +
						":lastName, " +
						":address, " +
						":city, " +
						":state, " +
						":zip)")
				.beanMapped()
				.build();
	}


}