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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class AssertZipPerformanceTest {
    private static File zipFile;

    @BeforeClass
    public static void setUp() throws Exception {
        ZipBuilder zipBuilder = new ZipBuilder();
        for (int i = 0; i < 10000; i++) {
            zipBuilder.withEntry(i + ".txt", "content");
        }
        zipFile = zipBuilder.build(File.createTempFile("test", ".zip"));
    }

    @AfterClass
    public static void deleteFile() {
        zipFile.delete();
    }

    @Test
    public void assertTheContentOfAnEntryShouldBePerformant() {
        assertElapsedTime(100L, new TimedTask() {
            public void performTask() {
                AssertZip.assertEntry("1000.txt", "content", zipFile);
            }
        });
    }

    @Test
    public void assertingTheNumberOfEntriesShouldBePerformant() {
        assertElapsedTime(100L, new TimedTask() {
            public void performTask() {
                AssertZip.assertNumberOfEntriesIs(10000, zipFile);
            }
        });
    }

    @Test
    public void assertingEntryExistenceShouldBePerformant() {
        assertElapsedTime(100L, new TimedTask() {
            public void performTask() {
                AssertZip.assertEntryExists("1.txt", zipFile);
            }
        });
    }

    private void assertElapsedTime(long expectedMinTime, TimedTask task) {
        long actualTime = time(task);
        assertTrue("Expected the task to finish <= " + expectedMinTime + " millis, but was " + actualTime + " millis",
                actualTime <= expectedMinTime);
    }

    private long time(TimedTask task) {
        long start = System.currentTimeMillis();
        task.performTask();
        return System.currentTimeMillis() - start;
    }

    private interface TimedTask {
        public void performTask();
    }
}
