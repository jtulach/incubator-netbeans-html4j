/**
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
package org.netbeans.html.presenters.render;

import java.net.URI;
import org.testng.annotations.Test;

public class ShowTest {

    public ShowTest() {
    }

    @Test
    public void showDefault() throws Exception {
     //   Show.show(null, new URI("http://netbeans.org"));
    }

    @Test
    public void showGTK() throws Exception {
     //   Show.show("GTK", new URI("http://netbeans.org"));
    }

    @Test
    public void showCocoa() throws Exception {
     //   Show.show("Cocoa", new URI("http://netbeans.org"));
    }
}
