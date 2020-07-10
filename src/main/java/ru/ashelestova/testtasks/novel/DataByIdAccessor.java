package ru.ashelestova.testtasks.novel;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * DataByIdAccessor class
 * input - source file. File format: Odd line is ID, Even line is data by ID (in previous line)
 * DataByIdAccessor can extract data by id.
 */
@Slf4j
class DataByIdAccessor implements AutoCloseable {
    private final File sourceFile;
    private final RandomAccessFile randomAccessFile;
    private final FileInputStream fileInputStream;
    private final long endLineBytesNum;

    private final Map<String, Long> dataOffsetById;

    DataByIdAccessor(String filePath) throws IOException {
        this(new File(filePath));
    }

    DataByIdAccessor(File file) throws IOException {
        sourceFile = file;
        randomAccessFile = new RandomAccessFile(sourceFile, "r");
        fileInputStream = new FileInputStream(randomAccessFile.getFD());
        dataOffsetById = new HashMap<>();

        endLineBytesNum = calcLineEndLength();
        loadData();
    }

    /**
     * @param id to search data
     * @return data by id
     */
    String getDataById(String id) {
        Long offset = dataOffsetById.get(id);

        if (offset == null) {
            return null;
        }

        try {
            randomAccessFile.seek(offset);
            BufferedReader bufferedReader = createBufferedReader();
            return bufferedReader.readLine();
        } catch (IOException e) {
            log.warn("Unexpected error. Could not read line by offset = {} in file {}", offset, sourceFile.getAbsolutePath(), e);
            return null;
        }
    }

    private void loadData() throws IOException {
        BufferedReader reader = createBufferedReader();
        String currentId;
        long currentOffset = 0;

        while ((currentId = reader.readLine()) != null) {
            currentOffset += currentId.getBytes().length + endLineBytesNum;
            dataOffsetById.put(Utils.parseId(currentId), currentOffset);

            String data = reader.readLine();
            if (data != null) {
                currentOffset += data.getBytes().length + endLineBytesNum;
            }
        }
    }

    private BufferedReader createBufferedReader() {
        return new BufferedReader(new InputStreamReader(fileInputStream));
    }

    int size() {
        return dataOffsetById.size();
    }

    public void close() {
        try {
            fileInputStream.close();
        } catch (Exception e) {
            log.error("Could not close fileInputStream for file {}", sourceFile.getAbsolutePath(), e);
        } finally {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                log.error("Could not close randomAccessFile for file {}", sourceFile.getAbsolutePath(), e);
            }
        }
    }

    private long calcLineEndLength() throws IOException {
        String firstLine = randomAccessFile.readLine();
        long endLineBytes = randomAccessFile.getFilePointer() - firstLine.getBytes().length;
        randomAccessFile.seek(0);
        return endLineBytes;
    }
}
