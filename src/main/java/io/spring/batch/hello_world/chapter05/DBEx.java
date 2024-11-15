package io.spring.batch.hello_world.chapter05;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

public class DBEx {

    @Qualifier("serverJdbcTemplate")
    @Autowired
    private JdbcTemplate serverJdbcTemplate;

    public void abc()  {
        serverJdbcTemplate.update("DROP TABLE IF EXISTS ABC");
        serverJdbcTemplate.update("CREATE TABLE ABC(name VARCHAR(20))");
    }
}
