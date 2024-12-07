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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MultipleLauncherTest {

    private JLinkMojo mojo = new JLinkMojo(null, null, null, null);

    @Test
    void testSingleLauncher() throws Exception {
        // It's OK to specify one launcher with "<launcher>" given
        Field launcher = mojo.getClass().getDeclaredField("launcher");
        launcher.setAccessible(true);
        launcher.set(mojo, "l=com.example.Launch");

        // when
        List<String> jlinkArgs = mojo.createJlinkArgs(List.of(), List.of());

        // then
        assertThat(launched(jlinkArgs)).contains("l=com.example.Launch");
    }

    @Test
    void testOneMultipleLauncher() throws Exception {
        // It's OK to specify one launcher with "<launchers>"
        Field launchers = mojo.getClass().getDeclaredField("launchers");
        launchers.setAccessible(true);
        launchers.set(mojo, List.of("l=com.example.Launch"));

        // when
        List<String> jlinkArgs = mojo.createJlinkArgs(List.of(), List.of());

        // then
        assertThat(launched(jlinkArgs)).contains("l=com.example.Launch");
    }

    @Test
    void testMultipleLaunchers() throws Exception {
        // It's OK to specify multiple launchers with the "<launchers>" element
        Field launchers = mojo.getClass().getDeclaredField("launchers");
        launchers.setAccessible(true);
        launchers.set(mojo, List.of("l1=com.example.Launch1", "l2=com.example.Launch2"));

        // when
        List<String> jlinkArgs = mojo.createJlinkArgs(List.of(), List.of());

        // then
        assertThat(launched(jlinkArgs)).contains("l1=com.example.Launch1", "l2=com.example.Launch2");
    }

    @Test
    void testInvalidLauncherConfig() throws Exception {
        // It's an error to specify both "<launcher>" and "<launchers>"
        Field launcher = mojo.getClass().getDeclaredField("launcher");
        launcher.setAccessible(true);
        launcher.set(mojo, "l3=com.example.Launch3");
        Field launchers = mojo.getClass().getDeclaredField("launchers");
        launchers.setAccessible(true);
        launchers.set(mojo, List.of("l1=com.example.Launch1", "l2=com.example.Launch2"));

        // When
        assertThatThrownBy(() -> mojo.createJlinkArgs(List.of(), List.of())).isInstanceOf(MojoExecutionException.class);
    }

    // Helper function - gather all the classes named by --launcher args
    private static Set<String> launched(List<String> args) {
        Set<String> classNames = new HashSet<>();
        Iterator<String> iterator = args.iterator();
        while (iterator.hasNext()) {
            String arg = iterator.next();
            if ("--launcher".equals(arg)) {
                if (iterator.hasNext()) {
                    classNames.add(iterator.next());
                } else {
                    throw new IllegalStateException("--launcher arg with no classname");
                }
            }
        }
        return classNames;
    }
}
