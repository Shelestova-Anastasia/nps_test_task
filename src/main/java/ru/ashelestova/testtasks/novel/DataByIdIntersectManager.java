package ru.ashelestova.testtasks.novel;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * DataByIdIntersectManager class
 * Input: firstSourcePath, secondSourcePath, resultPath
 *
 * Any SourcePath should point to source file. File format: Odd line is ID, Even line is data by ID (in previous line)
 * resultPath - point the path for result. File will contain data from both source files. File format: for each id file will have 3 lines: ID line, First file data by ID, Second file data by Id
 */
@Slf4j
class DataByIdIntersectManager {
    public static void main(String[] args) {
        if (args.length < 3) {
            log.error("Expected arguments: firstSourcePath, secondSourcePath, resultPath");
            return;
        }

        String firstSourcePath = args[0];
        String secondSourcePath = args[1];
        String resultPath = args[2];

        DataByIdIntersectManager dataByIdIntersectManager = new DataByIdIntersectManager();
        dataByIdIntersectManager.intersect(firstSourcePath, secondSourcePath, resultPath);
    }

    /**
     * Finds common ids in source files. Result file will contain data from both files by id.
     * @param firstSourcePath first source file path. File format: Odd line is ID, Even line is data by ID (in previous line)
     * @param secondSourcePath second source file path. File format: Odd line is ID, Even line is data by ID (in previous line)
     * @param resultPath result file path. File will contain data from both files. File format: for each id file will have 3 lines: ID line, First file data by ID, Second file data by Id
     */
    void intersect(String firstSourcePath, String secondSourcePath, String resultPath) {
        log.info("Start intersect files: {}, {}. Result will be written to {}", firstSourcePath, secondSourcePath, resultPath);

        File firstSourceFile = new File(firstSourcePath);
        File secondSourceFile = new File(secondSourcePath);

        if (firstSourceFile.length() == 0 || secondSourceFile.length() == 0) {
            log.info("Empty file in input. First source size = {}, second source size = {}", firstSourceFile.length(), secondSourceFile.length());
            createEmptyFile(resultPath);
            return;
        }

        boolean sourcesSwapped = false;
        if (firstSourceFile.length() > secondSourceFile.length()) {
            sourcesSwapped = true;
            File tmp = firstSourceFile;
            firstSourceFile = secondSourceFile;
            secondSourceFile = tmp;
        }

        long intersectIdsNumber = 0;

        try (DataByIdAccessor firstSourceAccessor = new DataByIdAccessor(firstSourceFile)) {
            try (BufferedReader secondFileReader = new BufferedReader(new FileReader(secondSourceFile))) {
                try (BufferedWriter resultWriter = new BufferedWriter(new FileWriter(resultPath))) {
                    String currentId;

                    while ((currentId = secondFileReader.readLine()) != null) {
                        String parsedId = Utils.parseId(currentId);
                        String secondSourceData = secondFileReader.readLine();

                        String firstSourceData = firstSourceAccessor.getDataById(parsedId);
                        if (firstSourceData != null) {
                            resultWriter.write(currentId);
                            resultWriter.newLine();

                            if (sourcesSwapped) {
                                writeData(resultWriter, secondSourceData, firstSourceData);
                            } else {
                                writeData(resultWriter, firstSourceData, secondSourceData);
                            }

                            intersectIdsNumber++;
                        }
                    }
                }
            }
            log.info("Finished intersect files {}, {}. Result file {}. Intersect ids number = {}", firstSourcePath, secondSourcePath, resultPath, intersectIdsNumber);
        } catch (IOException e) {
            log.error("Intersect files error", e);
        }

    }

    private void writeData(BufferedWriter resultWriter, String firstSourceData, String secondSourceData) throws IOException {
        resultWriter.write(firstSourceData);
        resultWriter.newLine();
        resultWriter.write(secondSourceData);
        resultWriter.newLine();
    }

    private void createEmptyFile(String resultPath) {
        File resultFile = new File(resultPath);
        if (!resultFile.exists()) {
            try {
                resultFile.createNewFile();
            } catch (IOException e) {
                log.error("Could not create empty result file", e);
            }
        }
    }
}
