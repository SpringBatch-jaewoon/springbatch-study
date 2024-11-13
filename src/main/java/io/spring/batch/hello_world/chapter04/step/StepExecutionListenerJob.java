package io.spring.batch.hello_world.chapter04.step;

import io.spring.batch.hello_world.chapter04.job.DailyJobTimeStamper;
import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class StepExecutionListenerJob {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("StepExecutionListenerJob", jobRepository)
                .start(step())
                .incrementer(new DailyJobTimeStamper())
                .listener(JobListenerFactoryBean.getListener(
                        new JobLoggerListener()))
                .build();
    }


    @Bean
    public Step step() {
        return new StepBuilder("step1", jobRepository)
                .<String, String>chunk(1000, transactionManager)
                .reader(itemReader())
                .writer(itemWriter())
                .listener(new StepListener())
                .build();
    }

    @Bean
    public ListItemReader<String> itemReader(){
        List<String> items = new ArrayList<>(10000);
        for(int i=0; i<10000; i++){
            items.add(UUID.randomUUID().toString());
        }
        return new ListItemReader<>(items);
    }
    @Bean
    public ItemWriter<String> itemWriter(){
        return items -> {
            for(String item : items){
                System.out.println(">> current item = " + item);
            }
        };
    }


    static class StepListener{
        @BeforeStep
        public void beforeStep (StepExecution stepExecution){
            System.out.println(stepExecution.getStepName() + " has begun!");
        }
        @AfterStep
        public ExitStatus afterStep (StepExecution stepExecution){
            System.out.println(stepExecution.getStepName() + " has ended!");
            return stepExecution.getExitStatus();
        }
        @BeforeChunk
        public void beforeChunk (ChunkContext chunkContext){
            System.out.println("chunk started");
        }
        @AfterChunk
        public void afterChunk (ChunkContext chunkContext){
            System.out.println("chunk ended");
        }

    }

}
