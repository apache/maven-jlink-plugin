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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.utils.cli.CommandLineException;
import org.apache.maven.shared.utils.cli.CommandLineUtils;
import org.apache.maven.shared.utils.cli.Commandline;
import org.apache.maven.toolchain.Toolchain;

abstract class AbstractJLinkToolchainExecutor extends AbstractJLinkExecutor {
    private final Toolchain toolchain;

    AbstractJLinkToolchainExecutor(Toolchain toolchain, Log log) {
        super(log);
        this.toolchain = toolchain;
    }

    protected Optional<Toolchain> getToolchain() {
        return Optional.ofNullable(this.toolchain);
    }

    /**
     * Execute JLink via toolchain.
     *
     * @return the exit code ({@code 0} on success).
     */
    @Override
    public int executeJlink(List<String> jlinkArgs) throws MojoExecutionException {
        File jlinkExecutable = getJlinkExecutable();
        getLog().info("Toolchain in maven-jlink-plugin: jlink [ " + jlinkExecutable + " ]");
        Commandline cmd = createJLinkCommandLine(jlinkExecutable, jlinkArgs);

        return executeCommand(cmd);
    }

    private File getJlinkExecutable() {
        return new File(getJLinkExecutable());
    }

    @Override
    public Optional<File> getJmodsFolder(/* nullable */ File sourceJdkModules) {
        // Really Hacky...do we have a better solution to find the jmods directory of the JDK?
        File jLinkParent = getJlinkExecutable().getParentFile().getParentFile();
        File jmodsFolder;
        if (sourceJdkModules != null && sourceJdkModules.isDirectory()) {
            jmodsFolder = new File(sourceJdkModules, JMODS);
        } else {
            jmodsFolder = new File(jLinkParent, JMODS);
        }

        getLog().debug(" Parent: " + jLinkParent.getAbsolutePath());
        getLog().debug(" jmodsFolder: " + jmodsFolder.getAbsolutePath());

        return Optional.of(jmodsFolder);
    }

    static Commandline createJLinkCommandLine(File jlinkExecutable, List<String> jlinkArgs) {
        Commandline cmd = new Commandline();
        // Don't quote every argument with single quote, but instead quote them with double quotes
        // and enclose all of them with single quotes to then be passed to the shell command as
        // /bin/sh -c '<all arguments, each one quoted with double quotes>'
        cmd.getShell().setQuotedArgumentsEnabled(false);

        String jlinkArgsStr = jlinkArgs.stream().map(arg -> "\"" + arg + "\"").collect(Collectors.joining(" "));
        cmd.setExecutable(jlinkExecutable.getAbsolutePath() + " " + jlinkArgsStr);
        return cmd;
    }

    private String getJLinkExecutable() {
        Optional<Toolchain> toolchain = getToolchain();

        if (!toolchain.isPresent()) {
            getLog().error("Either JDK9+ or a toolchain "
                    + "pointing to a JDK9+ containing a jlink binary is required.");
            getLog().info("See https://maven.apache.org/guides/mini/guide-using-toolchains.html "
                    + "for mor information.");
            throw new IllegalStateException("Running on JDK8 and no toolchain found.");
        }

        String jLinkExecutable =
                toolchain.orElseThrow(NoSuchElementException::new).findTool("jlink");

        if (jLinkExecutable.isEmpty()) {
            throw new IllegalStateException(
                    "The jlink executable '" + jLinkExecutable + "' doesn't exist or is not a file.");
        }

        // TODO: Check if there exist a more elegant way?
        String jLinkCommand = "jlink" + (isOSWindows() ? ".exe" : "");

        File jLinkExe = new File(jLinkExecutable);

        if (jLinkExe.isDirectory()) {
            jLinkExe = new File(jLinkExe, jLinkCommand);
        }

        if (isOSWindows() && jLinkExe.getName().indexOf('.') < 0) {
            jLinkExe = new File(jLinkExe.getPath() + ".exe");
        }

        if (!jLinkExe.isFile()) {
            throw new IllegalStateException("The jlink executable '" + jLinkExe + "' doesn't exist or is not a file.");
        }
        return jLinkExe.getAbsolutePath();
    }

    private int executeCommand(Commandline cmd) throws MojoExecutionException {
        if (getLog().isDebugEnabled()) {
            // no quoted arguments ???
            getLog().debug(CommandLineUtils.toString(cmd.getCommandline()));
        }

        CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer out = new CommandLineUtils.StringStreamConsumer();
        try {
            int exitCode = CommandLineUtils.executeCommandLine(cmd, out, err);

            String output =
                    out.getOutput().isBlank() ? null : '\n' + out.getOutput().trim();

            if (exitCode != 0) {

                if (output != null && !output.isEmpty()) {
                    // Reconsider to use WARN / ERROR ?
                    //  getLog().error( output );
                    for (String outputLine : output.split("\n")) {
                        getLog().error(outputLine);
                    }
                }

                StringBuilder msg = new StringBuilder("\nExit code: ");
                msg.append(exitCode);
                if (!err.getOutput().isBlank()) {
                    msg.append(" - ").append(err.getOutput());
                }
                msg.append('\n');
                msg.append("Command line was: ").append(cmd).append('\n').append('\n');

                throw new MojoExecutionException(msg.toString());
            }

            if (output != null && !output.isEmpty()) {
                // getLog().info( output );
                for (String outputLine : output.split("\n")) {
                    getLog().info(outputLine);
                }
            }

            return exitCode;
        } catch (CommandLineException e) {
            throw new MojoExecutionException("Unable to execute jlink command: " + e.getMessage(), e);
        }
    }

    private static boolean isOSWindows() {
        String osName;
        try {
            osName = System.getProperty("os.name");
        } catch (final SecurityException ex) {
            // we are not allowed to look at this property
            return false;
        }
        if (osName == null) {
            return false;
        }
        return osName.startsWith("Windows");
    }
}
