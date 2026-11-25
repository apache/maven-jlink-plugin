
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

File target = new File(basedir, "app/target")
assert target.isDirectory()

File jarArtifact = new File(target, "maven-jlink-plugin-mjlink-9-app-96.0.jar")
assert jarArtifact.isFile()

File distTarget = new File(basedir, "dist/target")
assert distTarget.isDirectory()

File artifact = new File(distTarget, "maven-jlink-plugin-mjlink-9-dist-96.0.zip")
assert artifact.isFile()

// optional: check if it contains a launcher script
