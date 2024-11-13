package io.spring.batch.hello_world.chapter04.step;

import io.spring.batch.hello_world.chapter04.job.DailyJobTimeStamper;
import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 다른 클래스 내의 메서드를 잡 내의 태스크릿처럼 실행하고 싶을 때 사용한다.
 */

@Configuration
public class MethodInvokingTaskletAdapterJob {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private CustomerService customerService;

    @Bean
    public Job job() {
        return new JobBuilder("MethodInvokingTaskletAdapterJob", jobRepository)
                .start(step())
                .next(step2())
                .incrementer(new DailyJobTimeStamper())
                .listener(JobListenerFactoryBean.getListener(
                        new JobLoggerListener()))
                .build();
    }


    @Bean
    public Step step() {
        return new StepBuilder("step1", jobRepository)
                .tasklet(methodInvokingTasklet(), transactionManager)
                .build();
    }

    @Bean
    public MethodInvokingTaskletAdapter methodInvokingTasklet(){
        MethodInvokingTaskletAdapter methodInvokingTaskletAdapter = new MethodInvokingTaskletAdapter();
        methodInvokingTaskletAdapter.setTargetObject(customerService);
        methodInvokingTaskletAdapter.setTargetMethod("serviceMethod");
        return methodInvokingTaskletAdapter;
    }

    @Bean
    public Step step2() {
        return new StepBuilder("step2", jobRepository)
                .tasklet(methodInvokingTasklet2(null), transactionManager)
                .build();
    }

    @StepScope
    @Bean
    public MethodInvokingTaskletAdapter methodInvokingTasklet2(
            @Value("#{jobParameters['message']}") String message) {
        MethodInvokingTaskletAdapter methodInvokingTaskletAdapter = new MethodInvokingTaskletAdapter();
        methodInvokingTaskletAdapter.setTargetObject(customerService);
        methodInvokingTaskletAdapter.setTargetMethod("serviceMethod2");
        methodInvokingTaskletAdapter.setArguments(new String[] {message});
        return methodInvokingTaskletAdapter;
    }
}
