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
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Checks how each mojo parameter turns into a jlink command-line argument. Whether jlink accepts the
 * combination is covered by the integration tests under {@code src/it/projects/cli-options}.
 */
class JLinkMojoArgumentsTest {

    private JLinkMojo mojo;

    @BeforeEach
    void setUp() {
        mojo = new JLinkMojo(null, null, null, null);
    }

    private void set(String field, Object value) throws Exception {
        Field f = JLinkMojo.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(mojo, value);
    }

    private List<String> args() throws Exception {
        return mojo.createJlinkArgs(List.of(), List.of());
    }

    @Test
    void noOptionsGiveNoArguments() throws Exception {
        assertThat(args()).isEmpty();
    }

    @Test
    void stripDebug() throws Exception {
        set("stripDebug", true);
        assertThat(args()).containsExactly("--strip-debug");
    }

    @Test
    void bindServices() throws Exception {
        set("bindServices", true);
        assertThat(args()).containsExactly("--bind-services");
    }

    @Test
    void ignoreSigningInformation() throws Exception {
        set("ignoreSigningInformation", true);
        assertThat(args()).containsExactly("--ignore-signing-information");
    }

    @Test
    void noHeaderFiles() throws Exception {
        set("noHeaderFiles", true);
        assertThat(args()).containsExactly("--no-header-files");
    }

    @Test
    void noManPages() throws Exception {
        set("noManPages", true);
        assertThat(args()).containsExactly("--no-man-pages");
    }

    @Test
    void verbose() throws Exception {
        set("verbose", true);
        assertThat(args()).containsExactly("--verbose");
    }

    @ParameterizedTest
    @ValueSource(strings = {"little", "big"})
    void endian(String value) throws Exception {
        set("endian", value);
        assertThat(args()).containsExactly("--endian", value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "1", "2", "zip-1", "zip-6"})
    void compress(String value) throws Exception {
        set("compress", value);
        assertThat(args()).containsExactly("--compress", value);
    }

    @Test
    void disablePlugin() throws Exception {
        set("disablePlugin", "compress");
        assertThat(args()).containsExactly("--disable-plugin", "compress");
    }

    @Test
    void addOptionsAreJoinedIntoOneArgument() throws Exception {
        set("addOptions", List.of("-Xmx128m", "--enable-preview", "-Dvar=value"));
        assertThat(args()).containsExactly("--add-options=-Xmx128m --enable-preview -Dvar=value");
    }

    @Test
    void emptyAddOptionsGiveNoArgument() throws Exception {
        set("addOptions", List.of());
        assertThat(args()).isEmpty();
    }

    @Test
    void includeLocalesAddLocaleDataModule() throws Exception {
        set("includeLocales", List.of("en", "ja", "de", "*-IN"));
        assertThat(args()).containsExactly("--add-modules", "jdk.localedata", "--include-locales", "en,ja,de,*-IN");
    }

    @Test
    void limitModules() throws Exception {
        set("limitModules", List.of("java.base", "java.logging"));
        assertThat(args()).containsExactly("--limit-modules", "java.base,java.logging");
    }

    @Test
    void suggestProvidersComeLast() throws Exception {
        set("suggestProviders", List.of("java.security.Provider", "java.sql.Driver"));
        set("verbose", true);
        assertThat(args())
                .containsExactly("--verbose", "--suggest-providers", "java.security.Provider,java.sql.Driver");
    }

    @Test
    void singleLauncher() throws Exception {
        set("launcher", "hello=mod/pkg.Main");
        assertThat(args()).containsExactly("--launcher", "hello=mod/pkg.Main");
    }

    @Test
    void launcherAndLaunchersTogetherFail() throws Exception {
        set("launcher", "a=m/A");
        set("launchers", List.of("b=m/B"));
        assertThatThrownBy(this::args).isInstanceOf(MojoExecutionException.class);
    }

    @Test
    void outputIsTheImageDirectory() throws Exception {
        set("buildDirectory", new File("target"));
        set("outputDirectoryImage", new File("target/maven-jlink/default"));
        assertThat(args()).containsExactly("--output", new File("target/maven-jlink/default").getAbsolutePath());
    }

    @Test
    void modulePathAndModulesToAdd() throws Exception {
        List<String> result = mojo.createJlinkArgs(List.of("a"), List.of("m1", "m2"));
        assertThat(result).containsExactly("--module-path", "a", "--add-modules", "m1,m2");
    }

    @Test
    void allOptionsTogetherKeepTheirRelativeOrder() throws Exception {
        set("stripDebug", true);
        set("bindServices", true);
        set("endian", "big");
        set("ignoreSigningInformation", true);
        set("compress", "2");
        set("addOptions", List.of("-Xmx128m"));
        set("noHeaderFiles", true);
        set("noManPages", true);
        set("includeLocales", List.of("en"));
        set("verbose", true);

        assertThat(mojo.createJlinkArgs(List.of("p"), List.of("m")))
                .containsExactly(
                        "--strip-debug",
                        "--bind-services",
                        "--endian",
                        "big",
                        "--ignore-signing-information",
                        "--compress",
                        "2",
                        "--add-options=-Xmx128m",
                        "--module-path",
                        "p",
                        "--no-header-files",
                        "--no-man-pages",
                        "--add-modules",
                        "m",
                        "--add-modules",
                        "jdk.localedata",
                        "--include-locales",
                        "en",
                        "--verbose");
    }

    @Test
    void sourceJdkModulesPointsToItsJmodsFolder(@TempDir Path jdkHome) {
        JLinkExecutor executor = new JLinkExecutor(null, Mockito.mock(Log.class));
        Optional<File> jmods = executor.getJmodsFolder(jdkHome.toFile());
        assertThat(jmods).contains(new File(jdkHome.toFile(), "jmods"));
    }

    @Test
    void withoutSourceJdkModulesNoJmodsFolderIsSet() {
        JLinkExecutor executor = new JLinkExecutor(null, Mockito.mock(Log.class));
        assertThat(executor.getJmodsFolder(null)).isEmpty();
    }
}
