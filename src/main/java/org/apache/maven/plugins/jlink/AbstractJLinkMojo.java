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
import java.util.Collection;
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
     * Specify the requirements for this jdk toolchain. This overrules the toolchain selected by the
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

    private ToolchainManager toolchainManager;

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
     * Returns the archive file to generate, based on an optional classifier.
     *
     * @param basedir the output directory
     * @param finalName the name of the ear file
     * @param classifier an optional classifier
     * @param archiveExt The extension of the file.
     * @return the file to generate
     */
    protected File getArchiveFile(File basedir, String finalName, String classifier, String archiveExt) {
        if (basedir == null) {
            throw new IllegalArgumentException("basedir is not allowed to be null");
        }
        if (finalName == null) {
            throw new IllegalArgumentException("finalName is not allowed to be null");
        }
        if (archiveExt == null) {
            throw new IllegalArgumentException("archiveExt is not allowed to be null");
        }

        if (finalName.isEmpty()) {
            throw new IllegalArgumentException("finalName is not allowed to be empty.");
        }
        if (archiveExt.isEmpty()) {
            throw new IllegalArgumentException("archiveExt is not allowed to be empty.");
        }

        StringBuilder fileName = new StringBuilder(finalName);

        if (hasClassifier(classifier)) {
            fileName.append("-").append(classifier);
        }

        fileName.append('.');
        fileName.append(archiveExt);

        return new File(basedir, fileName.toString());
    }

    protected boolean hasClassifier(String classifier) {
        boolean result = false;
        if (classifier != null && !classifier.isEmpty()) {
            result = true;
        }

        return result;
    }

    /**
     * This will convert a module path separated by either {@code :} or {@code ;} into a string which uses the platform
     * depend path separator uniformly.
     *
     * @param pluginModulePath The module path.
     * @return The platform separated module path.
     */
    protected StringBuilder convertSeparatedModulePathToPlatformSeparatedModulePath(String pluginModulePath) {
        StringBuilder sb = new StringBuilder();
        // Split the module path by either ":" or ";" linux/windows path separator and
        // convert uniformly to the platform used separator.
        String[] splitModule = pluginModulePath.split("[;:]");
        for (String module : splitModule) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparatorChar);
            }
            sb.append(module);
        }
        return sb;
    }

    /**
     * Convert a list into a string which is separated by platform depend path separator.
     *
     * @param modulePaths The list of elements.
     * @return The string which contains the elements separated by {@link File#pathSeparatorChar}.
     */
    protected String getPlatformDependSeparateList(Collection<String> modulePaths) {
        return String.join(Character.toString(File.pathSeparatorChar), modulePaths);
    }

    /**
     * Convert a list into a
     * @param modules The list of modules.
     * @return The string with the module list which is separated by {@code ,}.
     */
    protected String getCommaSeparatedList(Collection<String> modules) {
        return String.join(",", modules);
    }
}
