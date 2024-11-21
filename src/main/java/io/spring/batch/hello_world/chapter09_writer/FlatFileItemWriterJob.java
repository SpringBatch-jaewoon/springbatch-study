package io.spring.batch.hello_world.chapter09_writer;

import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import io.spring.batch.hello_world.chapter08_processor.EvenFilteringItemProcessor;
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
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.WritableResource;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
public class FlatFileItemWriterJob {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("job", jobRepository)
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
                .writer(customerItemWriter2(null))
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
	@StepScope
	public FlatFileItemWriter<Customer> customerItemWriter(
			@Value("#{jobParameters['outputFile']}") FileSystemResource outputFile) {

		return new FlatFileItemWriterBuilder<Customer>()
				.name("customerItemWriter")
                .resource(outputFile)
                .formatted()
                .format("%s %s lives at %s %s in %s, %s.")
				.names(new String[] {"firstName",
                        "lastName", "address", "city", "state", "zip"})
				.build();
	}

    @Bean
    @StepScope
    public FlatFileItemWriter<Customer> customerItemWriter2(
            @Value("#{jobParameters['outputFile']}") FileSystemResource outputFile) {

        return new FlatFileItemWriterBuilder<Customer>()
                .name("customerItemWriter")
                .resource(outputFile)
                .delimited()
                .delimiter(";")
                .names(new String[] {"firstName",
                        "lastName", "address", "city", "state", "zip"})
                .build();
    }
}