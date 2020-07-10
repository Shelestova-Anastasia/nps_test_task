package ru.ashelestova.testtasks.novel;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataByIdIntersectManagerTest {
    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();
    private final DataByIdIntersectManager dataByIdIntersectManager = new DataByIdIntersectManager();

    private final String firstSourcePath = DataByIdAccessor.class.getResource("/source1.txt").getPath();
    private final String secondSourcePath = DataByIdAccessor.class.getResource("/source2.txt").getPath();
    private final String expectedResultPath = DataByIdAccessor.class.getResource("/result.txt").getPath();
    private final String expectedResultPath2 = DataByIdAccessor.class.getResource("/result2.txt").getPath();

    @Test
    public void intersectFirstSecond() throws IOException {
        File resultFile = testFolder.newFile("result.txt");

        dataByIdIntersectManager.intersect(firstSourcePath, secondSourcePath, resultFile.getPath());

        String result = getFileAsString(resultFile);
        String expectedResult = getFileAsString(new File(expectedResultPath));
        assertEquals(expectedResult, result);
    }

    @Test
    public void intersectSecondFirst() throws IOException {
        File resultFile = testFolder.newFile("result.txt");

        dataByIdIntersectManager.intersect(secondSourcePath, firstSourcePath, resultFile.getPath());

        String result = getFileAsString(resultFile);
        String expectedResult = getFileAsString(new File(expectedResultPath2));
        assertEquals(expectedResult, result);
    }

    @Test
    public void emptySourceFile() throws IOException {
        File emptyFile = testFolder.newFile("empty.txt");
        File resultFile = testFolder.newFile("result.txt");

        dataByIdIntersectManager.intersect(firstSourcePath, emptyFile.getPath(), resultFile.getPath());
        String result = getFileAsString(resultFile);
        assertEquals("", result);

        dataByIdIntersectManager.intersect(emptyFile.getPath(), secondSourcePath, resultFile.getPath());
        result = getFileAsString(resultFile);
        assertEquals("", result);
    }

    @Test
    public void notExistingResultFile() throws IOException {
        File emptyFile = testFolder.newFile("empty.txt");

        String wantedResultPath = expectedResultPath.replace("result.txt", "test_result.txt");
        dataByIdIntersectManager.intersect(firstSourcePath, emptyFile.getPath(), wantedResultPath);

        File resultFile = new File(wantedResultPath);
        assertTrue(resultFile.exists());

        String result = getFileAsString(resultFile);
        assertEquals("", result);

        resultFile.delete();
    }

    @Test
    public void notExistingSourceFile() throws IOException {
        String notExistingSourcePath = firstSourcePath.replace("source1", "no_source1");

        File resultFile = testFolder.newFile("result.txt");

        dataByIdIntersectManager.intersect(notExistingSourcePath, secondSourcePath, resultFile.getPath());
        String result = getFileAsString(resultFile);
        assertEquals("", result);
    }

    private String getFileAsString(File resultFile) throws IOException {
        return new String(Files.readAllBytes(Paths.get(resultFile.getPath())));
    }

}
