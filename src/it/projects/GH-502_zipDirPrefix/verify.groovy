
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

def isWindows = System.getProperty('os.name').startsWith('Windows')
def base = basedir.toString() + '/target'
def archive = base + '/maven-jlink-plugin-gh502-42.0.0.zip'
def unzipCommand = isWindows
                     ? ['powershell.exe', 'Expand-Archive', '-Path', archive, '-DestinationPath', base]
                     : ['unzip', '-d', base, archive]
unzipCommand.execute().waitFor()

def testCommand = base + '/prefix-test/bin/helloworld'
if (isWindows) {
  testCommand += '.bat'
}
def testProc = testCommand.execute()
testProc.waitFor()
assert testProc.text.trim() == 'Hello World'
