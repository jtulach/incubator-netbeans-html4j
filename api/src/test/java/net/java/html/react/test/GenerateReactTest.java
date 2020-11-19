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
package net.java.html.react.test;

import net.java.html.junit.BrowserRunner;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import net.java.html.junit.HTMLContent;
import net.java.html.react.React;
import net.java.html.react.RegisterComponent;
import net.java.html.react.Render;
import static org.junit.Assert.assertEquals;
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

        @Render(
        "  <div class='empty'>\n" +
        "    <a href='{url}'>Link</a>\n" +
        "  </div>"
        )
        protected abstract React.Element someAttrs(String url);

        @Render(
        "  <div class='empty'>\n" +
        "    <button id='clickButton' onClick='{call}'>Press {name}!</button>\n" +
        "  </div>"
        )
        protected abstract React.Element callback(String name, Runnable call);

        @Render(
            "<input value='{i}' onChange='{ch}'/>"
        )
        protected abstract React.Element input(int i, Runnable ch);
 
        @Render(
            "<div>{this.input(i, null)}</div>"
        )
        protected abstract React.Element divInput(int i, Runnable ch);
        
        @Render(
            "<div><p>{this.getInt()}</p><p>{this.getString()}</p></div>"
        )
        protected abstract React.Element methods();
        
        protected int getInt() {
            return 42;
        }
        
        protected String getString() {
            return "Good to see React4J working!";
        }
        
        @Render(
            "<div><input type='text' size='{this.getInt()}' value='{this.getString()}' /></div>"
        )
        protected abstract React.Element attributes();
    }

    @Test
    public void renderNoArgs() throws Exception {
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

    @Test
    public void someAttrs() throws Exception {
        React.Element element = new GenerateReactRender(null).someAttrs("http://netbeans.org");
        assertNotNull("Element has been generated", element);
        React.render(element, "mocknode");

        String text = innerHTML("mocknode");
        assertTrue(text, text.contains("netbeans.org"));
    }

    @Test
    public void callbackArgument() throws Exception {
        int[] cnt = { 0 };
        Runnable onClick = () -> {
            cnt[0]++;
        };

        React.Element element = new GenerateReactRender(null).callback("the button", onClick);
        assertNotNull("Element has been generated", element);
        React.render(element, "mocknode");

        String text = innerHTML("mocknode");
        assertTrue(text, text.contains("Press the button!"));
        doClick("clickButton");

        assertEquals("Counter incremented", 1, cnt[0]);
    }

    @Test
    public void input() throws Exception {
        React.Element element = new GenerateReactRender(null).input(3, () -> {});
        assertNotNull("Element has been generated", element);
        React.render(element, "mocknode");

        String text = innerHTML("mocknode");

        assertTrue(text, text.contains("input value=\"3\""));
    }

    @Test
    public void divIput() throws Exception {
        React.Element element = new GenerateReactRender(null).divInput(3, () -> {});
        assertNotNull("Element has been generated", element);
        React.render(element, "mocknode");

        String text = innerHTML("mocknode");
        assertTrue(text, text.startsWith("<div"));
        assertTrue(text, text.contains("input value=\"3\""));
        assertTrue(text, text.endsWith("</div>"));
    }
    
    @Test
    public void methods() throws Exception {
        React.Element element = new GenerateReactRender(null).methods();
        assertNotNull("Element has been generated", element);
        React.render(element, "mocknode");

        String text = innerHTML("mocknode");
        assertTrue(text, text.startsWith("<div"));
        assertTrue(text, text.contains("Good to see React4J working!"));
        assertTrue(text, text.contains("42"));
        assertTrue(text, text.endsWith("</div>"));
    }
    
    @Test
    public void attributes() throws Exception {
        React.Element element = new GenerateReactRender(null).attributes();
        assertNotNull("Element has been generated", element);
        React.render(element, "mocknode");

        String text = innerHTML("mocknode");
        assertTrue(text, text.startsWith("<div><input"));
        assertTrue(text, text.contains("value=\"Good to see React4J working!\""));
        assertTrue(text, text.contains("size=\"42\""));
        assertTrue(text, text.endsWith("</div>"));
    }

    private static String innerHTML(String id) throws Exception {
        Fn.Presenter p = Fn.activePresenter();
        assertNotNull("Presenter is active", p);
        return (String) p.defineFn("return document.getElementById('" + id + "').innerHTML").invoke(null);
    }

    private static String doClick(String id) throws Exception {
        Fn.Presenter p = Fn.activePresenter();
        assertNotNull("Presenter is active", p);
        return (String) p.defineFn("document.getElementById('" + id + "').click();").invoke(null);
    }
}
