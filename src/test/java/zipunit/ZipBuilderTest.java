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

public class ZipBuilderTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private ZipBuilder zipBuilder;

    @Before
    public void setUp() throws Exception {
        zipBuilder = new ZipBuilder(temporaryFolder.newFolder());
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
