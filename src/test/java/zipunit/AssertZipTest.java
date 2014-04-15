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

import java.io.File;
import java.io.IOException;

public class AssertZipTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private ZipBuilder zipBuilder;

    @Before
    public void setUp() throws Exception {
        zipBuilder = new ZipBuilder(temporaryFolder.newFolder());
        zipBuilder.withEntry("1.txt", "content");
        zipBuilder.withEntry("2.bin", new byte[]{1, 2, 3});
        zipBuilder.withDirEntry("dir/");
    }

    @Test
    public void shouldAllowAssertingTheActualSizeOfTheFileEntry() {
        AssertZip.assertEntryActualSize("1.txt", 7, zipBuilder.build());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenTheEntryDoesNotExistWhenCheckingTheFileSize() {
        AssertZip.assertEntryActualSize("doesNotExist", 0, zipBuilder.build());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenTheFileSizeDoesNotMatch() {
        AssertZip.assertEntryActualSize("1.txt", 0, zipBuilder.build());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenAnEntryDoesNotExistWhenAssertingADirectory() {
        AssertZip.assertDirectoryEntryExist("doesNotExist", zipBuilder.build());
    }

    @Test
    public void shouldAllowAssertingDirectoryEntries() {
        AssertZip.assertDirectoryEntryExist("dir", zipBuilder.build());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfAZipFileIsNotFoundWhenAssertingADirectory() {
        AssertZip.assertDirectoryEntryExist("dir", nonExistentZipFile());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfAZipFileIsNotFoundWhenAssertingEntryBinaryContent() {
        AssertZip.assertEntry("2.bin", new byte[]{1, 2, 3}, nonExistentZipFile());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfAZipFileIsNotFoundWhenAssertingEntryStringContent() {
        AssertZip.assertEntry("1.txt", "content", nonExistentZipFile());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailIfAZipFileIsNotFoundWhenAssertingEntryCount() {
        AssertZip.assertNumberOfEntriesIs(0, nonExistentZipFile());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenTheNumberEntriesDoesNotMatch() {
        AssertZip.assertNumberOfEntriesIs(0, zipBuilder.build());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenAnEntryBinaryContentsDoesNotMatch() {
        AssertZip.assertEntry("2.bin", new byte[]{0}, zipBuilder.build());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenAnEntryStringContentsDoesNotMatch() {
        AssertZip.assertEntry("1.txt", "doesNotMatch", zipBuilder.build());
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenAnEntryDoesNotExistingInTheZipFile() {
        AssertZip.assertEntryExists("doesNotExist", zipBuilder.build());
    }

    @Test
    public void shouldSupportAssertingBinaryContentOfAnEntry() {
        AssertZip.assertEntry("2.bin", new byte[]{1, 2, 3}, zipBuilder.build());
    }

    @Test
    public void shouldSupportAssertingTheNumberOfEntriesInAFile() throws IOException {
        ZipBuilder zipBuilder = new ZipBuilder(temporaryFolder.newFolder());
        zipBuilder.withEntry("0.txt", "");
        AssertZip.assertNumberOfEntriesIs(3, this.zipBuilder.build());
    }

    @Test
    public void shouldSupportAssertingTheExistenceOfAEntry() {
        AssertZip.assertEntryExists("1.txt", zipBuilder.build());
    }

    @Test
    public void shouldSupportAssertingEntryContentAsAString() {
        AssertZip.assertEntry("1.txt", "content", zipBuilder.build());
    }

    private File nonExistentZipFile() {
        return new File("doesNotExist.zip");
    }
}
