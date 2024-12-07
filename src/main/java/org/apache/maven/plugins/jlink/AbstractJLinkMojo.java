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
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.Toolchain;
import org.apache.maven.toolchain.ToolchainManager;

/**
 * @author Karl Heinz Marbaise <a href="mailto:khmarbaise@apache.org">khmarbaise@apache.org</a>
 */
public abstract class AbstractJLinkMojo extends AbstractMojo {
    /**
     * <p>
     * Specify the requirements for this JDK toolchain. This overrules the toolchain selected by the
     * maven-toolchain-plugin.
     * </p>
     * <strong>note:</strong> requires at least Maven 3.3.1
     */
    @Parameter
    private Map<String, String> jdkToolchain;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    private final ToolchainManager toolchainManager;

    public AbstractJLinkMojo(ToolchainManager toolchainManager) {
        this.toolchainManager = toolchainManager;
    }

    /**
     * Overload this to produce a zip with another classifier, for example a jlink-zip.
     * @return get the classifier.
     */
    protected abstract String getClassifier();

    protected JLinkExecutor getJlinkExecutor() {
        return new JLinkExecutor(getToolchain().orElse(null), getLog());
    }

    protected Optional<Toolchain> getToolchain() {
        Toolchain tc = null;

        if (jdkToolchain != null) {
            // Maven 3.3.1 has plugin execution scoped Toolchain Support
            try {
                Method getToolchainsMethod = toolchainManager
                        .getClass()
                        .getMethod("getToolchains", MavenSession.class, String.class, Map.class);

                @SuppressWarnings("unchecked")
                List<Toolchain> tcs = (List<Toolchain>)
                        getToolchainsMethod.invoke(toolchainManager, getSession(), "jdk", jdkToolchain);

                if (tcs != null && tcs.size() > 0) {
                    tc = tcs.get(0);
                }
            } catch (ReflectiveOperationException | SecurityException | IllegalArgumentException e) {
                // ignore
            }
        }

        if (tc == null) {
            // TODO: Check if we should make the type configurable?
            tc = toolchainManager.getToolchainFromBuildContext("jdk", getSession());
        }

        return Optional.ofNullable(tc);
    }

    protected MavenProject getProject() {
        return project;
    }

    protected MavenSession getSession() {
        return session;
    }

    /**
     * This will convert a module path separated by either {@code :} or {@code ;} into a string which uses the platform
     * path separator uniformly.
     *
     * @param pluginModulePath the module path
     * @return the platform separated module path
     */
    protected StringBuilder convertSeparatedModulePathToPlatformSeparatedModulePath(String pluginModulePath) {
        StringBuilder sb = new StringBuilder();
        // Split the module path by either ":" or ";" Linux/Windows path separator and
        // convert uniformly to the platform separator.
        String[] splitModule = pluginModulePath.split("[;:]");
        for (String module : splitModule) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparatorChar);
            }
            sb.append(module);
        }
        return sb;
    }
}
