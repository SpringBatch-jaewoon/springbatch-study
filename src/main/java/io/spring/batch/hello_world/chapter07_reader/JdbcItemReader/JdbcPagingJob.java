package io.spring.batch.hello_world.chapter07_reader.JdbcItemReader;

import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import io.spring.batch.hello_world.domain.Customer;
import java.util.HashMap;
import java.util.Map;
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
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JdbcPagingJob {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("JdbcPagingJob", jobRepository)
                .start(copyFileStep())
                .listener(JobListenerFactoryBean.getListener(new JobLoggerListener()))
                .build();
    }

    @Bean
    public Step copyFileStep() {
        return new StepBuilder("copyFileStep", jobRepository)
                .<Customer, Customer>chunk(10, transactionManager)
                .reader(customerItemReader(null, null, null))
                .writer(itemWriter())
                .build();
    }


	@Bean
	@StepScope
	public JdbcPagingItemReader<Customer> customerItemReader(@Qualifier("serverDatasource") DataSource dataSource,
                                                             PagingQueryProvider pagingQueryProvider,
                                                             @Value("#{jobParameters['city']}") String city) {

		Map<String, Object> parameterValues = new HashMap<>(1);
		parameterValues.put("city", city);

		return new JdbcPagingItemReaderBuilder<Customer>()
				.name("customerItemReader")
				.dataSource(dataSource)
				.queryProvider(pagingQueryProvider)
				.parameterValues(parameterValues)
				.pageSize(10)
				.rowMapper(new CustomerRowMapper())
				.build();
	}

    @Bean
	public SqlPagingQueryProviderFactoryBean pagingQueryProvider(@Qualifier("serverDatasource") DataSource dataSource) {
		SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
		factoryBean.setDataSource(dataSource);
		factoryBean.setSelectClause("select *");
		factoryBean.setFromClause("from Customer");
		factoryBean.setWhereClause("where city = :city");
		factoryBean.setSortKey("lastName");
		return factoryBean;
	}

    @Bean
    public ItemWriter itemWriter() {
        return (items) -> items.forEach(System.out::println);
    }

}
