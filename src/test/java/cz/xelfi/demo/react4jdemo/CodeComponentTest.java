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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BrowserRunner.class)
public class CodeComponentTest {

    public CodeComponentTest() {
    }

    @JavaScriptBody(args = { "element" }, body = ""
            + "let dom = document.createElement('div');\n"
            + "document.body.appendChild(dom);\n"
            + "ReactDOM.render(element, dom)\n"
            + "return dom.innerHTML;\n"
            + "\n"
    )
    private static native String renderAndReturn(Object element);

    @Test
    public void boldMainMethod() throws Exception {
        String code = "public static void main(String... args) {}";
        Object result = CodeComponent.boldJavaKeywords(code, Collections.emptyMap(), Collections.emptySet());
        String innerHTML = renderAndReturn(result);
        Assert.assertEquals("<pre><b>public</b> <b>static</b> <b>void</b> main(String... args) {}</pre>", innerHTML);
    }

    @Test
    public void boldClassDefinition() throws Exception {
        String code = ""
                + "public class Main {\n"
                + "  public static final String x = new String();\n"
                + "}\n";
        Object result = CodeComponent.boldJavaKeywords(code, Collections.emptyMap(), Collections.emptySet());
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
