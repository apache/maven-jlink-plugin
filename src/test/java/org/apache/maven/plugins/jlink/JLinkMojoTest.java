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

import java.lang.reflect.Field;
import java.util.List;

import org.apache.maven.plugin.MojoFailureException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class JLinkMojoTest {

    @Test
    void quote_every_argument() throws Exception {
        // given
        JLinkMojo mojo = new JLinkMojo();
        Field stripDebug = mojo.getClass().getDeclaredField("stripDebug");
        stripDebug.setAccessible(true);
        stripDebug.set(mojo, Boolean.TRUE);

        // when
        List<String> jlinkArgs = mojo.createJlinkArgs(List.of(), List.of());

        // then
        assertThat(jlinkArgs).noneMatch(arg -> arg.trim().isBlank());
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                "0,true",
                "1,true",
                "2,true",
                "3,false",
                "9,false",
                // empty string is not valid.
                "'',false",
                // null is valid (i.e. not set).
                ",true",
                "zip,false",
                "zip-0,true",
                "zip-6,true",
                "zip-9,true",
                "zip-10,false",
            })
    void accepts_only_valid_compression_levels(String compress, boolean acceptable) {
        try {
            JLinkMojo.checkCompressParameter(compress);
        } catch (MojoFailureException e) {
            if (acceptable) {
                Assertions.fail("Value [" + compress + "] should have been a valid value", e);
            }

            return;
        }

        if (!acceptable) {
            Assertions.fail("Value [" + compress + "] should not have been a valid value");
        }
    }
}
