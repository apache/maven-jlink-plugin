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

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.toolchain.Toolchain;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.spi.ToolProvider;

/**
 * JDK9+ executor for jlink.
 *
 * <p>This implementation uses the JDK9+ Toolprovider SPI to find and execute jlink.
 * This way, no fork needs to be created.</p>
 */
class JLinkExecutor extends AbstractJLinkExecutor
{

    private final ToolProvider toolProvider;

    JLinkExecutor( Toolchain toolchain, Log log ) throws IOException
    {
        super( toolchain, log );
        this.toolProvider = getJLinkExecutable();
    }

    @Override
    public Optional<File> getJmodsFolder( /* nullable */ File sourceJdkModules )
    {
        if ( sourceJdkModules != null && sourceJdkModules.isDirectory() )
        {
            return Optional.of( new File( sourceJdkModules, JMODS ) );
        }

        // ToolProvider does not need jmods folder to be set.
        return Optional.empty();
    }

    protected final ToolProvider getJLinkExecutable()
    {
        return ToolProvider
                .findFirst( "jlink" )
                .orElseThrow( () -> new IllegalStateException( "No jlink tool found." ) );
    }

    @Override
    public int executeJlink( List<String> jlinkArgs ) throws MojoExecutionException
    {
        if ( getLog().isDebugEnabled() )
        {
            // no quoted arguments ???
            getLog().debug( this.toolProvider.name() + " " + jlinkArgs );
        }

        try ( ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
              PrintWriter err = new PrintWriter( baosErr );
              ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
              PrintWriter out = new PrintWriter( baosOut ) )
        {
            int exitCode = this.toolProvider.run( out, err, jlinkArgs.toArray( new String[0] ) );
            out.flush();
            err.flush();

            String outAsString = baosOut.toString( "UTF-8" );
            String output = ( StringUtils.isEmpty( outAsString ) ? null : '\n' + outAsString.trim() );

            if ( exitCode != 0 )
            {
                if ( StringUtils.isNotEmpty( output ) )
                {
                    // Reconsider to use WARN / ERROR ?
                    //  getLog().error( output );
                    for ( String outputLine : output.split( "\n" ) )
                    {
                        getLog().error( outputLine );
                    }
                }

                StringBuilder msg = new StringBuilder( "\nExit code: " );
                msg.append( exitCode );
                String errAsString = baosErr.toString();
                if ( StringUtils.isNotEmpty( errAsString ) )
                {
                    msg.append( " - " ).append( errAsString );
                }
                msg.append( '\n' );
                msg.append( "Command line was: " ).append( this.toolProvider.name() ).append( ' ' ).append(
                        jlinkArgs ).append( '\n' ).append( '\n' );

                throw new MojoExecutionException( msg.toString() );
            }

            if ( StringUtils.isNotEmpty( output ) )
            {
                //getLog().info( output );
                for ( String outputLine : output.split( "\n" ) )
                {
                    getLog().info( outputLine );
                }
            }

            return exitCode;
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to execute jlink command: " + e.getMessage(), e );
        }
    }
}
