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
package cz.xelfi.demo.react4jdemo;

import java.util.Collections;
import net.java.html.js.JavaScriptBody;
import net.java.html.junit.BrowserRunner;
import net.java.html.react.React;
import net.java.html.react.React.Element;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BrowserRunner.class)
public class CodeComponentTest {

    public CodeComponentTest() {
    }

    private static int counter;

    private static String renderAndReturn(Element element) {
        final String id = "cc-test-" + ++counter;
        Object div = createDiv(id);
        React.render(element, id);
        return readHtml(div);
    }

    @JavaScriptBody(args = { "id" }, body = ""
            + "let dom = document.createElement('div');\n"
            + "dom.setAttribute('id', id);\n"
            + "document.body.appendChild(dom);\n"
            + "return dom;\n"
            + "\n"
    )
    private static native Object createDiv(String id);

    @JavaScriptBody(args = { "dom" }, body = ""
            + "return dom.innerHTML;\n"
            + "\n"
    )
    private static native String readHtml(Object dom);

    @Test
    public void boldMainMethod() throws Exception {
        String code = "public static void main(String... args) {}";
        Element result = CodeComponent.boldJavaKeywords(code, Collections.emptyMap(), Collections.emptySet());
        String innerHTML = renderAndReturn(result);
        Assert.assertEquals("<pre><b>public</b> <b>static</b> <b>void</b> main(String... args) {}</pre>", innerHTML);
    }

    @Test
    public void boldClassDefinition() throws Exception {
        String code = ""
                + "public class Main {\n"
                + "  public static final String x = new String();\n"
                + "}\n";
        Element result = CodeComponent.boldJavaKeywords(code, Collections.emptyMap(), Collections.emptySet());
        String innerHTML = renderAndReturn(result);
        Assert.assertEquals(""
            + "<pre><b>public</b> <b>class</b> Main {\n"
            + "  <b>public</b> <b>static</b> <b>final</b> String x = <b>new</b> String();\n"
            + "}\n"
            + "</pre>",
            innerHTML
        );
    }
}
