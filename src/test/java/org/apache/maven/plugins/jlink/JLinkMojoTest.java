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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.apache.maven.shared.utils.cli.Commandline;
import org.apache.maven.toolchain.ToolchainManager;
import org.codehaus.plexus.languages.java.jpms.JavaModuleDescriptor;
import org.codehaus.plexus.languages.java.jpms.LocationManager;
import org.codehaus.plexus.languages.java.jpms.ResolvePathsRequest;
import org.codehaus.plexus.languages.java.jpms.ResolvePathsResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JLinkMojoTest {

    private JLinkMojo mojo;

    @Mock
    private ToolchainManager toolchainManager;

    @Mock
    private MavenProjectHelper projectHelper;

    @Mock
    private MavenResourcesFiltering mavenResourcesFiltering;

    @Mock
    private LocationManager locationManager;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mojo = new JLinkMojo(projectHelper, toolchainManager, mavenResourcesFiltering, locationManager);
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

    @Test
    void getCompileClasspathElementsShouldSkipPomTypeArtifacts() throws Exception {
        // Given
        MavenProject project = Mockito.mock(MavenProject.class);

        Artifact pomArtifact = new DefaultArtifact(
                "group",
                "artifact-pom",
                VersionRange.createFromVersion("1.0"),
                "compile",
                "pom",
                null,
                new DefaultArtifactHandler("pom"));

        Artifact jarArtifact = new DefaultArtifact(
                "group",
                "artifact-jar",
                VersionRange.createFromVersion("1.0"),
                "compile",
                "jar",
                null,
                new DefaultArtifactHandler("jar"));

        File jarFile = new File("some.jar");
        jarArtifact.setFile(jarFile);

        when(project.getArtifacts()).thenReturn(Set.of(pomArtifact, jarArtifact));

        // When
        List<File> classpathElements = mojo.getCompileClasspathElements(project);

        // Then
        assertThat(classpathElements).containsExactly(jarFile).doesNotContainNull();
    }

    @Test
    void testGetModulePathElements() throws Exception {
        File outputDirectory = new File("target/test-classes");
        outputDirectory.mkdirs();

        MavenProject project = mock(MavenProject.class);
        Artifact artifact = mock(Artifact.class);
        when(artifact.getFile()).thenReturn(new File("target/test-classes/dependency.jar"));
        when(project.getArtifacts()).thenReturn(Collections.singleton(artifact));
        when(project.getBasedir()).thenReturn(new File("."));

        JavaModuleDescriptor moduleDescriptor = mock(JavaModuleDescriptor.class);
        when(moduleDescriptor.name()).thenReturn("test.module");
        when(moduleDescriptor.isAutomatic()).thenReturn(false);

        ResolvePathsResult<File> resolvePathsResult = mock(ResolvePathsResult.class);
        when(resolvePathsResult.getPathElements())
                .thenReturn(Collections.singletonMap(new File("target/test-classes/dependency.jar"), moduleDescriptor));
        when(locationManager.resolvePaths(any(ResolvePathsRequest.class))).thenReturn(resolvePathsResult);

        Field projectField = mojo.getClass().getSuperclass().getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);

        Field outputDirectoryField = mojo.getClass().getDeclaredField("outputDirectory");
        outputDirectoryField.setAccessible(true);
        outputDirectoryField.set(mojo, outputDirectory);

        Map<String, File> modulePathElements = mojo.getModulePathElements();

        assertThat(modulePathElements).hasSize(1);
        assertThat(modulePathElements).containsKey("test.module");
        assertThat(modulePathElements.get("test.module")).isEqualTo(new File("target/test-classes/dependency.jar"));
    }

    @Test
    void testIgnoreAutomaticModules() throws Exception {
        File outputDirectory = new File("target/test-classes");
        outputDirectory.mkdirs();

        MavenProject project = mock(MavenProject.class);
        Artifact artifact1 = mock(Artifact.class);
        when(artifact1.getFile()).thenReturn(new File("target/test-classes/dependency1.jar"));
        Artifact artifact2 = mock(Artifact.class);
        when(artifact2.getFile()).thenReturn(new File("target/test-classes/dependency2.jar"));

        when(project.getArtifacts()).thenReturn(Set.of(artifact1, artifact2));
        when(project.getBasedir()).thenReturn(new File("."));

        JavaModuleDescriptor moduleDescriptor1 = mock(JavaModuleDescriptor.class);
        when(moduleDescriptor1.name()).thenReturn("valid.module");
        when(moduleDescriptor1.isAutomatic()).thenReturn(false);

        JavaModuleDescriptor moduleDescriptor2 = mock(JavaModuleDescriptor.class);
        when(moduleDescriptor2.name()).thenReturn("automatic.module");
        when(moduleDescriptor2.isAutomatic()).thenReturn(true);

        ResolvePathsResult<File> resolvePathsResult = mock(ResolvePathsResult.class);
        when(resolvePathsResult.getPathElements())
                .thenReturn(Map.of(
                        new File("target/test-classes/dependency1.jar"), moduleDescriptor1,
                        new File("target/test-classes/dependency2.jar"), moduleDescriptor2));

        when(locationManager.resolvePaths(any(ResolvePathsRequest.class))).thenReturn(resolvePathsResult);

        Field projectField = mojo.getClass().getSuperclass().getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(mojo, project);

        Field outputDirectoryField = mojo.getClass().getDeclaredField("outputDirectory");
        outputDirectoryField.setAccessible(true);
        outputDirectoryField.set(mojo, outputDirectory);

        Map<String, File> modulePathElements = mojo.getModulePathElements();

        assertThat(modulePathElements).hasSize(1);
        assertThat(modulePathElements).containsKey("valid.module");
        assertThat(modulePathElements).doesNotContainKey("automatic.module");
    }
}
