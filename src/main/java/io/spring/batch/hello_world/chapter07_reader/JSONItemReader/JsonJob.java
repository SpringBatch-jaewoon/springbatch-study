package io.spring.batch.hello_world.chapter07_reader.JSONItemReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import io.spring.batch.hello_world.domain.Customer;
import java.text.SimpleDateFormat;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JsonJob {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("JsonJob", jobRepository)
                .start(copyFileStep())
                .listener(JobListenerFactoryBean.getListener(new JobLoggerListener()))
                .build();
    }

    @Bean
    public Step copyFileStep() {
        return new StepBuilder("copyFileStep", jobRepository)
                .<Customer, Customer>chunk(10, transactionManager)
                .reader(customerFileReader(null))
                .writer(itemWriter())
                .build();
    }

    @Bean
	@StepScope
	public JsonItemReader<Customer> customerFileReader(
            @Value("#{jobParameters['customerFile']}") ClassPathResource inputFile) {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));

		JacksonJsonObjectReader<Customer> jsonObjectReader = new JacksonJsonObjectReader<>(Customer.class);
		jsonObjectReader.setMapper(objectMapper);

		return new JsonItemReaderBuilder<Customer>()
				.name("customerFileReader")
				.jsonObjectReader(jsonObjectReader)
				.resource(inputFile)
				.build();
	}

	@Bean
	public ItemWriter itemWriter() {
		return (items) -> items.forEach(System.out::println);
	}


}
