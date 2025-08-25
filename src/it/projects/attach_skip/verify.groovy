
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

import java.io.*;
import java.util.*;
import java.util.jar.*;
import org.codehaus.plexus.util.*;

try
{
    File target = new File( basedir, "target" )
    if ( !target.exists() || !target.isDirectory() ) {
        System.err.println( "target file is missing or not a directory." )
        return false
    }

    // Check script variable was passed correctly from parent POM
    if (!binding.hasVariable('localRepoStr') || localRepoStr == null || localRepoStr.isEmpty()) {
        System.err.println("localRepoStr script variable not set or null.")
        return false
    }

    // Get local repository path from script variable and validate it exists
    File localRepo = new File(localRepoStr)
    System.out.println("[INFO] localRepo set to: " + localRepo.getAbsolutePath())

    if ( !localRepo.exists() || !localRepo.isDirectory() ) {
        System.err.println("localRepo not set to a valid directory.")
        return false
    }


    // Check that output artifacts have been created
    File artifact1 = new File( target, "maven-jlink-plugin-it-attach-skip-1.0-jlink1.zip" )
    if ( !artifact1.exists() || artifact1.isDirectory() ) {
        System.err.println( "maven-jlink-plugin-it-attach-skip-1.0-jlink1.zip file is missing or is a directory." )
        return false
    }

    File artifact2 = new File( target, "maven-jlink-plugin-it-attach-skip-1.0.zip" )
    if ( !artifact2.exists() || artifact2.isDirectory() ) {
        System.err.println( "maven-jlink-plugin-it-attach-skip-1.0.zip file is missing or is a directory." )
        return false
    }

    File artifact3 = new File( target, "maven-jlink-plugin-it-attach-skip-1.0-jlink3.zip" )
    if ( !artifact3.exists() || artifact3.isDirectory() ) {
        System.err.println( "maven-jlink-plugin-it-attach-skip-1.0-jlink3.zip file is missing or is a directory." )
        return false
    }


    // Check that the artifacts' installation status is as expected based on the attach parameter
    installedArtifact1 = new File( localRepo, "org/apache/maven/plugins/maven-jlink-plugin-it-attach-skip/1.0/maven-jlink-plugin-it-attach-skip-1.0-jlink1.zip" )
    if ( installedArtifact1.exists() ) {
        System.err.println( "maven-jlink-plugin-it-attach-skip-1.0-jlink1.zip WAS installed when attach parameter set to FALSE." )
        return false
    }

    installedArtifact2 = new File( localRepo, "org/apache/maven/plugins/maven-jlink-plugin-it-attach-skip/1.0/maven-jlink-plugin-it-attach-skip-1.0.zip" )
    if ( installedArtifact2.exists() ) {
        System.err.println( "maven-jlink-plugin-it-attach-skip-1.0.zip WAS installed when attach parameter set to FALSE." )
        return false
    }

    installedArtifact3 = new File( localRepo, "org/apache/maven/plugins/maven-jlink-plugin-it-attach-skip/1.0/maven-jlink-plugin-it-attach-skip-1.0-jlink3.zip" )
    if ( !installedArtifact3.exists() ) {
        System.err.println( "maven-jlink-plugin-it-attach-skip-1.0-jlink3.zip NOT installed when attach parameter set to TRUE." )
        return false
    }

    return true
}
catch( Throwable e )
{
    e.printStackTrace()
    return false
}
