package io.spring.batch.hello_world;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.support.DatabaseType;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.autoconfigure.batch.JobLauncherApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.util.StringUtils;

@EnableConfigurationProperties(BatchProperties.class)
@Configuration
public class BatchConfig extends DefaultBatchConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "spring.batch.job", name = "enabled", havingValue = "true", matchIfMissing = true)
    public JobLauncherApplicationRunner jobLauncherApplicationRunner(JobLauncher jobLauncher, JobExplorer jobExplorer,
                                                                     JobRepository jobRepository, BatchProperties properties) {
        JobLauncherApplicationRunner runner = new JobLauncherApplicationRunner(jobLauncher, jobExplorer, jobRepository);
        String jobNames = properties.getJob().getName();
        if (StringUtils.hasText(jobNames)) {
            String[] split = jobNames.split(",");
            for(String s: jobNames.split(",")) {
                runner.setJobName(s);
            }
            System.out.println("실행될 잡: " + jobNames);
        }
        return runner;
    }

//    @Primary
//    @Bean("batchDb")
//    @ConfigurationProperties(prefix = "spring.datasource.batch")
//    public DataSource dataSource(){
//        HikariDataSource build = DataSourceBuilder.create().type(HikariDataSource.class).build();
//        return build;
//    }

    @Primary
    @Bean("batchDb")
    public DataSource dataSource(){
        DataSourceBuilder builder = DataSourceBuilder.create();
        builder.type(HikariDataSource.class);
        builder.driverClassName("com.mysql.cj.jdbc.Driver");
        builder.username("root");
        builder.password("1234");
        builder.url("jdbc:mysql://localhost:3306/server?serverTimezone=Asia/Seoul&useSSL=false");
        return builder.build();
    }


    @Override
    protected DataSource getDataSource() {
        return dataSource();
    }

    @Primary
    @Bean("transactionManager")
    protected PlatformTransactionManager getTransactionManager() {
        return new DataSourceTransactionManager(dataSource());
//        return new JpaTransactionManager();
    }


//    @Override
//    public JobExplorer jobExplorer() throws BatchConfigurationException {
//        return super.jobExplorer();
//    }

    @SneakyThrows
    @Override
    public JobRepository jobRepository() {
        JobRepositoryFactoryBean factoryBean = new JobRepositoryFactoryBean();
        factoryBean.setDatabaseType(DatabaseType.MYSQL.getProductName());
//        factoryBean.setTablePrefix("B_");
        factoryBean.setIsolationLevelForCreateEnum(Isolation.REPEATABLE_READ);
        factoryBean.setDataSource(dataSource());
        factoryBean.setTransactionManager(getTransactionManager());
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    @Bean
    public ExecutionContextSerializer jacksonSerializer() {
        return new Jackson2ExecutionContextStringSerializer();
    }
}