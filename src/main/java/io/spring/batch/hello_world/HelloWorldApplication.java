package io.spring.batch.hello_world;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;


@SpringBootApplication
public class HelloWorldApplication {

	@Autowired private JobRepository jobRepository;
	@Autowired private PlatformTransactionManager transactionManager;

	@Bean
	public Job job(){
		return new JobBuilder("job", jobRepository)
				.start(step1())
				.build();
	}

	@Bean
	public Step step1() {
		return new StepBuilder("step1", jobRepository)
				.tasklet(new Tasklet() {
					@Override
					public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
							throws Exception {
						System.out.println("Hello, World!");
						return RepeatStatus.FINISHED;
					}
				}, transactionManager).build();
	}




	public static void main(String[] args) {
		SpringApplication.run(HelloWorldApplication.class, args);
	}

}
