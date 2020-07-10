package ru.ashelestova.testtasks.novel;

import org.junit.Assert;
import org.junit.Test;

public class DataByIdAccessorTest {
    private final String filePath = DataByIdAccessor.class.getResource("/source1.txt").getPath();

    @Test
    public void getDataById() throws Exception {
        try (DataByIdAccessor dataByIdAccessor = new DataByIdAccessor(filePath)) {
            Assert.assertEquals("bar1", dataByIdAccessor.getDataById("@four"));
            Assert.assertEquals("baz1", dataByIdAccessor.getDataById("@six"));
            Assert.assertEquals("foo1", dataByIdAccessor.getDataById("@one"));
            Assert.assertNull(dataByIdAccessor.getDataById("@hhh"));
        }
    }
}