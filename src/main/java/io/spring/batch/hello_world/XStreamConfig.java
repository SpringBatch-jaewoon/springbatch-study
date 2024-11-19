package io.spring.batch.hello_world;

import static com.thoughtworks.xstream.XStream.PRIORITY_NORMAL;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import io.spring.batch.hello_world.domain.Customer;
import io.spring.batch.hello_world.domain.Transaction;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.xstream.XStreamMarshaller;

@Configuration
public class XStreamConfig {
    public XStreamConfig(XStreamMarshaller marshaller) {
        XStream xstream = marshaller.getXStream();
        xstream.allowTypesByWildcard(new String[]{"io.spring.batch.hello_world.**"});
        xstream.registerConverter(new DateConverter("yyyy-MM-dd HH:mm:ss",
                new String[] {"yyyy-MM-dd hh:mm:ss"}), PRIORITY_NORMAL);
        xstream.processAnnotations(new Class[] {Customer.class, Transaction.class});
        xstream.addImplicitCollection(Customer.class, "transactions");
    }
}
