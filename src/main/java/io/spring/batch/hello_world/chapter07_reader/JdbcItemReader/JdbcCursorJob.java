package io.spring.batch.hello_world.chapter07_reader.JdbcItemReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import io.spring.batch.hello_world.domain.Customer;
import java.text.SimpleDateFormat;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JdbcCursorJob {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("JdbcCursorJob", jobRepository)
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
	public JdbcCursorItemReader<Customer> customerItemReader(@Qualifier("serverDatasource") DataSource dataSource) {
		return new JdbcCursorItemReaderBuilder<Customer>()
				.name("customerItemReader")
				.dataSource(dataSource)
                .rowMapper(new CustomerRowMapper())
				.sql("select * from customer where city = ?")
				.preparedStatementSetter(citySetter(null))
				.build();
	}

    @Bean
	@StepScope
	public ArgumentPreparedStatementSetter citySetter(
			@Value("#{jobParameters['city']}") String city) {
		return new ArgumentPreparedStatementSetter(new Object [] {city});
	}

    @Bean
    public ItemWriter itemWriter() {
        return (items) -> items.forEach(System.out::println);
    }

}
