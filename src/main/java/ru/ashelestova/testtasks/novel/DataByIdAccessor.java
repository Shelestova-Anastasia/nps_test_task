package ru.ashelestova.testtasks.novel;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
    private final Map<String, Long> dataOffsetById;

    DataByIdAccessor(File file) throws IOException {
        sourceFile = file;
        randomAccessFile = new RandomAccessFile(sourceFile, "r");
        dataOffsetById = new HashMap<>();
        loadData();
    }

    DataByIdAccessor(String filePath) throws IOException {
        this(new File(filePath));
    }


    /**
     *
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
            return randomAccessFile.readLine();
        } catch (IOException e) {
            log.warn("Unexpected error. Could not seek offset = {} in file {}", offset, sourceFile.getAbsolutePath(), e);
            return null;
        }
    }

    private void loadData() throws IOException {
        String currentId;

        while ((currentId = randomAccessFile.readLine()) != null) {
            dataOffsetById.put(currentId, randomAccessFile.getFilePointer());
        }
    }

    public void close() throws IOException {
        randomAccessFile.close();
    }
}
