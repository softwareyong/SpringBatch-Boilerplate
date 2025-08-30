package com.samplebatch.batch;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class ExcelRowReader implements ItemStreamReader<Row> {

    private final String filePath;
    private FileInputStream fileInputStream;
    private Workbook workbook;
    private Iterator<Row> rowCursor;
    private int currentRowNumber;
    private final String CURRENT_ROW_KEY = "current.row.number";

    public ExcelRowReader(String filePath) throws IOException {

        this.filePath = filePath;
        this.currentRowNumber = 0;
    }

    /**
     * open: ItemStreamReader가 실행될 때, 맨 처음에 단 한번만 실행되는 메소드
     * 엑셀파일을 열거나, 특정한 초기화
     */
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {

        try {
            fileInputStream = new FileInputStream(filePath);
            workbook = WorkbookFactory.create(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0);
            this.rowCursor = sheet.iterator();

            // 동일 배치 파라미터에 대해 특정 키 값 "current.row.number"의 값이 존재한다면 초기화
            // 스프링 배치의 메타데이터에 어제나 현재까지 된 행(ex: 50행)까지 이후부터 다시 배치돌게 처리
            if (executionContext.containsKey(CURRENT_ROW_KEY)) {
                currentRowNumber = executionContext.getInt(CURRENT_ROW_KEY);
            }

            // 위의 값을 가져와 이미 실행한 부분은 건너 뜀
            for (int i = 0; i < currentRowNumber && rowCursor.hasNext(); i++) {
                rowCursor.next();
            }

        } catch (IOException e) {
            throw new ItemStreamException(e);
        }
    }

    /**
     * read: 배치처리가 청크단위로 진행될 때, 매번 불려지는 메소드
     * 데이터의 각각의 행을 읽는 작업 세팅
     */
    @Override
    public Row read() {

        if (rowCursor != null && rowCursor.hasNext()) {
            currentRowNumber++;
            return rowCursor.next();
        } else {
            return null;
        }
    }

    /**
     * update: read처럼 매번 불려지는데, 매번처리되고 나서 특정한 변수값을 업데이트
     */
    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt(CURRENT_ROW_KEY, currentRowNumber);
    }

    /**
     * close: 가장 마지막에 한 번만 실행, 변수값 다시 초기화
     * 엑셀과, fileInputStream을 close
     */
    @Override
    public void close() throws ItemStreamException {

        try {
            if (workbook != null) {
                workbook.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (IOException e) {
            throw new ItemStreamException(e);
        }
    }
}
