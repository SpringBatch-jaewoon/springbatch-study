package io.spring.batch.hello_world;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ServerDBConfig {
    @Bean("serverDb")
    public DataSource serverDb() {
        DataSourceBuilder builder = DataSourceBuilder.create();
        builder.type(HikariDataSource.class);
        builder.driverClassName("com.mysql.cj.jdbc.Driver");
        builder.username("root");
        builder.password("1234");
        builder.url("jdbc:mysql://localhost:3306/serverDb?serverTimezone=Asia/Seoul&useSSL=false");
        return builder.build();
    }

    @Bean("workTr")
    protected PlatformTransactionManager workTransactionManager() {
        return new DataSourceTransactionManager(serverDb());
    }
}
