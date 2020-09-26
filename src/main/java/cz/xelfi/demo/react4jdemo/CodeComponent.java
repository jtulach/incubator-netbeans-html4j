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

import net.java.html.react.React;
import net.java.html.react.React.Element;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.java.html.js.JavaScriptBody;
import net.java.html.json.Model;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;

@Model(className = "LoadingUrl", builder = "with", properties = {
    @Property(name = "name", type = String.class),
    @Property(name = "url", type = String.class),
    @Property(name = "code", type = String.class),
})
final class CodeComponent extends React.Component<LoadingUrl> {
    private static final Object CLAZZ = React.register("CodeComponent", CodeComponent::new);
    private static final String REMOTE = "https://raw.githubusercontent.com/jtulach/netbeans-html4j/react4jdemo/src/main/java/cz/xelfi/demo/react4jdemo/";
    private static final String VIEW = "https://github.com/jtulach/netbeans-html4j/blob/react4jdemo/src/main/java/cz/xelfi/demo/react4jdemo/";

    CodeComponent(React.Props props) {
        super(props);
        LoadingUrl loadUrl = props.as(LoadingUrl.class);
        loadUrl.loadCode(loadUrl.getUrl(), this);
        setState(loadUrl);
    }

    @OnReceive(url = "{url}", onError = "loadCodeFailed")
    static void loadCode(LoadingUrl model, String code, CodeComponent component) {
        if (code.startsWith("/*")) {
            int commentEnd = code.indexOf("*/");
            if (commentEnd != -1) {
                code = code.substring(commentEnd + 2);
            }
        }
        component.setState(model.clone().withCode(code));
    }

    static void loadCodeFailed(LoadingUrl model, Throwable ex, CodeComponent component) {
        if (model.getUrl().startsWith(REMOTE)) {
            model.setUrl(ex.getMessage());
        } else {
            model.setUrl(REMOTE + model.getName());
            model.loadCode(model.getUrl(), component);
        }
        component.setState(model);
    }

    @Override
    protected Element render() {
        Element content;
        if (state().getCode() == null) {
            content = React.createElement("div", null, React.createText("Loading " + state().getUrl()));
        } else {
            content = boldJavaKeywords(state().getCode(), Collections.emptyMap(), Collections.emptySet());
        }
        return React.createElement("div", null,
            React.createElement("table", React.props("width", "100%"),
                React.createElement("tr", null,
                    React.createElement("td", React.props("align", "center"),
                        React.createElement("a", React.props("target", "_blank", "href", VIEW + state().getName()),
                            React.createText(state().getName())
                        )
                    )
                )
            ),
            content
        );
    }


    @JavaScriptBody(args = {  }, body = "return window.location.href;")
    private static native String homePageUrl();

    static void loadCode(String code) {
        try {
            URL url = new URL(new URL(homePageUrl()), code);
            Element codeComponent = React.createElement(CLAZZ, new LoadingUrl().
                withUrl(url.toExternalForm()).
                withName(code)
            );
            React.render(codeComponent, "codecontainer");
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }
    private static final Pattern WORDS = Pattern.compile("(\\w+)|(//.*)\n|(\"[^\"]*\")");
    static Element boldJavaKeywords(String text, Map<String,String> imports, Set<String> packages) {
        List<Element> children = new ArrayList<>();
        int prev = 0;
        Matcher m = WORDS.matcher(text);
        while (m.find()) {
            if (m.start() > prev) {
                children.add(React.createText(text.substring(prev, m.start())));
            }
            prev = m.end();

            Element append;
            switch (m.group(0)) {
                case "abstract":
                case "assert":
                case "boolean":
                case "break":
                case "byte":
                case "case":
                case "catch":
                case "class":
                case "const":
                case "continue":
                case "default":
                case "do":
                case "double":
                case "else":
                case "enum":
                case "extends":
                case "final":
                case "finally":
                case "float":
                case "for":
                case "goto":
                case "char":
                case "if":
                case "implements":
                case "import":
                case "instanceof":
                case "int":
                case "interface":
                case "long":
                case "native":
                case "new":
                case "package":
                case "private":
                case "protected":
                case "public":
                case "return":
                case "short":
                case "static":
                case "strictfp":
                case "super":
                case "switch":
                case "synchronized":
                case "this":
                case "throw":
                case "throws":
                case "transient":
                case "try":
                case "void":
                case "volatile":
                case "while":
                case "true":
                case "false":
                case "null":
                    append = React.createElement("b", null, React.createText(m.group(0)));
                    break;
                default:
                    if (m.group(0).startsWith("//")) {
                        append = React.createElement("em", null, React.createText(m.group(0).substring(0, m.group(0).length() - 1)));
                        break;
                    }
                    if (m.group(0).startsWith("\"")) {
                        append = React.createElement("em", null, React.createText(m.group(0)));
                        break;
                    }
                    String fqn;
                    fqn = imports.get(m.group(0));
                    if (fqn == null) {
                        fqn = tryLoad("java.lang", m.group(0));
                        if (fqn == null && packages != null) {
                            for (String p : packages) {
                                fqn = tryLoad(p, m.group(0));
                                if (fqn != null) {
                                    break;
                                }
                            }
                        }
                    }
                    if (fqn == null) {
                        append = React.createText(m.group(0));
                    } else {
                        append = React.createText("{@link " + fqn + "}");
                    }
            }
            children.add(append);
        }
        if (prev < text.length()) {
            children.add(React.createText(text.substring(prev)));
        }
        return React.createElement("pre", null, children.toArray(new Element[0]));
    }

    private static String tryLoad(String pkg, String name) {
        return null;
    }
}
