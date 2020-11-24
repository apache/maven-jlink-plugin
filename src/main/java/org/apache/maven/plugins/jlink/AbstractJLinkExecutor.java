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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

abstract class AbstractJLinkExecutor
{
    protected static final String JMODS = "jmods";

    private final Log log;

    private final List<String> modulesToAdd = new ArrayList<>();
    private final List<String> modulePaths = new ArrayList<>();

    AbstractJLinkExecutor( Log log )
    {
        this.log = log;
    }

    protected Log getLog()
    {
        return this.log;
    }

    public abstract Optional<File> getJmodsFolder( /* nullable */ File sourceJdkModules );

    public abstract int executeJlink( List<String> jlinkArgs ) throws MojoExecutionException;

    public void addAllModules( Collection<String> modulesToAdd )
    {
        this.modulesToAdd.addAll( modulesToAdd );
    }

    public void addAllModulePaths( Collection<String> pathsOfModules )
    {
        this.modulePaths.addAll( pathsOfModules );
    }
}
