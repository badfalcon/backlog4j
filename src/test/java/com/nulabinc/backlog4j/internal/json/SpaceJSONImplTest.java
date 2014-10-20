package com.nulabinc.backlog4j.internal.json;

import com.nulabinc.backlog4j.SharedFile;
import com.nulabinc.backlog4j.Space;
import org.junit.Test;
import uk.co.it.modular.hamcrest.date.DateMatchers;

import java.io.IOException;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author nulab-inc
 */
public class SpaceJSONImplTest extends AbstractJSONImplTest{
    @Test
    public void createSpaceTest() throws IOException {
        String fileContentStr = getJsonString("json/space.json");
        Space space = factory.createSpace(fileContentStr);

        assertEquals("nulab-test", space.getName());
        assertEquals("nulab-test", space.getSpaceKey());
        assertEquals(1234567890, space.getOwnerId());
        assertEquals("ja", space.getLang());
        assertEquals("09:00:00", space.getReportSendTime());
        assertEquals("backlog", space.getTextFormattingRule());

        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, 0, 21, 7, 30, 9);
        assertThat(calendar.getTime(), DateMatchers.sameDay(space.getCreated()));
        calendar.set(2014, 3, 2, 9, 27, 7);
        assertThat(calendar.getTime(), DateMatchers.sameDay(space.getUpdated()));

    }

    @Test
    public void equalsTest() throws IOException {
        String fileContentStr = getJsonString("json/space.json");
        Space space1 = factory.createSpace(fileContentStr);
        Space space2 = factory.createSpace(fileContentStr);
        assertEquals(space1, space2);

    }

    @Test
    public void hashCodeTest() throws IOException {
        String fileContentStr = getJsonString("json/space.json");
        Space space1 = factory.createSpace(fileContentStr);
        Space space2 = factory.createSpace(fileContentStr);
        assertEquals(space1.hashCode(), space2.hashCode());

    }
}
