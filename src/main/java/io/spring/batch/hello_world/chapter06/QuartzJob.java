package io.spring.batch.hello_world.chapter06;

import io.spring.batch.hello_world.chapter04.job.DailyJobTimeStamper;
import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
public class QuartzJob {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("QuartzJob", jobRepository)
                .start(step1())
                .incrementer(new RunIdIncrementer())
                .listener(JobListenerFactoryBean.getListener(
                        new JobLoggerListener()))
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step1 ran!");
                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }
}
