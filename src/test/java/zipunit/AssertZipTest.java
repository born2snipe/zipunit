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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    public void shouldFailWhenTheEntryExistsAndYouExpectedItToNotExist() {
        assertFailure("The entry [1.txt] appears to exist and we did not expect the entry to exist", new ExpectedAssertionFailure() {
            protected void performAssertion() {
                AssertZip.assertEntryDoesNotExist("1.txt", zipBuilder.build());
            }
        });
    }

    @Test
    public void shouldSupportAssertingAnEntryDoesNotExist() {
        AssertZip.assertEntryDoesNotExist("doesNotExist", zipBuilder.build());
    }

    @Test
    public void shouldFailWhenAnEntryCanNotBeFoundWhenComparingComments() {
        assertEntryDoesNotExistFailure("doesNotExist", new ExpectedAssertionFailure() {
            protected void performAssertion() {
                AssertZip.assertEntryComment("doesNotExist", "comment", zipBuilder.build());
            }
        });
    }

    @Test
    public void shouldFailWhenTheCommentsDoNotMatch() {
        zipBuilder.withEntry(entryWithComment("test.txt", "comment"));
        assertFailure("The entry comment does not match", new ExpectedAssertionFailure() {
            protected void performAssertion() {
                AssertZip.assertEntryComment("test.txt", "doesNotMatch", zipBuilder.build());
            }
        });
    }

    @Test
    public void shouldAllowAssertingTheCommentOfAnEntry() {
        zipBuilder.withEntry(entryWithComment("test.txt", "comment"));
        AssertZip.assertEntryComment("test.txt", "comment", zipBuilder.build());
    }

    @Test
    public void shouldAllowAssertingTheActualSizeOfTheFileEntry() {
        AssertZip.assertEntryActualSize("1.txt", 7, zipBuilder.build());
    }

    @Test
    public void shouldFailWhenTheEntryDoesNotExistWhenCheckingTheFileSize() {
        assertEntryDoesNotExistFailure("doesNotExist", new ExpectedAssertionFailure() {
            protected void performAssertion() {
                AssertZip.assertEntryActualSize("doesNotExist", 0, zipBuilder.build());
            }
        });
    }

    @Test
    public void shouldFailWhenTheFileSizeDoesNotMatch() {
        assertFailure("The entry expected size does not match", new ExpectedAssertionFailure() {
            protected void performAssertion() {
                AssertZip.assertEntryActualSize("1.txt", 0, zipBuilder.build());
            }
        });
    }

    @Test
    public void shouldFailWhenAnEntryDoesNotExistWhenAssertingADirectory() {
        assertEntryDoesNotExistFailure("doesNotExist/", new ExpectedAssertionFailure() {
            protected void performAssertion() {
                AssertZip.assertDirectoryEntryExist("doesNotExist", zipBuilder.build());
            }
        });
    }

    @Test
    public void shouldAllowAssertingDirectoryEntries() {
        AssertZip.assertDirectoryEntryExist("dir", zipBuilder.build());
    }

    @Test
    public void shouldFailIfAZipFileIsNotFoundWhenAssertingADirectory() {
        assertFileNotFoundFailure(new ExpectedAssertionFailure() {
            protected void performAssertion() {
                AssertZip.assertDirectoryEntryExist("dir", nonExistentZipFile());
            }
        });
    }

    @Test
    public void shouldFailIfAZipFileIsNotFoundWhenAssertingEntryBinaryContent() {
        assertFileNotFoundFailure(new ExpectedAssertionFailure() {
            protected void performAssertion() {
                AssertZip.assertEntry("2.bin", new byte[]{1, 2, 3}, nonExistentZipFile());
            }
        });
    }

    @Test
    public void shouldFailIfAZipFileIsNotFoundWhenAssertingEntryStringContent() {
        assertFileNotFoundFailure(new ExpectedAssertionFailure() {
            protected void performAssertion() {
                AssertZip.assertEntry("1.txt", "content", nonExistentZipFile());
            }
        });
    }

    @Test
    public void shouldFailIfAZipFileIsNotFoundWhenAssertingEntryCount() {
        assertFileNotFoundFailure(new ExpectedAssertionFailure() {
            protected void performAssertion() {
                AssertZip.assertNumberOfEntriesIs(0, nonExistentZipFile());
            }
        });
    }

    @Test
    public void shouldFailWhenTheNumberEntriesDoesNotMatch() {
        assertFailure("Number of entries do not match", new ExpectedAssertionFailure() {
            @Override
            protected void performAssertion() {
                AssertZip.assertNumberOfEntriesIs(0, zipBuilder.build());
            }
        });
    }

    @Test
    public void shouldFailWhenAnEntryBinaryContentsDoesNotMatch() {
        assertFailure("Expected content does not match for entry [2.bin]", new ExpectedAssertionFailure() {
            protected void performAssertion() {
                AssertZip.assertEntry("2.bin", new byte[]{0}, zipBuilder.build());
            }
        });
    }

    @Test
    public void shouldFailWhenAnEntryStringContentsDoesNotMatch() {
        assertFailure("Expected content does not match for entry [1.txt]", new ExpectedAssertionFailure() {
            protected void performAssertion() {
                AssertZip.assertEntry("1.txt", "doesNotMatch", zipBuilder.build());
            }
        });
    }

    @Test
    public void shouldFailWhenAnEntryDoesNotExistingInTheZipFile() {
        assertEntryDoesNotExistFailure("doesNotExist", new ExpectedAssertionFailure() {
            protected void performAssertion() {
                AssertZip.assertEntryExists("doesNotExist", zipBuilder.build());
            }
        });
    }

    @Test
    public void shouldSupportAssertingBinaryContentOfAnEntry() {
        AssertZip.assertEntry("2.bin", new byte[]{1, 2, 3}, zipBuilder.build());
    }

    @Test
    public void shouldSupportAssertingTheNumberOfEntriesInAFile() throws IOException {
        ZipBuilder zipBuilder = new ZipBuilder(temporaryFolder.newFolder());
        zipBuilder.withEntry("0.txt", "");
        AssertZip.assertNumberOfEntriesIs(1, zipBuilder.build());
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

    private ZipBuilder.Entry entryWithComment(String entryPath, String comment) {
        ZipBuilder.Entry entry = new ZipBuilder.Entry(entryPath, "");
        entry.setComment(comment);
        return entry;
    }

    private void assertEntryDoesNotExistFailure(final String expectedEntry, ExpectedAssertionFailure expectedAssertionFailure) {
        assertFailure("Expected to find entry [" + expectedEntry + "], but was not found", expectedAssertionFailure);
    }

    private void assertFileNotFoundFailure(ExpectedAssertionFailure expectedAssertionFailure) {
        assertFailure("ZIP file does not exist", expectedAssertionFailure);
    }

    private void assertFailure(String expectedMessage, ExpectedAssertionFailure expectedAssertionFailure) {
        String actualMessage = expectedAssertionFailure.runAssertion();
        assertTrue("Please provide a message", expectedMessage.trim().length() > 0);
        assertTrue("Failure message does not start as we expected.\n" +
                        "\texpected=[" + expectedMessage + "]\n" +
                        "\tactual=[" + actualMessage + "]",
                actualMessage.startsWith(expectedMessage)
        );
    }

    private abstract class ExpectedAssertionFailure {
        public String runAssertion() {
            try {
                performAssertion();
                fail("we expected the assertion to fail");
                return null;
            } catch (AssertionError error) {
                return error.getMessage();
            }
        }

        protected abstract void performAssertion();
    }
}
