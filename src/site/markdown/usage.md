<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
# Usage

## Introduction

The Maven JLink plugin is used to create [Modular Run-Time Images](https://openjdk.java.net/jeps/220) with JDK 9.

The intended artifacts to be linked together into a Modular Run-Time Image are the **jmod** and **jar** files. JMod files can be created by using the [Maven JMod Plugin](../maven-jmod-plugin/) and **jar** files can be created by using the [Maven JAR Plugin](../maven-jar-plugin/).

## Configuration of the Maven JLink Plugin

To use the Maven JLink Plugin you have to configure it as an `extensions` which means the configuration in your pom file has to look like this:

```
<project>
  [...]
  <build>
    [...]
    <plugins>
      [...]
      <plugin>
        <artifactId>maven-jlink-plugin</artifactId>
        <version>${project.version}</version>
        <extensions>true</extensions>
        <configuration>
          <!-- configuration elements goes here -->
        </configuration>
      </plugin>
   [...]
</project>
```

The configuration element contains the configuration for the plugin [like any other Maven plugin](https://maven.apache.org/guides/mini/guide-configuring-plugins.html). The different elements which can be configured for this plugin can identified by the [goals documentation](./plugin-info.html).

## Requirements

Based on the foundation of the plugin it is required to have JDK 11 or newer installed. This means either you have it configured via **JAVA_HOME** which means to run the whole Maven build with JDK 11+ or via **Toolchains**.

Howto configure Maven related to Toolchains can be read in the [Toolchains documentation](https://maven.apache.org/guides/mini/guide-using-toolchains.html).

## Usage of the Maven JLink Plugin

You can use the maven-jlink-plugin the same way you use the maven-jar-plugin, maven-war-plugin, maven-assembly-plugin or any other artifact-generating plugin. Create a separate module for the artifact generation and specify `<packaging>jlink</packaging>`.

Another way to use this plugin is to add it to an existing project. In this case you want to add an explicit execution in the `package` phase.

Let’s assume you have a multi module structure that contains two modules **mod-1** and **mod-2** which you like to put into the resulting Run Time Image.

The parent of the multi module looks similar like this: 

```
<project ...>
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.corporate.maven</groupId>
    <artifactId>maven-parent</artifactId>
    <version>2.3.1</version>
  </parent>
  <groupId>com.corporate.project</groupId>
  <artifactId>parent</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>pom</packaging>
   [...]
  <modules>
    <module>mod-1</module>
    <module>mod-2</module>
  </modules>  
   [...]
</project>
```

A directory structure of such a project looks like this:

```
.
├── mod-1
│   └── src
└── mod-2
    └── src
```

The **mod-1** module looks similar like this:

```
<project ...>
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.corporate.project</groupId>
    <artifactId>parent</artifactId>
    <version>1.0-SNAPSHOT</version>
  </parent>
  <artifactId>mod-1</artifactId>
   [...]
</project>
```

The **mod-2** module looks similar like this:

```
<project ...>
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.corporate.project</groupId>
    <artifactId>parent</artifactId>
    <version>1.0-SNAPSHOT</version>
  </parent>
  <artifactId>mod-2</artifactId>
   [...]
</project>
```

If you like to create a Java Run Time Image of your modules you have to create a separate module **mod-jlink** which contains the configuration to create the Run Time Image which looks similar like this:

```
<project ...>
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.corporate.project</groupId>
    <artifactId>parent</artifactId>
    <version>1.0-SNAPSHOT</version>
  </parent>
  <packaging>jlink</packaging>
  <artifactId>mod-jlink</artifactId>
   [...]
</project>
```

The directory structure now looks like this:

```
.
├── mod-1
│   └── src
├── mod-2
│   └── src
└── mod-jlink
    └── src
```

Before you can do this you have to add the configuration to the parent like shown in [Configuration of the Maven JLink Plugin](Configuration\_of\_the\_Maven\_JLink\_Plugin).

Now you need to define which modules should be linked into the resulting Java Run Time Image which simply can be done by defining the modules as dependencies to your **mod-jlink** module like this:

```
<project ...>
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.corporate.project</groupId>
    <artifactId>parent</artifactId>
    <version>1.0-SNAPSHOT</version>
  </parent>
  <packaging>jlink</packaging>
  <artifactId>mod-jlink</artifactId>
 <dependencies>
    <dependency>
      <groupId>com.corporate.project</groupId>
      <artifactId>mod-1</artifactId>
      <version>\${project.version}</version>
    </dependency>
    <dependency>
      <groupId>com.corporate.project</groupId>
      <artifactId>mod-2</artifactId>
      <version>\${project.version}</version>
    </dependency>
  </dependencies>  
   [...]
</project>
```

After you have added the appropriate configuration you can simply create the Java Run Time Image by calling from the root of your multi module project like this:

```
mvn clean package
```

There are some output lines similar like this:

```
[INFO]
[INFO] --- maven-jlink-plugin:${project.version}:jlink (default-jlink) @ mod-jlink ---
[INFO] Toolchain in maven-jlink-plugin: jlink [ /.../openjdk/21/libexec/openjdk.jdk/Contents/Home/Contents/Home/bin/jlink ]
[INFO] The following dependencies will be linked into the runtime image:
[INFO]  -> module: com.soebes.nine.one.jar ( /.../mod-1/target/mod-1-1.0-SNAPSHOT.jar )
[INFO]  -> module: com.soebes.nine.two.jar ( /.../mod-2/target/mod-2-1.0-SNAPSHOT.jar )
[INFO] Building zip: /.../mod-jlink/target/mod-jlink-1.0-SNAPSHOT.zip
[INFO]
```

If you like to install the resulting Java Run Time Image files into your local cache you can achieve this by using:

```
mvn clean install
```

or if you like to deploy the resulting artifacts to a remote repository you have to use:

```
mvn clean deploy
```

At the moment the resulting Java Run Time Image is packaged into a **zip** archive which used to transport the whole structure which is created by **jlink** to a repository.

The resulting [Installed Directory Structure of JDK](https://docs.oracle.com/en/java/javase/11/install/installed-directory-structure-jdk.html) looks like this:

```
jlink/
├── bin
├── conf
├── include
├── legal
├── lib
└── release
```

