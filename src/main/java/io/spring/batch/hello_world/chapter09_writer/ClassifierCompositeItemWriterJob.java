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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.builder.ClassifierCompositeItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ClassifierCompositeItemWriterJob {
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
                .reader(itemReader(null))
                .writer(classifierCompositeItemWriter())
                .stream(xmlDelegate(null))
                .build();
    }

    @Bean
	@StepScope
	public FlatFileItemReader<Customer> itemReader(
            @Value("#{jobParameters['customerFile']}") ClassPathResource inputFile) {
		return new FlatFileItemReaderBuilder<Customer>()
				.name("itemReader")
				.resource(inputFile)
				.delimited()
				.names(new String[] {"firstName",
						"middleInitial",
						"lastName",
						"address",
						"city",
						"state",
						"zip",
						"email"})
				.targetType(Customer.class)
				.build();
	}

    @Bean
    public ClassifierCompositeItemWriter<Customer> classifierCompositeItemWriter() throws Exception {
        Classifier<Customer, ItemWriter<? super Customer>> classifier
                = new CustomerClassifier(xmlDelegate(null), jdbcDelegate(null));

        return new ClassifierCompositeItemWriterBuilder<Customer>()
                .classifier(classifier)
                .build();
    }

	@Bean
	@StepScope
	public StaxEventItemWriter<Customer> xmlDelegate(
			@Value("#{jobParameters['outputFile']}") FileSystemResource outputFile) throws Exception {

        Map<String, Class> aliases = new HashMap<>();
        aliases.put("customer", Customer.class);
        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(aliases);
        marshaller.afterPropertiesSet();
        return new StaxEventItemWriterBuilder<Customer>()
                .name("xmlDelegate")
                .resource(outputFile)
                .marshaller(marshaller)
                .rootTagName("customers")
                .build();
	}


	@Bean
	public JdbcBatchItemWriter<Customer> jdbcDelegate(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Customer>()
				.namedParametersJdbcTemplate(new NamedParameterJdbcTemplate(dataSource))
				.sql("INSERT INTO CUSTOMER (firstName, " +
						"middleInitial, " +
						"lastName, " +
						"address, " +
						"city, " +
						"state, " +
						"zip, " +
						"email) " +
						"VALUES(:firstName, " +
						":middleInitial, " +
						":lastName, " +
						":address, " +
						":city, " +
						":state, " +
						":zip, " +
						":email)")
				.beanMapped()
				.build();
	}
}
