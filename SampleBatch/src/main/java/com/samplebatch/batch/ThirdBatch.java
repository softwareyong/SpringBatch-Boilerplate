package com.samplebatch.batch;

import com.samplebatch.entity.AfterEntity;
import com.samplebatch.repository.AfterRepository;
import com.samplebatch.repository.WinRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;

/**
 * 중요 포인트1
 * ItemStreamReader에서 엑셀 파일은 언제 열리고 닫힐까?
 * chunk 단위로 수행시 매번 열릴지?
 * 아니면 열린 상태로 존재할지?
 *
 * 정답
 * open() 메소드 때, 한 번 열리고 read 때 읽기
 * 파일이 열리고 닫히는 것은 컴퓨터 자원에 많은 부하
 *
 * 중요 포인트2
 * 엑셀 파일에서 데이터를 읽다가 프로그램 멈춘다면, 다시 실행 할 때, ExecutionContext에서 관리하도록 한다.
 * ExcelRowReader-update에서 관리중
 */

@RequiredArgsConstructor
@Configuration
public class ThirdBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final AfterRepository afterRepository;

    /**
     * JobBuilder: Job객체를 빌드하는 빌드하는 클래스, Builder패턴 방식으로 fourthJob이라는 Job 생성
     */
    @Bean
    public Job thirdJob() {

        System.out.println("third job");

        return new JobBuilder("thirdJob", jobRepository)
                .start(thirdStep())
                .build();
    }

    /**
     * Step: 실제 배치처리를 진행하는 부분
     * Row: 엑셀의 하나의 행을 읽음
     * chunk: (읽기 -> 처리 -> 쓰기) 작업의 단위
     */
    @Bean
    public Step thirdStep() {

        return new StepBuilder("thirdStep", jobRepository)
                .<Row, AfterEntity> chunk(10, platformTransactionManager)
                .reader(excelReader())
                .processor(thirdProcessor())
                .writer(thirdAfterWriter())
                .build();
    }

    /**
     * Read: 엑셀 시트에서 읽어오는 Reader
     */
    @Bean
    public ItemStreamReader<Row> excelReader() {

        try {
            return new ExcelRowReader("C:\\Users\\NHN\\Documents\\GitHub\\SpringBatch-Boilerplate\\yongwoo.xlsx");
            //리눅스나 맥은 /User/형태로
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * process: 읽어온 데이터를 처리하는 Process
     */
    @Bean
    public ItemProcessor<Row, AfterEntity> thirdProcessor() {

        return new ItemProcessor<Row, AfterEntity>() {

            @Override
            public AfterEntity process(Row item) {

                AfterEntity afterEntity = new AfterEntity();
                afterEntity.setUsername(item.getCell(0).getStringCellValue());

                return afterEntity;
            }
        };
    }

    /**
     * 단순 save, 이전과 동일
     */
    @Bean
    public RepositoryItemWriter<AfterEntity> thirdAfterWriter() {

        return new RepositoryItemWriterBuilder<AfterEntity>()
                .repository(afterRepository)
                .methodName("save")
                .build();
    }



}
