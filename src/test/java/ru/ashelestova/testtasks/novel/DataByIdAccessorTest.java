package ru.ashelestova.testtasks.novel;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataByIdAccessorTest {
    private final String filePath = DataByIdAccessor.class.getResource("/source1.txt").getPath();

    @Test
    public void getDataById() throws Exception {
        try (DataByIdAccessor dataByIdAccessor = new DataByIdAccessor(filePath)) {
            assertEquals(4, dataByIdAccessor.size());
            assertEquals("bar1", dataByIdAccessor.getDataById("four"));
            assertEquals("baz1", dataByIdAccessor.getDataById("six"));
            assertEquals("foo1", dataByIdAccessor.getDataById("one"));
            Assert.assertNull(dataByIdAccessor.getDataById("hhh"));
        }
    }
}