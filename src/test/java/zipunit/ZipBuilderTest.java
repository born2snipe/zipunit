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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ZipBuilderTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private ZipBuilder zipBuilder;

    @Before
    public void setUp() throws Exception {
        zipBuilder = new ZipBuilder(temporaryFolder.newFolder());
    }

    @Test
    public void shouldAllowProvidingAnInputStreamAsEntryContent() {
        zipBuilder.withEntry("test.txt", new ByteArrayInputStream("content".getBytes()));
        AssertZip.assertEntry("test.txt", "content", zipBuilder.build());
    }

    @Test
    public void shouldAllowProvidingCommentsOnEntries() {
        ZipBuilder.Entry entry = new ZipBuilder.Entry("1.txt", "content");
        entry.setComment("comment");
        zipBuilder.withEntry(entry);
        AssertZip.assertEntryComment("1.txt", "comment", zipBuilder.build());
    }

    @Test
    public void shouldCloseTheInputStreamOfTheEntryEvenWhenThereIsAnErrorReading() {
        MockInputStream input = new MockInputStream("content");
        input.throwExceptionOnRead();

        zipBuilder.withEntry(new ZipBuilder.Entry("1.txt", input));
        try {
            zipBuilder.build();
            fail();
        } catch (Exception e) {

        }

        assertTrue(input.isClosed());
    }

    @Test
    public void shouldCloseTheInputStreamOfTheEntry() {
        MockInputStream input = new MockInputStream("content");

        zipBuilder.withEntry(new ZipBuilder.Entry("1.txt", input));
        zipBuilder.build();

        assertTrue(input.isClosed());
    }

    @Test
    public void shouldAutomaticallySetTheSizeOfAnEntry() {
        zipBuilder.withEntry("test/1.txt", "content");
        AssertZip.assertEntryActualSize("test/1.txt", 7, zipBuilder.build());
    }

    @Test
    public void shouldAllowAddingAnEntryWithAnInputStream() {
        zipBuilder.withEntry(new ZipBuilder.Entry("test.txt", new ByteArrayInputStream("content".getBytes())));
        AssertZip.assertEntry("test.txt", "content", zipBuilder.build());
    }
    
    @Test(expected = AssertionError.class)
    public void shouldBlowUpIfYouTryToBuildAZipWithoutAFolder() {
        new ZipBuilder().build("test");
    }

    @Test
    public void shouldAllowCreatingDirectoryEntriesWithoutATrailingSlash() {
        zipBuilder.withDirEntry("dir");
        AssertZip.assertDirectoryEntryExist("dir", zipBuilder.build());
    }

    @Test
    public void shouldAllowCreatingDirectoryEntriesWithATrailingSlash() {
        zipBuilder.withDirEntry("dir/");
        AssertZip.assertDirectoryEntryExist("dir/", zipBuilder.build());
    }

}
