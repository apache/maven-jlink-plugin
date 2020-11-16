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

boolean result = false;

try
{
    File buildLog = new File( basedir, "build.log" );
    if ( !buildLog.exists() || buildLog.isDirectory() )
    {
        System.err.println( "build.log file is missing or is a directory." );
        return false;
    }

    def line = buildLog.eachLine { line ->
        if (line.contains('--include-locales, en,ja,de,*-IN'))
        {
            result = true;
        }
    }
}
catch( Throwable e )
{
    e.printStackTrace();
}

return result;
