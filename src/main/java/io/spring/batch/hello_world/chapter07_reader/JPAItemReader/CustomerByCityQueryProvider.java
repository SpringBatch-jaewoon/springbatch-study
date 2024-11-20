package io.spring.batch.hello_world.chapter07_reader.JPAItemReader;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.batch.item.database.orm.AbstractJpaQueryProvider;
import org.springframework.util.Assert;

public class CustomerByCityQueryProvider extends AbstractJpaQueryProvider {
    private String cityName;

    @Override
    public Query createQuery() {
        EntityManager manager = getEntityManager();
        Query query = manager.createQuery("select c from Customer c where c.city = :city");
        query.setParameter("city", cityName);
        return query;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(cityName, "City name is required");
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
