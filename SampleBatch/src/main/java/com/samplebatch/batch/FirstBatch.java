package com.samplebatch.batch;

import com.samplebatch.entity.AfterEntity;
import com.samplebatch.entity.BeforeEntity;
import com.samplebatch.repository.AfterRepository;
import com.samplebatch.repository.BeforeRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
public class FirstBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final BeforeRepository beforeRepository;
    private final AfterRepository afterRepository;

    public FirstBatch(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, BeforeRepository beforeRepository, AfterRepository afterRepository) {

        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.beforeRepository = beforeRepository;
        this.afterRepository = afterRepository;
    }

    /**
     * Job 정의
     * 리턴은 Spring Batch의 단위인 Job
     *  start: 작업에서 처음 시작할 스텝
     *  Job밑에 n개의 스텝이 정의되어 있음
     *
     */
    @Bean
    public Job firstJob() {

        System.out.println("first job");

        return new JobBuilder("firstJob", jobRepository)
                .start(firstStep())
                .build();
    }

    /**
     * chunk: 대량의 데이터를 끊어서 읽을 최소 단위(총 데이터 n개를 chunk갯수만큼 끊어서 읽음)
     * platformTransactionManager: 청크단위로 작업을 진행하다가 실패했을 때, 다시 처리하도록 롤백 등 스프링 배치가 처리한다.
     * 단위가 작다면: I/O 처리가 많아짐
     * 단위가 크다면: 메모리 적재, CPU 자원 비용이 너무 많이 든다.
     * .reader(), .processor(), .writer()를 메소드로 작성한다.
     */
    @Bean
    public Step firstStep() {

        return new StepBuilder("firstStep", jobRepository)
                .<BeforeEntity, AfterEntity> chunk(10, platformTransactionManager)
                .reader(beforeReader())
                .processor(middleProcessor())
                .writer(afterWriter())
                .build();
    }

    /**
     * Reader에는 다양한 인터페이스와 구현체가 존재한다.
     * JPA, JDBC, item데이터를 읽는다 등의 예시가 있음.
     * methodName("findAll"): 데이터를 전부 읽음
     * pageSize: 페이징처리하여 10개씩 끊어서 읽겠다.
     * sorts: findAll을 페이지 단위로 읽어들일 때, sort를 진행해서 데이터 순서를 맞도록 한다.
     */
    @Bean
    public RepositoryItemReader<BeforeEntity> beforeReader() {

        return new RepositoryItemReaderBuilder<BeforeEntity>()
            .name("beforeReader")
            .pageSize(10)
            .methodName("findAll")
            .repository(beforeRepository)
            .sorts(Map.of("id", Sort.Direction.ASC))
            .build();
        }

    /**
     * Process: 읽어온 데이터를 처리하는 Process (큰 작업을 수행하지 않을 경우 생략 가능)
     * item이라는 매개변수로 보통 받게 된다.
     * write에서 쓸 수 있도록 세팅이 되었다.
     */
    @Bean
    public ItemProcessor<BeforeEntity, AfterEntity> middleProcessor() {

        return new ItemProcessor<BeforeEntity, AfterEntity>() {

            @Override
            public AfterEntity process(BeforeEntity item) throws Exception {

                AfterEntity afterEntity = new AfterEntity();
                afterEntity.setUsername(item.getUsername());

                return afterEntity;
            }
        };
    }

    /**
     * writer
     * AfterEntity에 처리한 결과를 Repository를 이용해서 save
     * writer도 마찬가지로, JPA,JDBC등 여러가지로 데이터베이스에 저장가능
     */
    @Bean
    public RepositoryItemWriter<AfterEntity> afterWriter() {

        return new RepositoryItemWriterBuilder<AfterEntity>()
                .repository(afterRepository)
                .methodName("save")
                .build();
    }
}
