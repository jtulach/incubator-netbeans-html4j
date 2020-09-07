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

import cz.xelfi.demo.react4jdemo.api.GenerateReact;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import javax.tools.StandardLocation;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = Processor.class)
public class JavaSxProcessor extends AbstractProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        Set<String> names = new HashSet<>();
        names.add(GenerateReact.class.getCanonicalName());
        names.add(GenerateReact.Group.class.getCanonicalName());
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
        Map</*package*/String,Set<Element>> annotatedElementsByPackage = new HashMap<>();
        for (Element e : roundEnv.getElementsAnnotatedWith(GenerateReact.class)) {
            GenerateReact a = e.getAnnotation(GenerateReact.class);
            if (a == null) {
                continue;
            }
            prepareElement(e, annotatedElementsByPackage);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(GenerateReact.Group.class)) {
            GenerateReact.Group a = e.getAnnotation(GenerateReact.Group.class);
            if (a == null) {
                continue;
            }
            prepareElement(e, annotatedElementsByPackage);
        }
        PACKAGE: for (Map.Entry<String,Set<Element>> packageEntry : annotatedElementsByPackage.entrySet()) {
            String pkg = packageEntry.getKey();
            Set<Element> annotatedElements = packageEntry.getValue();
            PackageElement pkgE = processingEnv.getElementUtils().getPackageElement(pkg);
            if (pkgE != null) {
                Set<Element> unscannedTopElements = new HashSet<>();
                unscannedTopElements.add(pkgE);
                try {
                    unscannedTopElements.addAll(pkgE.getEnclosedElements());
                } catch (/*NullPointerException,BadClassFile*/RuntimeException x) { // #196556
                    processingEnv.getMessager().printMessage(Kind.WARNING, "#196556: reading " + pkg + " failed with " + x + " in " + x.getStackTrace()[0] + "; do a clean build!");
                }
                unscannedTopElements.removeAll(roundEnv.getRootElements());
                addToAnnotatedElements(unscannedTopElements, annotatedElements);
            } else {
                processingEnv.getMessager().printMessage(Kind.WARNING, "Could not check for other source files in " + pkg);
            }
            Map</*key*/String,/*value*/String> pairs = new HashMap<>();
            Map</*identifier*/String,Element> identifiers = new HashMap<>();
            Map</*key*/String,/*simplename*/String> compilationUnits = new HashMap<>();
            Map</*key*/String,/*line*/String[]> comments = new HashMap<>();
            for (Element e : annotatedElements) {
                String simplename = findCompilationUnitName(e);
                List<String> runningComments = new ArrayList<>();
                for (String keyValue : Collections.singleton(e.getAnnotation(GenerateReact.class).value())) {
                    if (keyValue.startsWith("#")) {
                        runningComments.add(keyValue);
                        if (keyValue.matches("# +(PART)?(NO)?I18N *")) {
                            processingEnv.getMessager().printMessage(Kind.ERROR, "#NOI18N and related keywords must not include spaces", e);
                        }
                        continue;
                    }
                    int i = keyValue.indexOf('=');
                    if (i == -1) {
                        processingEnv.getMessager().printMessage(Kind.ERROR, "Bad key=value: " + keyValue, e);
                        continue;
                    }
                    String key = keyValue.substring(0, i);
                    if (key.isEmpty() || !key.equals(key.trim())) {
                        processingEnv.getMessager().printMessage(Kind.ERROR, "Whitespace not permitted in key: " + keyValue, e);
                        continue;
                    }
                    Element original = identifiers.put(toIdentifier(key), e);
                    if (original != null) {
                        processingEnv.getMessager().printMessage(Kind.ERROR, "Duplicate key: " + key, e);
                        processingEnv.getMessager().printMessage(Kind.ERROR, "Duplicate key: " + key, original);
                        continue PACKAGE; // do not generate anything
                    }
                    String value = keyValue.substring(i + 1);
                    pairs.put(key, value);
                    compilationUnits.put(key, simplename);
                    if (!runningComments.isEmpty()) {
                        comments.put(key, runningComments.toArray(new String[runningComments.size()]));
                        runningComments.clear();
                    }
                }
                if (!runningComments.isEmpty()) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, "Comments must precede keys", e);
                }
            }
            Element[] elements = new HashSet<>(identifiers.values()).toArray(new Element[0]);
        }
        return true;
    }

    private void prepareElement(Element e, Map<String, Set<Element>> annotatedElementsByPackage) {
        String pkg = findPackage(e);
        Set<Element> annotatedElements = annotatedElementsByPackage.get(pkg);
        if (annotatedElements == null) {
            annotatedElements = new HashSet<>();
            annotatedElementsByPackage.put(pkg, annotatedElements);
        }
        annotatedElements.add(e);
    }

    private String findPackage(Element e) {
        switch (e.getKind()) {
        case PACKAGE:
            return ((PackageElement) e).getQualifiedName().toString();
        default:
            return findPackage(e.getEnclosingElement());
        }
    }

    private String findCompilationUnitName(Element e) {
        switch (e.getKind()) {
        case PACKAGE:
            return "package-info";
        case CLASS:
        case INTERFACE:
        case ENUM:
        case ANNOTATION_TYPE:
            switch (e.getEnclosingElement().getKind()) {
            case PACKAGE:
                return e.getSimpleName().toString();
            }
        }
        return findCompilationUnitName(e.getEnclosingElement());
    }

    private String toIdentifier(String key) {
        return key;
    }

    private String toJavadoc(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace("*/", "&#x2A;/").replace("\n", "<br>").replace("@", "&#64;");
    }

    private void addToAnnotatedElements(Collection<? extends Element> unscannedElements, Set<Element> annotatedElements) {
        for (Element e : unscannedElements) {
            if (e.getAnnotation(GenerateReact.class) != null) {
                annotatedElements.add(e);
            }
            if (e.getKind() != ElementKind.PACKAGE) {
                addToAnnotatedElements(e.getEnclosedElements(), annotatedElements);
            }
        }
    }

    private void warnUndocumented(int i, Element e, String key) {
        AnnotationMirror mirror = null;
        AnnotationValue value = null;
        if (e != null) {
            for (AnnotationMirror _mirror : e.getAnnotationMirrors()) {
                if (_mirror.getAnnotationType().toString().equals(GenerateReact.class.getCanonicalName())) {
                    mirror = _mirror;
                    for (Map.Entry<? extends ExecutableElement,? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
                        if (entry.getKey().getSimpleName().contentEquals("value")) {
                            // SimpleAnnotationValueVisitor6 unusable here since we need to determine the AnnotationValue in scope when visitString is called:
                            Object v = entry.getValue().getValue();
                            if (v instanceof String) {
                                if (((String) v).startsWith(key + "=")) {
                                    value = entry.getValue();
                                }
                            } else {
                                for (AnnotationValue subentry : ((List<AnnotationValue>) v)) {
                                    v = subentry.getValue();
                                    if (v instanceof String) {
                                        if (((String) v).startsWith(key + "=")) {
                                            value = subentry;
                                            break;
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        }
        processingEnv.getMessager().printMessage(Kind.WARNING, "Undocumented format parameter {" + i + "}", e, mirror, value);
    }

}
