package com.example.springbatchSample.batchConfig;

import com.example.springbatchSample.model.Student;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class BatchConfiguration {

    private StepBuilderFactory stepBuilderFactory;

    private JobBuilderFactory jobBuilderFactory;

    private DataSource dataSource;

    @Bean
    public JdbcCursorItemReader<Student> reader(){
        JdbcCursorItemReader<Student> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("select id, firstName, lastName, email from studentbatch");
        reader.setRowMapper(new RowMapper<Student>() {

            @Override
            public Student mapRow(ResultSet rs, int rowNum) throws SQLException {
                Student c = new Student();
                c.setId(rs.getInt("id"));
                c.setFirstName(rs.getString("firstName"));
                c.setLastName(rs.getString("lastName"));
                c.setEmail(rs.getString("email"));
                return c;
            }
        });
        return reader;
    }
    @Bean
    public FlatFileItemWriter<Student> writer(){
        FlatFileItemWriter<Student> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource("src/main/resources/output.csv"));
        DelimitedLineAggregator<Student> aggregator = new DelimitedLineAggregator<>();
        BeanWrapperFieldExtractor<Student> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] {"id","firstName","lastName","email"});
        aggregator.setFieldExtractor(fieldExtractor);
        writer.setLineAggregator(aggregator);
        return writer;
    }

    @Bean
    public Step executeStep(){
     return stepBuilderFactory.get("executeStep").<Student, Student>chunk(10).reader(reader()).writer(writer()).build();
    }
    @Bean
    public Job processJob(){
    return jobBuilderFactory.get("processJob").incrementer(new RunIdIncrementer()).flow(executeStep()).end().build();
    }
}
