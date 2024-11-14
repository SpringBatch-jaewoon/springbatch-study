package io.spring.batch.hello_world.chapter04.step;

import io.spring.batch.hello_world.chapter04.job.DailyJobTimeStamper;
import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
public class FlowJob {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("FlowJob", jobRepository)
                .start(firstStep())
//                    .on("FAILED").to(failureStep())
//                    .from(firstStep()).on("*").to(successStep())
//                .end()
                .next(decider())
                .from(decider())
                    .on("FAILED").to(failureStep())
                .from(decider())
                    .on("*").to(successStep())
                .end()
                .incrementer(new DailyJobTimeStamper())
                .listener(JobListenerFactoryBean.getListener(
                        new JobLoggerListener()))
                .build();
    }


    @Bean
	public Step firstStep() {
		return new StepBuilder("firstStep", jobRepository)
                .tasklet(passTasklet() ,transactionManager)
				.build();
	}

	@Bean
	public Step successStep() {
        return new StepBuilder("successStep", jobRepository)
                .tasklet(successTasklet(), transactionManager)
                .build();
	}

	@Bean
	public Step failureStep() {
        return new StepBuilder("failureStep", jobRepository)
                .tasklet(failTasklet(), transactionManager)
                .build();
	}

    @Bean
	public Tasklet passTasklet() {
		return (contribution, chunkContext) -> {
//			return RepeatStatus.FINISHED;
			throw new RuntimeException("Causing a failure");
		};
	}

	@Bean
	public Tasklet successTasklet() {
		return (contribution, context) -> {
			System.out.println("Success!");
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Tasklet failTasklet() {
		return (contribution, context) -> {
			System.out.println("Failure!");
			return RepeatStatus.FINISHED;
		};
	}

    @Bean
    public JobExecutionDecider decider() {
        return new RandomDecider();
    }
}