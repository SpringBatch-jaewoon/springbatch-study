package io.spring.batch.hello_world.chapter09_writer;

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
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemWriter;
import org.springframework.batch.item.file.builder.MultiResourceItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MultiResourceItemWriterJob {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() throws Exception {
        return new JobBuilder("job", jobRepository)
                .start(step())
                .listener(JobListenerFactoryBean.getListener(
                        new JobLoggerListener()))
                .build();
    }

    @Bean
    public Step step() throws Exception {
        return new StepBuilder("step", jobRepository)
                .<Customer, Customer>chunk(10, transactionManager)
                .reader(customerJdbcCursorItemReader(null))
                .writer(multiCustomerFileWriter(null))
                .build();
    }

    @Bean
	public JdbcCursorItemReader<Customer> customerJdbcCursorItemReader(DataSource dataSource) {
		return new JdbcCursorItemReaderBuilder<Customer>()
				.name("customerItemReader")
				.dataSource(dataSource)
				.sql("select * from customer")
				.rowMapper(new BeanPropertyRowMapper<>(Customer.class))
				.build();
	}

    @Bean
	public MultiResourceItemWriter<Customer> multiCustomerFileWriter(CustomerOutputFileSuffixCreator suffixCreator) throws Exception {
		return new MultiResourceItemWriterBuilder<Customer>()
				.name("multiCustomerFileWriter")
				.delegate(delegateItemWriter2(null))
				.itemCountLimitPerResource(25)
				.resource(new FileSystemResource("customerB"))
				.resourceSuffixCreator(suffixCreator)
				.build();
	}

	@Bean
	@StepScope
	public FlatFileItemWriter<Customer> delegateItemWriter2(CustomerRecordCountFooterCallback footerCallback) throws Exception {
		BeanWrapperFieldExtractor<Customer> fieldExtractor = new BeanWrapperFieldExtractor<>();
		fieldExtractor.setNames(new String[] {"firstName", "lastName", "address", "city", "state", "zip"});
		fieldExtractor.afterPropertiesSet();

		FormatterLineAggregator<Customer> lineAggregator = new FormatterLineAggregator<>();

		lineAggregator.setFormat("%s %s lives at %s %s in %s, %s.");
		lineAggregator.setFieldExtractor(fieldExtractor);

		FlatFileItemWriter<Customer> itemWriter = new FlatFileItemWriter<>();
		itemWriter.setName("delegateItemWriter1");
		itemWriter.setLineAggregator(lineAggregator);
		itemWriter.setAppendAllowed(true);
		itemWriter.setFooterCallback(footerCallback);
		return itemWriter;
	}

    @Bean
	@StepScope
	public StaxEventItemWriter<Customer> delegateItemWriter1(CustomerXmlHeaderCallback headerCallback) throws Exception {
		Map<String, Class> aliases = new HashMap<>();
		aliases.put("customer", Customer.class);
		XStreamMarshaller marshaller = new XStreamMarshaller();
		marshaller.setAliases(aliases);
		marshaller.afterPropertiesSet();
		return new StaxEventItemWriterBuilder<Customer>()
				.name("customerItemWriter")
				.marshaller(marshaller)
				.rootTagName("customers")
				.headerCallback(headerCallback)
				.build();
	}
}