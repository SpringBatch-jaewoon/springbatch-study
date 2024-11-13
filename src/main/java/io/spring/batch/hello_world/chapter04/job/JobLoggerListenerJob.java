package io.spring.batch.hello_world.chapter04.job;

import java.util.Arrays;
import java.util.Date;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
public class JobLoggerListenerJob {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("JobLoggerListenerJob", jobRepository)
                .start(step())
                .validator(validator())
                .incrementer(new DailyJobTimeStamper())
//                .listener(new JobLoggerListener())
                .listener(JobListenerFactoryBean.getListener(
                        new JobLoggerListener()))
                .build();
    }

    @Bean
    public CompositeJobParametersValidator validator() {
        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();

        DefaultJobParametersValidator defaultJobParametersValidator = new DefaultJobParametersValidator();
        defaultJobParametersValidator.setRequiredKeys(new String[]{"fileName"});
        defaultJobParametersValidator.setOptionalKeys(new String[]{"name", "run.id", "currentDate"});
        defaultJobParametersValidator.afterPropertiesSet();

        validator.setValidators(
                Arrays.asList(new JobParameterValidator(), defaultJobParametersValidator));

        return validator;
    }

    @Bean
    public Step step() {
        return new StepBuilder("step1", jobRepository)
                .tasklet(helloWorldTasklet(null, null), transactionManager)
                .build();
    }

    @StepScope
    @Bean
    public Tasklet helloWorldTasklet(
            @Value("#{jobParameters['name']}") String name,
            @Value("#{jobParameters['fileName']}") String fileName) {
        return (contribution, chunkContext) -> {
            System.out.println(
                    String.format("name = %s!", name));

            System.out.println(
                    String.format("fileName = %s!", fileName));

//            long runId = (long) chunkContext.getStepContext().getJobParameters().get("run.id");
//            System.out.println(
//                    String.format("run.id = %s", runId));

            Date currentDate = (Date) chunkContext.getStepContext().getJobParameters().get("currentDate");
            System.out.println(
                    String.format("date = %s", currentDate));
            return RepeatStatus.FINISHED;
        };
    }
}