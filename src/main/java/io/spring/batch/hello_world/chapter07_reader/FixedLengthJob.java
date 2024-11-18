package io.spring.batch.hello_world.chapter07_reader;

import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class FixedLengthJob {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("FixedLengthJob", jobRepository)
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
				.fixedLength()
				.columns(new Range[]{new Range(1,11), new Range(12, 12), new Range(13, 22),
						new Range(23, 26), new Range(27,46), new Range(47,62), new Range(63,64),
						new Range(65,69)})
				.names(new String[] {"firstName", "middleInitial", "lastName",
						"addressNumber", "street", "city", "state","zipCode"})
				.targetType(Customer.class)
				.build();
	}

	@Bean
	public ItemWriter<Customer> itemWriter() {
		return (items) -> items.forEach(System.out::println);
	}
}
