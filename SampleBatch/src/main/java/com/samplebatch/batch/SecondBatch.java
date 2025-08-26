package com.samplebatch.batch;

import com.samplebatch.entity.WinEntity;
import com.samplebatch.repository.WinRepository;
import lombok.RequiredArgsConstructor;
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

import java.util.Collections;
import java.util.Map;

/**
 * 승리횟수(win)가 10이 넘는경우, reward(보상)가 true로 바뀌는 배치
 */
@Configuration
@RequiredArgsConstructor
public class SecondBatch {

    /**
     * jobRepository: job을 관리
     * PlatformTransactionManager: 트랜잭션 관리
     * winRepository: winRepository 접근
     */
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final WinRepository winRepository;

    @Bean
    public Job secondJob() {

        return new JobBuilder("secondJob", jobRepository)
                .start(secondStep())
                .build();
    }

    /**
     * 실제 배치 처리는 Job 아래의 하나의 Step에서 수행
     * Step 읽기 -> 처리 -> 쓰기 과정을 구성
     * chunk: 데이터를 읽고 쓸 때, 한 번에 처리할 단위, 벤치마킹을 잘해서 구성
     */
    @Bean
    public Step secondStep() {

        return new StepBuilder("secondStep", jobRepository)
                .<WinEntity, WinEntity> chunk(10, platformTransactionManager)
                .reader(winReader())
                .processor(trueProcessor())
                .writer(winWriter())
                .build();
    }

    /**
     * 메소드 작명 이유(winReader): Repository를 기반으로 win을 읽을거임
     * arguments: Reader가 호출할 리포지토리 메소드에 전달할 인자(파라미터) 지정
     */
    @Bean
    public RepositoryItemReader<WinEntity> winReader() {
        return new RepositoryItemReaderBuilder<WinEntity>()
                .name("winReader")
                .pageSize(10)
                .methodName("findByWinGreaterThanEqual")
                .arguments(Collections.singletonList(10L))
                .repository(winRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    /**
     * Reader -> Processor -> Writer 의 흐름으로 움직임
     * Processor: 읽어온 데이터를 가공/변환해서 다음 단계로 넘김
     */
    @Bean
    public ItemProcessor<WinEntity, WinEntity> trueProcessor() {

        return item -> {
            item.setReward(true);
            return item;
        };
    }

    /**
     * Writer
     * 처리한 메소드를 save
     */
    @Bean
    public RepositoryItemWriter<WinEntity> winWriter() {

        return new RepositoryItemWriterBuilder<WinEntity>()
                .repository(winRepository)
                .methodName("save")
                .build();
    }

}
