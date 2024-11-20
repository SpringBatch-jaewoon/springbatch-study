package io.spring.batch.hello_world.chapter07_reader.StoredProcedureItemReader;

import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import io.spring.batch.hello_world.chapter07_reader.JPAItemReader.CustomerByCityQueryProvider;
import io.spring.batch.hello_world.chapter07_reader.JdbcItemReader.CustomerRowMapper;
import io.spring.batch.hello_world.domain.Customer;
import jakarta.persistence.EntityManagerFactory;
import java.sql.Types;
import java.util.Collections;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.StoredProcedureItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.StoredProcedureItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class StoredProcedureJob {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("StoredProcedureJob", jobRepository)
                .start(copyFileStep())
                .listener(JobListenerFactoryBean.getListener(new JobLoggerListener()))
                .build();
    }

    @Bean
    public Step copyFileStep() {
        return new StepBuilder("copyFileStep", jobRepository)
                .<Customer, Customer>chunk(10, transactionManager)
                .reader(customerItemReader(null, null))
                .writer(itemWriter())
                .build();
    }


    @Bean
    @StepScope
    public StoredProcedureItemReader<Customer> customerItemReader(
            DataSource dataSource,
            @Value("#{jobParameters['city']}") String city) {

		return new StoredProcedureItemReaderBuilder<Customer>()
				.name("customerItemReader")
				.dataSource(dataSource)
				.procedureName("customer_list")
				.parameters(new SqlParameter[]{new SqlParameter("cityOption", Types.VARCHAR)})
				.preparedStatementSetter(new ArgumentPreparedStatementSetter(new Object[] {city}))
				.rowMapper(new CustomerRowMapper())
				.build();
    }

    @Bean
    public ItemWriter itemWriter() {
        return (items) -> items.forEach(System.out::println);
    }
}