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
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.cli.Commandline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class JLinkMojoTest {

    private JLinkMojo mojo = new JLinkMojo(null, null, null, null);

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field stripDebug = mojo.getClass().getDeclaredField("stripDebug");
        stripDebug.setAccessible(true);
        stripDebug.set(mojo, Boolean.TRUE);
    }

    @Test
    void double_quote_every_argument() throws Exception {
        // when
        List<String> jlinkArgs = mojo.createJlinkArgs(List.of(), List.of());

        // then
        assertThat(jlinkArgs).noneMatch(arg -> arg.trim().isBlank());
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    void single_quotes_shell_command_unix() throws Exception {
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
    void single_quotes_shell_command_windows() throws Exception {
        // when
        List<String> jlinkArgs = mojo.createJlinkArgs(List.of("foo", "bar"), List.of("mvn", "jlink"));
        Commandline cmdLine = JLinkExecutor.createJLinkCommandLine(new File("/path/to/jlink"), jlinkArgs);

        // then
        assertThat(cmdLine.toString()).startsWith("cmd.exe ");
        assertThat(cmdLine.toString())
                .contains(
                        "\\path\\to\\jlink \"--strip-debug\" \"--module-path\" \"foo;bar\" \"--add-modules\" \"mvn,jlink");
    }

    @Test
    void getCompileClasspathElements() throws Exception {
        // Given
        MavenProject project = Mockito.mock(MavenProject.class);

        Artifact pomArtifact = Mockito.mock(Artifact.class);
        when(pomArtifact.getType()).thenReturn("pom");

        Artifact jarArtifact = Mockito.mock(Artifact.class);
        when(jarArtifact.getType()).thenReturn("jar");
        when(jarArtifact.getFile()).thenReturn(new File("some.jar"));

        when(project.getArtifacts()).thenReturn(Set.of(pomArtifact, jarArtifact));

        List<File> classpathElements = mojo.getCompileClasspathElements(project);

        // Then
        assertThat(classpathElements).containsExactly(new File("some.jar"));
    }
}
