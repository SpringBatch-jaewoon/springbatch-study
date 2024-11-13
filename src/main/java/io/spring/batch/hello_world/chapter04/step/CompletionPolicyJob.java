package io.spring.batch.hello_world.chapter04.step;

import io.spring.batch.hello_world.chapter04.job.DailyJobTimeStamper;
import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CompletionPolicyJob {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("CompletionPolicyJob", jobRepository)
                .start(step())
                .incrementer(new DailyJobTimeStamper())
                .listener(JobListenerFactoryBean.getListener(
                        new JobLoggerListener()))
                .build();
    }


    @Bean
    public Step step() {
        return new StepBuilder("step1", jobRepository)
//                .<String, String>chunk(1000, transactionManager)
//                .<String, String>chunk(completionPolicy(), transactionManager)
                .<String, String>chunk(randomCompletionPolicy(), transactionManager)
                .reader(itemReader())
                .writer(itemWriter())
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
    @Bean
    public CompletionPolicy completionPolicy(){
        CompositeCompletionPolicy policy = new CompositeCompletionPolicy();
        policy.setPolicies(
                new CompletionPolicy[]{
                        new TimeoutTerminationPolicy(3),
                        new SimpleCompletionPolicy(1000)
                }
        );
        return policy;
    }

    @Bean
    public CompletionPolicy randomCompletionPolicy() {
        return new RandomChunkSizePolicy();
    }

    static class RandomChunkSizePolicy implements CompletionPolicy {

        private int chunkSize;
        private int totalProcessed;
        private Random random = new Random();

        @Override
        public boolean isComplete(RepeatContext context, RepeatStatus result) {
            if(RepeatStatus.FINISHED == result) return true;
            else return isComplete(context);
        }

        @Override
        public boolean isComplete(RepeatContext context) {
            return this.totalProcessed >= chunkSize;
        }

        @Override
        public RepeatContext start(RepeatContext parent) {
            this.chunkSize = random.nextInt(20);
            this.totalProcessed = 0;
            System.out.println("The chunk size has been set to " + this.chunkSize);
            return parent;
        }

        @Override
        public void update(RepeatContext context) {
            this.totalProcessed++;
        }
    }
}
