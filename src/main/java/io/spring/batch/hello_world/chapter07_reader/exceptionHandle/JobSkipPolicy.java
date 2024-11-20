package io.spring.batch.hello_world.chapter07_reader.exceptionHandle;

import java.io.FileNotFoundException;
import java.text.ParseException;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

public class JobSkipPolicy implements SkipPolicy {

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        if(t instanceof FileNotFoundException){
            return false;
        } else if(t instanceof ParseException && skipCount<=10){
            return true;
        } else return false;
    }
}
