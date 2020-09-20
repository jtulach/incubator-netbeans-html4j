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
package cz.xelfi.demo.react4jdemo.javasx;

import cz.xelfi.demo.react4jdemo.api.React;
import cz.xelfi.demo.react4jdemo.api.RegisterComponent;
import net.java.html.junit.BrowserRunner;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import cz.xelfi.demo.react4jdemo.api.Render;
import net.java.html.junit.HTMLContent;
import static org.junit.Assert.assertTrue;
import org.netbeans.html.boot.spi.Fn;

@RunWith(BrowserRunner.class)
@HTMLContent(value = "\n"
    + "<div id='mocknode'/>\n"
)
public class GenerateReactTest {
    @RegisterComponent(name = "GenerateReactRender")
    static abstract class RenderTest {
        RenderTest(React.Props p) {
        }

        @Render(
        "  <div class='empty'>\n" +
        "    <h1>Hello!</h1>\n" +
        "    <h2>Good to see React4J working!</h2>\n" +
        "  </div>"
        )
        protected abstract React.Element noArgs();

        @Render(
        "  <div class='empty'>\n" +
        "    <h1>Hello, {name}!</h1>\n" +
        "    <h2>Good to see it working {times}x times!</h2>\n" +
        "  </div>"
        )
        protected abstract React.Element someArgs(String name, int times);
    }

    @Test
    public void divH1H2() throws Exception {
        React.Element element = new GenerateReactRender(null).noArgs();
        assertNotNull("Element has been generated", element);
        React.render(element, "mocknode");

        String text = innerHTML("mocknode");
        assertTrue(text, text.contains("Good to see React4J working!"));
    }

    @Test
    public void someArguments() throws Exception {
        React.Element element = new GenerateReactRender(null).someArgs("guys", 2);
        assertNotNull("Element has been generated", element);
        React.render(element, "mocknode");

        String text = innerHTML("mocknode");
        assertTrue(text, text.contains("Hello, guys!"));
        assertTrue(text, text.contains("working 2x times!"));
    }

    private static String innerHTML(String id) throws Exception {
        Fn.Presenter p = Fn.activePresenter();
        assertNotNull("Presenter is active", p);
        return (String) p.defineFn("return document.getElementById('" + id + "').innerHTML").invoke(null);
    }
}
