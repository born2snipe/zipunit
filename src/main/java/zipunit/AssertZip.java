/**
 *
 * Copyright to the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package zipunit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.*;

public class AssertZip {
    public static void assertEntry(String expectedEntry, String expectedContents, File actualZipFile) {
        assertEntry(expectedEntry, expectedContents.getBytes(), actualZipFile);
    }

    public static void assertEntry(String expectedEntry, byte[] expectedContents, File actualZipFile) {
        assertEntryExists(expectedEntry, actualZipFile);
        Map<String, byte[]> entries = entriesIn(actualZipFile);
        assertArrayEquals("Expected content does not match for entry [" + expectedEntry + "]",
                expectedContents, entries.get(expectedEntry));
    }

    public static void assertEntryExists(String expectedEntry, File actualZipFile) {
        Map<String, byte[]> entries = entriesIn(actualZipFile);
        assertTrue("Expected to find entry [" + expectedEntry + "], but was not found. Actual entries:" + entries,
                entries.keySet().contains(expectedEntry));
    }

    public static void assertNumberOfEntriesIs(int expectedNumberOfEntries, File actualZipFile) {
        assertEquals("Number of entries do not match", expectedNumberOfEntries, entriesIn(actualZipFile).size());
    }

    public static void assertDirectoryEntryExist(final String expectedDirectoryPath, File actualZip) {
        final String directoryPath = dirName(expectedDirectoryPath);
        assertFileExists(actualZip);
        assertEntryExists(directoryPath, actualZip);
        open(actualZip, new WhileZipIsOpen() {
            public void whileOpen(ZipFile zipFile) throws Exception {
                ZipEntry entry = zipFile.getEntry(directoryPath);
                assertTrue("It appears the entry [" + expectedDirectoryPath + "] is not a directory", entry.isDirectory());
            }
        });
    }

    private static String dirName(String expectedDirectoryPath) {
        if (expectedDirectoryPath.endsWith("/")) {
            return expectedDirectoryPath;
        }
        return expectedDirectoryPath + "/";
    }

    private static Map<String, byte[]> entriesIn(File actualZipFile) {
        assertFileExists(actualZipFile);
        final LinkedHashMap<String, byte[]> entriesInZip = new LinkedHashMap<String, byte[]>();
        open(actualZipFile, new EachEntry() {
            protected void eachEntryOf(ZipFile zip, ZipEntry zipEntry) throws Exception {
                String name = zipEntry.getName();
                entriesInZip.put(name, contentsOf(zipEntry, zip));
            }
        });
        return entriesInZip;
    }

    private static void assertFileExists(File actualZipFile) {
        assertTrue("ZIP file does not exist", actualZipFile.exists());
    }

    private static void open(File zipFile, WhileZipIsOpen opener) {
        ZipFile zip = null;
        try {
            zip = new ZipFile(zipFile);
            opener.whileOpen(zip);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close(zip);
        }
    }

    private static byte[] contentsOf(ZipEntry entry, ZipFile file) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int length = -1;
        byte[] buffer = new byte[1024];

        InputStream input = null;
        try {
            input = file.getInputStream(entry);
            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
        } finally {
            close(input);
        }

        return output.toByteArray();
    }

    private static void close(InputStream input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {

            }
        }
    }

    private static void close(ZipFile zip) {
        if (zip != null) {
            try {
                zip.close();
            } catch (IOException e) {

            }
        }
    }

    private static interface WhileZipIsOpen {
        void whileOpen(ZipFile zipFile) throws Exception;
    }

    private static abstract class EachEntry implements WhileZipIsOpen {
        public void whileOpen(ZipFile zipFile) throws Exception {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                eachEntryOf(zipFile, entries.nextElement());
            }
        }

        protected abstract void eachEntryOf(ZipFile zipFile, ZipEntry entry) throws Exception;
    }
}
