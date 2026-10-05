package com.j256.simplecsv.processor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.Test;
import com.j256.simplecsv.common.CsvColumn;

public class EmptyHeaderTest {
    @Test
    public void emptyHeaderPreservesColumnPositions() throws Exception {
        CsvProcessor<Row> processor = new CsvProcessor<Row>(Row.class).withIgnoreUnknownColumns(true);
        BufferedReader input = new BufferedReader(new StringReader("first,,second\nA,ignored,B\n"));
        assertArrayEquals(new String[] {"first", "", "second"}, processor.readHeader(input, null));
        Row row = processor.readRow(input, null);
        assertEquals("A", row.first);
        assertEquals("B", row.second);
    }

    @Test
    public void quotedEmptyHeaderPreservesColumnPositions() throws Exception {
        CsvProcessor<Row> processor = new CsvProcessor<Row>(Row.class).withIgnoreUnknownColumns(true);
        BufferedReader input = new BufferedReader(new StringReader("first,\"\",second\nA,ignored,B\n"));
        processor.readHeader(input, null);
        assertEquals("B", processor.readRow(input, null).second);
    }

    @Test
    public void leadingAndTrailingEmptyHeadersAreRetained() throws Exception {
        CsvProcessor<Row> processor = new CsvProcessor<Row>(Row.class).withIgnoreUnknownColumns(true);
        BufferedReader input = new BufferedReader(new StringReader(",first,second,\nignored,A,B,ignored\n"));
        assertArrayEquals(new String[] {"", "first", "second", ""}, processor.readHeader(input, null));
        Row row = processor.readRow(input, null);
        assertEquals("A", row.first);
        assertEquals("B", row.second);
    }

    @Test
    public void ordinaryHeaderDoesNotGainAnExtraColumn() throws Exception {
        CsvProcessor<Row> processor = new CsvProcessor<Row>(Row.class);
        assertArrayEquals(new String[] {"first", "second"}, processor.readHeader(
                new BufferedReader(new StringReader("first,second\n")), null));
    }

    public static class Row {
        @CsvColumn
        public String first;
        @CsvColumn
        public String second;
    }
}
