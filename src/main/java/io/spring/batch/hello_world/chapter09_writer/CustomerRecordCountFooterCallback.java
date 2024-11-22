package io.spring.batch.hello_world.chapter09_writer;

import io.spring.batch.hello_world.domain.Customer;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class CustomerRecordCountFooterCallback implements FlatFileFooterCallback {

    private int itemsWrittenInCurrentFile = 0;

    @Override
    public void writeFooter(Writer writer) throws IOException {
        writer.write("This file contains " +
                itemsWrittenInCurrentFile + " items");
    }

    //org.springframework.batch.item.file.FlatFileItemWriter.write(..)
    //org.springframework.batch.item.support.AbstractFileItemWriter.write(..)
    @Before("execution(* org.springframework.batch.item.support.AbstractFileItemWriter.write(..))")
    public void beforeWrite(JoinPoint joinPoint) {
        Chunk chunk = (Chunk) (joinPoint.getArgs()[0]);
        this.itemsWrittenInCurrentFile += chunk.getItems().size();
    }

    @Before("execution(* org.springframework.batch.item.support.AbstractFileItemWriter.open(..))")
    public void resetCounter() {
        this.itemsWrittenInCurrentFile = 0;
    }
}