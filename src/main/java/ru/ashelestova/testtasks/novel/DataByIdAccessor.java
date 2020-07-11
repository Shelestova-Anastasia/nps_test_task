package ru.ashelestova.testtasks.novel;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

/**
 * DataByIdAccessor class
 * input - source file. File format: Odd line is ID (prepended with @ symbol - like @one), Even line is data by ID (in previous line)
 * DataByIdAccessor can extract data by id.
 */
@Slf4j
class DataByIdAccessor implements AutoCloseable {
    private final String filePath;
    private final RandomAccessFile randomAccessFile;

    //optimization based on BufferedReader code to read fast from random access file
    private static int defaultCharBufferSize = 8192;
    private static int defaultExpectedLineLength = 80;
    private Long actualFilePointer;
    private char[] charBuffer;
    private int nChars;
    private int nextChar;
    private int bufferSize;
    private long lastOffset;
    private boolean skipLF;

    private final Map<String, Long> dataOffsetById;

    DataByIdAccessor(String filePath) throws IOException {
        this.filePath = filePath;
        randomAccessFile = new RandomAccessFile(filePath, "r");
        dataOffsetById = new HashMap<>();

        this.bufferSize = defaultCharBufferSize;
        charBuffer = new char[bufferSize];

        loadData();
    }

    /**
     * @param id to search data (without first @)
     * @return data by id
     */
    String getDataById(String id) {
        Long offset = dataOffsetById.get(id);

        if (offset == null) {
            return null;
        }

        try {
            seek(offset);
            return readLine();
        } catch (IOException e) {
            log.warn("Unexpected error. Could not read line by offset = {} in file {}", offset, filePath, e);
            return null;
        }
    }

    int size() {
        return dataOffsetById.size();
    }

    private void loadData() throws IOException {
        String currentId;

        while ((currentId = readLine()) != null) {
            dataOffsetById.put(Utils.parseId(currentId), actualFilePointer);
            readLine();
        }
    }

    private void seek(long pos) throws IOException {
        actualFilePointer = null;
        resetPosition();
        randomAccessFile.seek(pos);
    }

    private void resetPosition() throws IOException {
        if (actualFilePointer != null) {
            randomAccessFile.seek(actualFilePointer);
            actualFilePointer = null;
        }
        nChars = 0;
        nextChar = 0;
    }

    private String readLine() throws IOException {
        StringBuilder s = null;
        int startChar;
        int separatorIndex = 0;

        boolean omitLF = skipLF;

        for (; ; ) {

            if (nextChar >= nChars) {
                fill();
            }
            if (nextChar >= nChars) { /* EOF */
                if (s != null && s.length() > 0) {
                    return s.toString();
                } else {
                    return null;
                }
            }
            boolean eol = false;
            char c = 0;
            int i;

            if (omitLF && (charBuffer[nextChar] == '\n')) {
                nextChar++;
            }
            skipLF = false;
            omitLF = false;

            for (i = nextChar; i < nChars; i++) {
                c = charBuffer[i];
                if ((c == '\n') || (c == '\r')) {
                    eol = true;
                    break;
                }
            }

            startChar = nextChar;
            nextChar = i;

            if (eol) {
                String str;
                if (s == null) {
                    str = new String(charBuffer, startChar, i - startChar);
                } else {
                    s.append(charBuffer, startChar, i - startChar);
                    str = s.toString();
                }
                nextChar++;
                if (c == '\r') {
                    skipLF = true;
                    if (nextChar >= nChars) {
                        fill();
                    }
                    if (charBuffer[nextChar] == '\n') {
                        separatorIndex = 1;
                    }
                }
                actualFilePointer = lastOffset + nextChar + separatorIndex;
                return str;
            }

            if (s == null) {
                s = new StringBuilder(defaultExpectedLineLength);
            }
            s.append(charBuffer, startChar, i - startChar);
        }
    }

    private void fill() throws IOException {
        lastOffset = randomAccessFile.getFilePointer();
        actualFilePointer = lastOffset;
        byte[] buffer = new byte[bufferSize];
        int n = randomAccessFile.read(buffer);
        if (n > 0) {
            nChars = n;
            nextChar = 0;
        }
        for (int i = 0; i < buffer.length; i++) {
            charBuffer[i] = (char) buffer[i];
        }
    }

    public void close() {
        try {
            randomAccessFile.close();
        } catch (IOException e) {
            log.error("Could not close randomAccessFile for file {}", filePath, e);
        }
    }
}
