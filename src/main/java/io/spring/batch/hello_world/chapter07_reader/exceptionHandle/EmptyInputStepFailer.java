package io.spring.batch.hello_world.chapter07_reader.exceptionHandle;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;

public class EmptyInputStepFailer {
    @AfterStep
    public ExitStatus afterStep(StepExecution execution){
        if(execution.getReadCount() > 0){
            return execution.getExitStatus();
        } else {
            return ExitStatus.FAILED;
        }
    }
}
