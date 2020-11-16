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

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.toolchain.Toolchain;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * JDK 8-only Jlink executor.
 *
 * <p>As JDK8 does not ship jlink, a toolchain is required.</p>
 */
class JLinkExecutor extends AbstractJLinkExecutor
{
    private final String jLinkExec;

    JLinkExecutor( Toolchain toolchain, Log log ) throws IOException
    {
        super( toolchain, log );
        this.jLinkExec = getJLinkExecutable();
    }

    public File getJlinkExecutable()
    {
        return new File( this.jLinkExec );
    }

    @Override
    public Optional<File> getJmodsFolder( /* nullable */ File sourceJdkModules )
    {
        // Really Hacky...do we have a better solution to find the jmods directory of the JDK?
        File jLinkParent = getJlinkExecutable().getParentFile().getParentFile();
        File jmodsFolder;
        if ( sourceJdkModules != null && sourceJdkModules.isDirectory() )
        {
            jmodsFolder = new File( sourceJdkModules, JMODS );
        }
        else
        {
            jmodsFolder = new File( jLinkParent, JMODS );
        }

        getLog().debug( " Parent: " + jLinkParent.getAbsolutePath() );
        getLog().debug( " jmodsFolder: " + jmodsFolder.getAbsolutePath() );

        return Optional.of( jmodsFolder );
    }

    /**
     * Execute JLink via any means.
     *
     * @return the exit code ({@code 0} on success).
     */
    @Override
    public int executeJlink( File argsFile )
    {
        getLog().info( "Toolchain in maven-jlink-plugin: jlink [ " + this.jLinkExec + " ]" );

        Commandline cmd = createJLinkCommandLine( argsFile );
        cmd.setExecutable( this.jLinkExec );

        throw new UnsupportedOperationException( "not implemented" );
    }

    private Commandline createJLinkCommandLine( File argsFile )
    {
        Commandline cmd = new Commandline();
        cmd.createArg().setValue( '@' + argsFile.getAbsolutePath() );

        return cmd;
    }


    protected final String getJLinkExecutable() throws IOException
    {
        if ( getToolchain() == null )
        {
            getLog().error( "Either JDK9+ or a toolchain "
                    + "pointing to a JDK9+ containing a jlink binary is required." );
            getLog().info( "See https://maven.apache.org/guides/mini/guide-using-toolchains.html "
                    + "for mor information." );
            throw new IllegalStateException( "Running on JDK8 and no toolchain found." );
        }

        String jLinkExecutable = getToolchain().findTool( "jlink" );

        if ( StringUtils.isEmpty( jLinkExecutable ) )
        {
            throw new IOException( "The jlink executable '" + jLinkExecutable + "' doesn't exist or is not a file." );
        }

        // TODO: Check if there exist a more elegant way?
        String jLinkCommand = "jlink" + ( SystemUtils.IS_OS_WINDOWS ? ".exe" : "" );

        File jLinkExe = new File( jLinkExecutable );

        if ( jLinkExe.isDirectory() )
        {
            jLinkExe = new File( jLinkExe, jLinkCommand );
        }

        if ( SystemUtils.IS_OS_WINDOWS && jLinkExe.getName().indexOf( '.' ) < 0 )
        {
            jLinkExe = new File( jLinkExe.getPath() + ".exe" );
        }

        if ( !jLinkExe.isFile() )
        {
            throw new IOException( "The jlink executable '" + jLinkExe + "' doesn't exist or is not a file." );
        }
        return jLinkExe.getAbsolutePath();
    }
}
