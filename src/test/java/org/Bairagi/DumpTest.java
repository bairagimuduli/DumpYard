package org.Bairagi;

import org.Bairagi.dump.JsonToPojoUtil;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class DumpTest {
    @Test
    public void testDump() throws IOException {
        JsonToPojoUtil.generatePojoFromJsonFile(
                new File("src/main/resources/dump/marvel_charaters.json"),"org.Bairagi.dump.pojos", "MarvelCharacters");
    }

}
