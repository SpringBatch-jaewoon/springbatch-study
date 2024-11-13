package io.spring.batch.hello_world.chapter04.step;

import io.spring.batch.hello_world.chapter04.job.DailyJobTimeStamper;
import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.SimpleSystemProcessExitCodeMapper;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 시스템 명령을 비동기로 실행하고 싶을 때 사용한다.
 */

//@Configuration
public class SystemCommandTaskletJob {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("SystemCommandTaskletJob", jobRepository)
                .start(step())
                .incrementer(new DailyJobTimeStamper())
                .listener(JobListenerFactoryBean.getListener(
                        new JobLoggerListener()))
                .build();
    }


    @Bean
    public Step step() {
        return new StepBuilder("step1", jobRepository)
                .tasklet(systemCommandTasklet(), transactionManager)
                .build();
    }

    @Bean
    public SystemCommandTasklet systemCommandTasklet(){
        SystemCommandTasklet tasklet = new SystemCommandTasklet();
//        tasklet.setWorkingDirectory(System.getProperty("user.dir"));
        tasklet.setCommand("echo abc > abc.txt");
        tasklet.setTimeout(5000);
        tasklet.setInterruptOnCancel(true);
        tasklet.setSystemProcessExitCodeMapper(touchCodeMapper());
        tasklet.setTerminationCheckInterval(5000); //비동기의 시스템 명령 완료를 확인하는 주기
        tasklet.setTaskExecutor(new SimpleAsyncTaskExecutor());
        tasklet.setEnvironmentParams(new String[]{
                "JAVA_HOME=/java", "BATCH_HOME=/Users/batch"
        });
        return tasklet;
    }

    @Bean
    public SimpleSystemProcessExitCodeMapper touchCodeMapper() {
        return new SimpleSystemProcessExitCodeMapper();
    }
}
