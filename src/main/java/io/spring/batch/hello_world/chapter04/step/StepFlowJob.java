package io.spring.batch.hello_world.chapter04.step;

import io.spring.batch.hello_world.chapter04.job.DailyJobTimeStamper;
import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class StepFlowJob {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("stepFlowJob", jobRepository)
                .start(initializeBatch())
                .next(runBatch())
                .incrementer(new DailyJobTimeStamper())
                .listener(JobListenerFactoryBean.getListener(
                        new JobLoggerListener()))
                .build();
    }

    @Bean
    public Step initializeBatch() {
        return new StepBuilder("initializeBatch", jobRepository)
                .flow(preProcessingFlow())
                .build();
    }

    @Bean
    public Flow preProcessingFlow() {
        return new FlowBuilder<Flow>("preProcessingFlow")
                .start(loadFileStep())
                .next(loadCustomerStep())
                .next(updateStartStep())
                .build();
    }


    @Bean
    public Step loadFileStep() {
        return new StepBuilder("loadFileStep", jobRepository)
                .tasklet(loadStockFile(), transactionManager)
                .build();
    }

    @Bean
    public Step loadCustomerStep() {
        return new StepBuilder("loadCustomerStep", jobRepository)
                .tasklet(loadCustomerFile(), transactionManager)
                .build();
    }

    @Bean
    public Step updateStartStep() {
        return new StepBuilder("updateStartStep", jobRepository)
                .tasklet(updateStart(), transactionManager)
                .build();
    }

    @Bean
    public Step runBatch() {
        return new StepBuilder("runBatch", jobRepository)
                .tasklet(runBatchTasklet(), transactionManager)
                .build();
    }

    @Bean
	public Tasklet loadStockFile() {
		return (contribution, chunkContext) -> {
			System.out.println("The stock file has been loaded");
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Tasklet loadCustomerFile() {
		return (contribution, chunkContext) -> {
			System.out.println("The customer file has been loaded");
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Tasklet updateStart() {
		return (contribution, chunkContext) -> {
			System.out.println("The start has been updated");
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Tasklet runBatchTasklet() {
        return (contribution, chunkContext) -> {
            System.out.println("The batch has been run");
            return RepeatStatus.FINISHED;
        };
    }
}