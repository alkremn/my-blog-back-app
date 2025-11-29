package com.kremnev.blog.config;

import com.kremnev.blog.configuration.RestConfiguration;
import com.kremnev.blog.configuration.WebConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Test configuration for Spring TestContext Framework.
 * This configuration enables context caching for better test performance.
 * Uses H2 in-memory database for testing.
 */
@Configuration
@Import({
    RestConfiguration.class,
    WebConfiguration.class
})
@ComponentScan(basePackages = "com.kremnev.blog")
@PropertySource("classpath:application-test.properties")
public class TestConfiguration {

    /**
     * H2 in-memory database configured for PostgreSQL compatibility mode.
     */
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    @Bean
    @DependsOn("databaseInitializer")
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * Initialize H2 database schema for integration tests.
     * This bean runs after the DataSource is created to set up test tables.
     */
    @Bean
    public String databaseInitializer(DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.setContinueOnError(false);

        DatabasePopulatorUtils.execute(populator, dataSource);

        return "Database initialized";
    }
}
