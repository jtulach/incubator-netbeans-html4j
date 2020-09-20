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

package cz.xelfi.demo.react4jdemo.javasx;

import cz.xelfi.demo.react4jdemo.api.RegisterComponent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import cz.xelfi.demo.react4jdemo.api.Render;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

@ServiceProvider(service = Processor.class)
public class JavaSxProcessor extends AbstractProcessor {
    private static final String EXP_ERR_NAME = "cz.xelfi.demo.react4jdemo.javasx.ExpectedError";

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> names = new HashSet<>();
        names.add(Render.class.getCanonicalName());
        return names;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    public @Override boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        Set<Element> expectedErrors = new HashSet<>();

        Map<TypeElement,Set<ExecutableElement>> annotatedElementsByClass = new HashMap<>();
        for (Element e : roundEnv.getElementsAnnotatedWith(Render.class)) {
            if (e.getKind() != ElementKind.METHOD) {
                continue;
            }
            final Element clazz = e.getEnclosingElement();
            if (clazz.getKind() != ElementKind.CLASS || clazz.getAnnotation(RegisterComponent.class) == null) {
                emitError(e, expectedErrors, "@Render in a class without @RegisterComponent");
                continue;
            }
            if (
                !e.getModifiers().contains(Modifier.ABSTRACT) ||
                !e.getModifiers().contains(Modifier.PROTECTED)
            ) {
                emitError(e, expectedErrors, "@Render method must be protected abstract");
                continue;
            }

            ExecutableElement method = (ExecutableElement) e;
            TypeMirror returnType = method.getReturnType();
            boolean ok = false;
            if (returnType.getKind() == TypeKind.DECLARED) {
                Element returnElement = processingEnv.getTypeUtils().asElement(returnType);
                Name returnBinName = processingEnv.getElementUtils().getBinaryName((TypeElement) returnElement);
                ok = "cz.xelfi.demo.react4jdemo.api.React$Element".equals(returnBinName.toString());
            }
            if (!ok) {
                emitError(e, expectedErrors, "@Render method must return React.Element");
                continue;
            }

            Set<ExecutableElement> allMethods = annotatedElementsByClass.get((TypeElement) clazz);
            if (allMethods == null) {
                allMethods = new HashSet<>();
                annotatedElementsByClass.put((TypeElement) clazz, allMethods);
            }
            allMethods.add(method);
        }

        for (Map.Entry<TypeElement, Set<ExecutableElement>> entry : annotatedElementsByClass.entrySet()) {
            RegisterComponent cReg = entry.getKey().getAnnotation(RegisterComponent.class);
            String pkg = findAndVerify(entry.getKey(), expectedErrors);
            if (pkg == null) {
                continue;
            }
            try {
                generateComponent(entry.getKey(), pkg, cReg.name(), entry.getValue());
            } catch (IOException ex) {
                emitError(entry.getKey(), expectedErrors, ex.getMessage() + " while generating " + cReg.name());
            }
        }

