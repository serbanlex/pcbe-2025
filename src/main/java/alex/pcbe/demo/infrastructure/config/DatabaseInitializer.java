package alex.pcbe.demo.infrastructure.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database initializer that creates the database if it doesn't exist.
 * NOTE: In production, database provisioning should be handled by infrastructure/ops team.
 * This class implements BeanFactoryPostProcessor to ensure it runs BEFORE the DataSource is created.
 */
@Component
public class DatabaseInitializer implements BeanFactoryPostProcessor, EnvironmentAware {

    private Environment environment;
    private static final String DB_NAME = "guestbook_db";
    private static final String POSTGRES_URL = "jdbc:postgresql://localhost:5432/postgres";

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String username = environment.getProperty("spring.datasource.username");
        String password = environment.getProperty("spring.datasource.password");
        try {
            createDatabaseIfNotExists(username, password);
        } catch (SQLException e) {
            throw new BeansException("Failed to create database", e) {};
        }
    }

    private void createDatabaseIfNotExists(String username, String password) throws SQLException {
        try (Connection conn = DriverManager.getConnection(POSTGRES_URL, username, password);
             Statement stmt = conn.createStatement()) {

            // Check if database exists
            var rs = stmt.executeQuery(
                "SELECT 1 FROM pg_database WHERE datname = '" + DB_NAME + "'"
            );

            if (!rs.next()) {
                // Database doesn't exist, create it
                stmt.executeUpdate("CREATE DATABASE " + DB_NAME);
                System.out.println("Database '" + DB_NAME + "' created successfully.");
            } else {
                System.out.println("Database '" + DB_NAME + "' already exists.");
            }
        }
    }
}

