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
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;

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

        System.out.println("fourth job");

        return new JobBuilder("fourthJob", jobRepository)
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
                .writer(thirdhAfterWriter())
                .build();
    }

    /**
     * Read: 엑셀 시트에서 읽어오는 Reader
     */
    @Bean
    public ItemStreamReader<Row> excelReader() {

        try {
            return new ExcelRowReader("C:\\Users\\kim\\Desktop\\yummi.xlsx");
            //리눅스나 맥은 /User/형태로
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
