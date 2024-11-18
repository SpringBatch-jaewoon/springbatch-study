package io.spring.batch.hello_world.chapter07_reader.FlatFileItemReader;

import io.spring.batch.hello_world.domain.Customer;
import io.spring.batch.hello_world.domain.Transaction;
import java.util.ArrayList;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;

public class CustomerMultiFileReader implements ResourceAwareItemReaderItemStream<Customer> {

    private Object curItem = null;

    private ResourceAwareItemReaderItemStream<Object> delegate;

    public CustomerMultiFileReader(ResourceAwareItemReaderItemStream<Object> delegate) {
        this.delegate = delegate;
    }

    public Customer read() throws Exception {
        if(curItem == null) {
            curItem = delegate.read();
        }

        Customer item = (Customer) curItem;
        curItem = null;

        if(item != null) {
			item.setTransactions(new ArrayList<>());

			while(peek() instanceof Transaction) {
				item.getTransactions().add((Transaction) curItem);
				curItem = null;
			}
		}

        return item;
    }

    private Object peek() throws Exception {
        if (curItem == null) {
            curItem = delegate.read();
        }
        return curItem;
    }

    public void close() throws ItemStreamException {
        delegate.close();
    }

    public void open(ExecutionContext arg0) throws ItemStreamException {
        delegate.open(arg0);
    }

    public void update(ExecutionContext arg0) throws ItemStreamException {
        delegate.update(arg0);
    }

    @Override
    public void setResource(Resource resource) {
        this.delegate.setResource(resource);
    }
}