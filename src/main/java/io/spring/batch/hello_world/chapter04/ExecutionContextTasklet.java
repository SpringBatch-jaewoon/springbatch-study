package io.spring.batch.hello_world.chapter04;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;

public class ExecutionContextTasklet implements Tasklet {


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        String name = (String) chunkContext.getStepContext()
                .getJobParameters()
                .get("name");

        ExecutionContext jobExecutionContext =
                chunkContext.getStepContext()
                        .getStepExecution()
						.getJobExecution()
                        .getExecutionContext();

        ExecutionContext stepExecutionContext =
                chunkContext.getStepContext()
                        .getStepExecution()
                        .getExecutionContext();


        jobExecutionContext.put("name(job)", name+"job");
        stepExecutionContext.put("name(step)", name+"step");
        return RepeatStatus.FINISHED;
    }
}
