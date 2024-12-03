/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.plugins.jlink;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Karl Heinz Marbaise <a href="mailto:khmarbaise@apache.org">khmarbaise@apache.org</a>
 */
public class AbstractJLinkMojoTest {
    private AbstractJLinkMojo mojoMock;

    @BeforeEach
    public void before() {
        this.mojoMock = mock(AbstractJLinkMojo.class, Mockito.CALLS_REAL_METHODS);
        when(mojoMock.getLog()).thenReturn(mock(Log.class));
    }

    @Test
    @DisplayName("convert should return single characters")
    public void convertShouldReturnSingleCharacter() {
        StringBuilder result = mojoMock.convertSeparatedModulePathToPlatformSeparatedModulePath("x");
        assertThat(result).isNotEmpty().hasToString("x");
    }

    @Test
    @DisplayName("convert should two characters separated by path separator")
    public void convertShouldReturnTwoCharactersSeparatedByPathSeparator() {
        StringBuilder result = mojoMock.convertSeparatedModulePathToPlatformSeparatedModulePath("x;a");
        assertThat(result).hasToString("x" + File.pathSeparatorChar + "a");
    }

    @Test
    @DisplayName("convert using differential delimiter should return two characters separated by path separator")
    public void convertUsingDifferentDelimiterShouldReturnTwoCharactersSeparatedByPathSeparator() {
        StringBuilder result = mojoMock.convertSeparatedModulePathToPlatformSeparatedModulePath("x:a");
        assertThat(result).hasToString("x" + File.pathSeparatorChar + "a");
    }

    @Test
    @DisplayName("convertSeparatedModulePathToPlatformSeparatedModulePath() "
            + "should return two characters separated by path separator")
    public void convertUsingMultipleDelimitersShouldReturnTwoCharactersSeparatedByPathSeparator() {
        StringBuilder result = mojoMock.convertSeparatedModulePathToPlatformSeparatedModulePath("x:a::");
        assertThat(result).hasToString("x" + File.pathSeparatorChar + "a");
    }

    @Test
    @DisplayName("getPlatformDependSeparateList() should return a single character")
    public void getPlatformDependSeparateListShouldReturnASingleCharacter() {
        String result = mojoMock.getPlatformDependSeparateList(Collections.singletonList("A"));
        assertThat(result).isEqualTo("A");
    }

    @Test
    @DisplayName("getPlatformDependSeparateList() should return two characters separated")
    public void getPlatformDependSeparateListShouldReturnTwoCharactersSeparated() {
        String result = mojoMock.getPlatformDependSeparateList(Arrays.asList("A", "B"));
        assertThat(result).isEqualTo("A" + File.pathSeparatorChar + "B");
    }

    @Test
    @DisplayName("getPlatformDependSeparateList() should return three characters separated")
    public void getPlatformDependSeparateListShouldReturnThreeCharactersSeparated() {
        String result = mojoMock.getPlatformDependSeparateList(Arrays.asList("A", "B", "C"));
        assertThat(result).isEqualTo("A" + File.pathSeparatorChar + "B" + File.pathSeparatorChar + "C");
    }

    @Test
    @DisplayName("getCommaSeparatedList() should return a single character")
    public void getCommaSeparatedListShouldReturnASingleCharacter() {
        String result = String.join( ",", Collections.singletonList( "A" ) );
        assertThat(result).isEqualTo("A");
    }

    @Test
    @DisplayName("getCommaSeparatedList() should return two characters separated by comma")
    public void getCommaSeparatedListShouldReturnTwoCharactersSeparatedByComma() {
        String result = String.join( ",", Arrays.asList( "A", "B" ) );
        assertThat(result).isEqualTo("A,B");
    }
}