        TypeElement expErrType = processingEnv.getElementUtils().getTypeElement(EXP_ERR_NAME);
        if (expErrType != null) {
            for (Element e : roundEnv.getElementsAnnotatedWith(expErrType)) {
                if (!expectedErrors.contains(e)) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, "Expected error not emited", e);
                }
            }
        }
        return true;
    }

    private String findAndVerify(Element e, Set<Element> expectedErrors) {
        LinkedList<TypeElement> allButTopMostClasses = new LinkedList<>();
        PackageElement pkg;
        for (;;) {
            if (e.getKind() == ElementKind.PACKAGE) {
                pkg = ((PackageElement) e);
                break;
            }
            allButTopMostClasses.add((TypeElement) e);
            e = e.getEnclosingElement();
        }
        allButTopMostClasses.removeLast();
        for (TypeElement innerClass : allButTopMostClasses) {
            if (innerClass.getModifiers().contains(Modifier.PRIVATE)) {
                emitError(innerClass, expectedErrors, "@RegisterComponent: Make class non-private!");
                return null;
            }
            if (!innerClass.getModifiers().contains(Modifier.STATIC)) {
                emitError(innerClass, expectedErrors, "@RegisterComponent: Make class static!");
                return null;
            }
        }
        return pkg.getQualifiedName().toString();
    }

    private void printNodes(String indent, Node node, Set<String> variables, StringBuilder sb) {
        if (node instanceof Text) {
            String text = node.getTextContent();
            text = text.replaceAll("\\\\", "\\\\");
            text = text.replaceAll("\\\n", "\\\\n");

            text = eliminateVariables(text, variables);
            sb.append(indent).append("React.createText(").append(text).append(")");
            return;
        }

        sb.append(indent + "React.createElement(\"" + node.getNodeName() + "\", ");
        NamedNodeMap attr = node.getAttributes();
        if (attr != null && attr.getLength() > 0) {
            sb.append("\n" + indent + "React.props(");
            for (int i = 0; i < attr.getLength(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                Attr aNode = (Attr) attr.item(i);
                String attrValue = eliminateVariables(aNode.getValue(), variables);
                sb.append('"').append(aNode.getName()).append("\", ").append(attrValue);
            }
            sb.append(")");
        } else {
            sb.append("null");
        }
        sb.append(", ");
        NodeList children = node.getChildNodes();
        if (children.getLength() == 0) {
            sb.append("null");
        } else {
            final int len = children.getLength();
            for (int i = 0; i < len; i++) {
                sb.append("\n");
                printNodes("  " + indent, children.item(i), variables, sb);
                if (i < len - 1) {
                    sb.append(", ");
                }
            }
            sb.append("\n" + indent + ")");
        }
    }

    private String eliminateVariables(String text, Set<String> variables) {
        text = '"' + text + '"';
        for (String v : variables) {
            for (;;) {
                int at = text.indexOf("{" + v + "}");
                if (at == -1) {
                    break;
                }
                text = text.substring(0, at) + "\" + " + v + "+ \""
                        + text.substring(at + v.length() + 2);
            }
        }
        return text;
    }

    private void emitError(Element e, Set<Element> expectedErrors, String error) {
        for (AnnotationMirror am : e.getAnnotationMirrors()) {
            Element anno = am.getAnnotationType().asElement();
            if (anno.getKind() != ElementKind.ANNOTATION_TYPE) {
                continue;
            }
            TypeElement type = (TypeElement) anno;
            final String typeName = type.getQualifiedName().toString();
            if (EXP_ERR_NAME.equals(typeName)) {
                AnnotationValue value = am.getElementValues().values().iterator().next();
                if (error.equals(value.getValue())) {
                    expectedErrors.add(e);
                    return;
                }
            }
        }
        processingEnv.getMessager().printMessage(Kind.ERROR, error, e);
    }

    private void generateComponent(TypeElement key, String pkg, String name, Set<ExecutableElement> methods) throws IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
        JavaFileObject src = processingEnv.getFiler().createSourceFile(pkg + "." + name, methods.toArray(new Element[0]));
        Writer w = src.openWriter();
        w.append("package " + pkg  + ";\n");
        w.append("import cz.xelfi.demo.react4jdemo.api.React;\n");
        w.append("final class " + name + " extends " + key + " {\n");
        w.append("  " + name + "(cz.xelfi.demo.react4jdemo.api.React.Props props) {\n");
        w.append("    super(props);\n");
        w.append("  }\n");
        for (ExecutableElement m : methods) {
            Render render = m.getAnnotation(Render.class);
            Document node;
            try {
                node = builder.parse(new ByteArrayInputStream(render.value().getBytes()));
            } catch (SAXException ex) {
                throw new IOException(ex);
            }
            w.append("  @Override\n");
            w.append("  protected final React.Element " + m.getSimpleName() + "(");

            Set<String> replacements = new HashSet<>();
            {
                String sep = "";
                for (VariableElement p : m.getParameters()) {
                    TypeMirror pType = p.asType();

                    w.append(sep);
                    w.append(pType.toString());
                    w.append(" ");
                    w.append(p.getSimpleName());

                    replacements.add(p.getSimpleName().toString());

                    sep = ", ";
                }
            }
            w.append(") {\n");
            StringBuilder sb = new StringBuilder();
            sb.append("    return ");
            printNodes("    ", node.getChildNodes().item(0), replacements, sb);
            sb.append(";\n");
            w.append(sb.toString());
            w.append("  }\n");
        }
        w.append("}\n");
        w.close();
    }

}
