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

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import org.apache.maven.shared.utils.cli.Commandline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;

public class JLinkMojoTest {

    private JLinkMojo mojo = new JLinkMojo(null, null, null, null);

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field stripDebug = mojo.getClass().getDeclaredField("stripDebug");
        stripDebug.setAccessible(true);
        stripDebug.set(mojo, Boolean.TRUE);
    }

    @Test
    void doubleQuoteEveryArgument() throws Exception {
        // when
        List<String> jlinkArgs = mojo.createJlinkArgs(List.of(), List.of());

        // then
        assertThat(jlinkArgs).noneMatch(arg -> arg.trim().isBlank());
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    void singleQuotesShellCommandUnix() throws Exception {
        // when
        List<String> jlinkArgs = mojo.createJlinkArgs(List.of("foo", "bar"), List.of("mvn", "jlink"));
        Commandline cmdLine = JLinkExecutor.createJLinkCommandLine(new File("/path/to/jlink"), jlinkArgs);

        // then
        assertThat(cmdLine.toString())
                .isEqualTo(
                        "/bin/sh -c '/path/to/jlink \"--strip-debug\" \"--module-path\" \"foo:bar\" \"--add-modules\" \"mvn,jlink\"'");
    }

    @EnabledOnOs(OS.WINDOWS)
    @Test
    void singleQuotesShellCommandWindows() throws Exception {
        // when
        List<String> jlinkArgs = mojo.createJlinkArgs(List.of("foo", "bar"), List.of("mvn", "jlink"));
        Commandline cmdLine = JLinkExecutor.createJLinkCommandLine(new File("/path/to/jlink"), jlinkArgs);

        // then
        assertThat(cmdLine.toString()).startsWith("cmd.exe ");
        assertThat(cmdLine.toString())
                .contains(
                        "\\path\\to\\jlink \"--strip-debug\" \"--module-path\" \"foo;bar\" \"--add-modules\" \"mvn,jlink");
    }
}
