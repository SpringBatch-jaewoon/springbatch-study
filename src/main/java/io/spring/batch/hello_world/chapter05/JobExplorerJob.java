package io.spring.batch.hello_world.chapter05;

import io.spring.batch.hello_world.chapter04.job.DailyJobTimeStamper;
import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JobExplorerJob {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private JobExplorer jobExplorer;

    @Bean
    public Job job() {
        return new JobBuilder("JobExplorerJob", jobRepository)
                .start(step())
                .incrementer(new DailyJobTimeStamper())
                .listener(JobListenerFactoryBean.getListener(
                        new JobLoggerListener()))
                .build();
    }
    @Bean
    public Step step() {
        return new StepBuilder("explorerStep", jobRepository)
                .tasklet(explorerTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet explorerTasklet() {
        return new ExploringTasklet(this.jobExplorer);
    }
}
