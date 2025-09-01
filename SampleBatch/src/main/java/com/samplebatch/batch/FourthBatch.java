package com.samplebatch.batch;

import com.samplebatch.entity.BeforeEntity;
import com.samplebatch.repository.BeforeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.util.Map;

/**
 *  테이블 -> 엑셀로 배치처리
 *
 *  엑셀 -> 테이블은 중간에 종료되더라도 중단점부터 실행하면 효율적이다.
 *  테이블 -> 엑셀이 실패하면 파일을 새로 만들어야되기 때문에 처음부터 배치를 처리해야함
 */
@RequiredArgsConstructor
@Configuration
public class FourthBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final BeforeRepository beforeRepository;

    @Bean
    public Job fourthBatchJob() {

        System.out.println("fourth job");

        return new JobBuilder("fourthJob", jobRepository)
                .start(fifthStep())
                .build();
    }

    /**
     * <BeforeEntity, BeforeEntity> chunk: BeforeEntity타입으로 읽어와서, BeforeEntity타입으로 넘겨준다.
     */
    @Bean
    public Step fifthStep() {

        System.out.println("fourth step");

        return new StepBuilder("fourthStep", jobRepository)
                .<BeforeEntity, BeforeEntity> chunk(10, platformTransactionManager)
                .reader(fourthBeforeReader())
                .processor(fourthProcessor())
                .writer(excelWriter())
                .build();
    }

    /**
     * 데이터를 엑셀에서 읽어오는데, 만약 배치가 실패 할 경우
     * 다시 첫 번째부터 시작해야 함.
     * 만약 엑셀파일을 만드는것이 아닌, 기존의 엑셀파일에 업데이트하는 형식이라면 true 값을 세팅(아에없에서 true)
     */
    @Bean
    public RepositoryItemReader<BeforeEntity> fourthBeforeReader() {

        RepositoryItemReader<BeforeEntity> reader = new RepositoryItemReaderBuilder<BeforeEntity>()
                .name("beforeReader")
                .pageSize(10)
                .methodName("findAll")
                .repository(beforeRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();

        // 전체 데이터 셋에서 어디까지 수행 했는지의 값을 저장하지 않음
        reader.setSaveState(false); // true or false

        return reader;
    }

    @Bean
    public ItemProcessor<BeforeEntity, BeforeEntity> fourthProcessor() {

        return item -> item;
    }

    @Bean
    public ItemStreamWriter<BeforeEntity> excelWriter() {

        try {
            return new ExcelRowWriter("C:\\Users\\NHN\\Documents\\GitHub\\SpringBatch-Boilerplate\\result.xlsx");

            //리눅스나 맥은 /User/형태로
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
