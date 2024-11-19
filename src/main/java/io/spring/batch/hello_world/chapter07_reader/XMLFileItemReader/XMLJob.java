package io.spring.batch.hello_world.chapter07_reader.XMLFileItemReader;

import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import io.spring.batch.hello_world.domain.Customer;
import io.spring.batch.hello_world.domain.Transaction;
import java.util.HashMap;
import java.util.Map;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class XMLJob {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("XMLJob", jobRepository)
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
	public StaxEventItemReader<Customer> customerFileReader(
			@Value("#{jobParameters['customerFile']}") ClassPathResource inputFile) {

		return new StaxEventItemReaderBuilder<Customer>()
				.name("customerFileReader")
				.resource(inputFile)
				.addFragmentRootElements("customer")
				.unmarshaller(customerMarshaller())
				.build();
	}

	@Bean
	public XStreamMarshaller customerMarshaller() {
        Map<String, Class<?>> aliases = new HashMap<>();

        // 첫번째 키는 조각의 루트 엘리먼트, 값은 바인딩할 객체 타입
        aliases.put("customer", Customer.class);

        // 두번째 부터는 하위 엘리먼트와 각 클래스 타입
        aliases.put("firstName", String.class);
        aliases.put("middleInital", String.class);
        aliases.put("lastName", String.class);
        aliases.put("address", String.class);
        aliases.put("city", String.class);
        aliases.put("state", String.class);
        aliases.put("zip", String.class);
        aliases.put("transaction", Transaction.class);

        XStreamMarshaller xStreamMarshaller = new XStreamMarshaller();
        xStreamMarshaller.setAliases(aliases);
        return xStreamMarshaller;
	}

	@Bean
	public ItemWriter itemWriter() {
		return (items) -> items.forEach(System.out::println);
	}

}
