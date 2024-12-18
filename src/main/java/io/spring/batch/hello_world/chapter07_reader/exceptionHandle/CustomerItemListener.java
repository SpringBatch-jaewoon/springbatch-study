package io.spring.batch.hello_world.chapter07_reader.exceptionHandle;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.batch.core.annotation.OnReadError;
import org.springframework.batch.item.file.FlatFileParseException;

public class CustomerItemListener  {

    private static final Log logger = LogFactory.getLog(CustomerItemListener.class);

    @OnReadError
    public void onReadError(Exception e) {
        if(e instanceof FlatFileParseException) { //글자수 63개 초과 시
            FlatFileParseException ffpe = (FlatFileParseException) e;

            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("An error occured while processing the " +
                    ffpe.getLineNumber() +
                    " line of the file.  Below was the faulty input.\n");
            errorMessage.append(ffpe.getInput() + "\n");
            logger.error(errorMessage.toString(), ffpe);
        } else {
            logger.error("An error has occurred", e);
        }
    }
}