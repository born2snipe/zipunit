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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertNotNull;

public class ZipBuilder {
    private File folder;
    private ArrayList<Entry> entries = new ArrayList<Entry>();

    public ZipBuilder() {
        this(null);
    }

    public ZipBuilder(File folder) {
        this.folder = folder;
    }

    public ZipBuilder withEntry(String entryName, String content) {
        return withEntry(entryName, content.getBytes());
    }

    public ZipBuilder withEntry(String entryName, byte[] content) {
        return withEntry(new Entry(entryName, new ByteArrayInputStream(content)));
    }

    public ZipBuilder withEntry(Entry entry) {
        entries.add(entry);
        return this;
    }

    public ZipBuilder withDirEntry(String directoryName) {
        return withEntry(new Entry(dirName(directoryName), null));
    }

    public File build() {
        return build(System.nanoTime() + ".zip");
    }

    public File build(String filename) {
        assertNotNull("You need to provide a folder when constructing the builder and want to use this build method", folder);
        return build(new File(folder, filename));
    }

    public File build(File file) {
        ZipOutputStream output = null;
        try {
            output = new ZipOutputStream(new FileOutputStream(file));
            for (Entry entry : entries) {
                ZipEntry zipEntry = new ZipEntry(entry.name);
                output.putNextEntry(zipEntry);
                if (entry.content != null) {
                    copyContent(output, entry.content);
                }
                output.closeEntry();
            }
            output.finish();
            return file;
        } catch (Exception e) {
            throw new RuntimeException("A problem occurred while building zip file", e);
        } finally {
            close(output);
        }
    }

    private void copyContent(ZipOutputStream output, InputStream input) throws IOException {
        byte[] buffer = new byte[1024];
        int length = -1;
        try {
            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
            output.flush();
        } finally {
            close(input);
        }
    }

    private void close(InputStream input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {

            }
        }
    }

    private String dirName(String directoryName) {
        if (directoryName.endsWith("/")) {
            return directoryName;
        }
        return directoryName + "/";
    }

    private void close(ZipOutputStream output) {
        if (output != null) {
            try {
                output.closeEntry();
            } catch (IOException e) {

            }
        }
    }

    public static class Entry {
        private final String name;
        private final InputStream content;

        public Entry(String name, InputStream content) {
            this.name = name;
            this.content = content;
        }
    }
}
