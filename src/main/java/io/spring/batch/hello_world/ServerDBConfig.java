package io.spring.batch.hello_world;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ServerDBConfig {

    @Qualifier("serverDatasource")
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.server1")
    public DataSource serverDatasource(){
        return DataSourceBuilder.create().build();
    }

    @Qualifier("serverJdbcTemplate")
    @Bean
    public JdbcTemplate serverJdbcTemplate(@Qualifier("serverDatasource") DataSource serverDatasource){
        return new JdbcTemplate(serverDatasource);
    }

//    @Bean("serverDb")
//    public DataSource serverDb() {
//        HikariDataSource dataSource = new HikariDataSource();
//        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
//        dataSource.setUsername("root");
//        dataSource.setPassword("1234");
//        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/server?serverTimezone=Asia/Seoul&useSSL=false");
//        return dataSource;
//    }

//    @Bean("workTr")
//    protected PlatformTransactionManager workTransactionManager() {
//        return new DataSourceTransactionManager(serverDatasource());
//    }
}
