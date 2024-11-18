package io.spring.batch.hello_world.chapter06_bank;

import io.spring.batch.hello_world.chapter04.job.JobLoggerListener;
import io.spring.batch.hello_world.chapter06_bank.domain.AccountSummary;
import io.spring.batch.hello_world.chapter06_bank.domain.Transaction;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BankJob {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() {
        return new JobBuilder("BankJob", jobRepository)
//                .preventRestart()
//				.start(importTransactionFileStep())
//				.on("STOPPED").stopAndRestart(importTransactionFileStep())
//				.from(importTransactionFileStep()).on("*").to(applyTransactionsStep())
//				.from(applyTransactionsStep()).next(generateAccountSummaryStep())
//				.end()
                .start(importTransactionFileStep())
                .next(applyTransactionsStep())
                .next(generateAccountSummaryStep())
//                .incrementer(new RunIdIncrementer())
                .listener(JobListenerFactoryBean.getListener(
                        new JobLoggerListener()))
                .build();
    }

    /////
    /////
    //1. 거래내역 csv 파일 읽기
    //2. Transaction 테이블에 쓰기
    @Bean
    public Step importTransactionFileStep() {
        return new StepBuilder("importTransactionFileStep", jobRepository)
                .<Transaction, Transaction>chunk(100, transactionManager)
                .reader(transactionReader())
                .writer(transactionWriter(null))
//                .allowStartIfComplete(true)
                .listener(transactionReader()) //?
                .build();
    }

    @Bean
    @StepScope
    public TransactionReader transactionReader() {
        return new TransactionReader(fileItemReader(null));
    }

    @Bean
    @StepScope
    public FlatFileItemReader<FieldSet> fileItemReader(
            @Value("#{jobParameters['transactionFile']}") ClassPathResource inputFile) {

        return new FlatFileItemReaderBuilder<FieldSet>()
                .name("fileItemReader")
                .resource(inputFile)
                .lineTokenizer(new DelimitedLineTokenizer())
                .fieldSetMapper(new PassThroughFieldSetMapper())
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Transaction> transactionWriter(@Qualifier("serverDatasource") DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO TRANSACTION " +
                        "(ACCOUNT_SUMMARY_ID, TIMESTAMP, AMOUNT) " +
                        "VALUES ((SELECT ID FROM ACCOUNT_SUMMARY " +
                            "WHERE ACCOUNT_NUMBER = :accountNumber), :timestamp, :amount)")
                .dataSource(dataSource)
                .build();
    }

    ///////////
    //////////
    //1. AccountSummary 테이블에서 '거래 내역'이 있는 계좌들만 수집
    //2. 해당 계좌의 거래 최종 금액 계산하기
    //3. AccountSummary 테이블에 반영
    @Bean
    public TransactionDao transactionDao(@Qualifier("serverDatasource") DataSource dataSource) {
        return new TransactionDaoSupport(dataSource);
    }


    @Bean
    public Step applyTransactionsStep() {
        return new StepBuilder("applyTransactionsStep", jobRepository)
                .<AccountSummary, AccountSummary>chunk(100, transactionManager)
                .reader(accountSummaryReader(null))
                .processor(transactionApplierProcessor())
                .writer(accountSummaryWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<AccountSummary> accountSummaryReader(@Qualifier("serverDatasource") DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<AccountSummary>()
                .name("accountSummaryReader")
                .dataSource(dataSource)
                .sql("SELECT ACCOUNT_NUMBER, CURRENT_BALANCE " +
                        "FROM ACCOUNT_SUMMARY A " +
                        "WHERE A.ID IN (" +
                        "	SELECT DISTINCT T.ACCOUNT_SUMMARY_ID " +
                        "	FROM TRANSACTION T) " +
                        "ORDER BY A.ACCOUNT_NUMBER")
                .rowMapper((resultSet, rowNumber) -> {
                    AccountSummary summary = new AccountSummary();
                    summary.setAccountNumber(resultSet.getString("account_number"));
                    summary.setCurrentBalance(resultSet.getDouble("current_balance"));
                    return summary;
                }).build();
    }

    @Bean
    public TransactionApplierProcessor transactionApplierProcessor() {
        return new TransactionApplierProcessor(transactionDao(null));
    }

    @Bean
    public JdbcBatchItemWriter<AccountSummary> accountSummaryWriter(@Qualifier("serverDatasource") DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<AccountSummary>()
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(
                        new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("UPDATE ACCOUNT_SUMMARY " +
                        "SET CURRENT_BALANCE = :currentBalance " +
                        "WHERE ACCOUNT_NUMBER = :accountNumber")
                .build();
    }


    /////
    /////
    //1. AccountSummary 테이블에서 '거래 내역'이 있는 계좌들만 수집
    //2. 최종 결과를 csv 파일로 생성
    @Bean
    public Step generateAccountSummaryStep() {
        return new StepBuilder("generateAccountSummaryStep", jobRepository)
                .<AccountSummary, AccountSummary>chunk(100, transactionManager)
                .reader(accountSummaryReader(null))
                .writer(accountSummaryFileWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<AccountSummary> accountSummaryFileWriter(
            @Value("#{jobParameters['summaryFile']}") FileSystemResource summaryFile) {

        DelimitedLineAggregator<AccountSummary> lineAggregator = new DelimitedLineAggregator<>();
        BeanWrapperFieldExtractor<AccountSummary> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] {"accountNumber", "currentBalance"});
        fieldExtractor.afterPropertiesSet();
        lineAggregator.setFieldExtractor(fieldExtractor);

        return new FlatFileItemWriterBuilder<AccountSummary>()
                .name("accountSummaryFileWriter")
                .resource(summaryFile)
                .lineAggregator(lineAggregator)
                .build();
    }



}
