package com.samplebatch.batch;

import com.samplebatch.repository.WinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

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
                .start(스텝들어갈자리)
                .build();
    }

    

}
