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

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CLibrary;
import org.graalvm.nativeimage.c.struct.CStruct;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.PointerBase;
import org.graalvm.word.WordFactory;
import org.netbeans.html.presenters.render.Cocoa.ObjC;

@CLibrary("objc")
@CContext(RawObjC.ObjcDirectives.class)
public final class RawObjC implements ObjC {

    @CFunction
    private static native PointerBase objc_getClass(CCharPointer name);

    @Override
    public boolean class_addMethod(Pointer cls, Pointer name, Callback imp, String types) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String class_getName(Pointer cls) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String object_getClassName(Pointer cls) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Pointer class_copyMethodList(Class cls, IntByReference outCount) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Pointer objc_allocateClassPair(Pointer cls, String name, int additionalBytes) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Pointer objc_getClass(String name) {
        CTypeConversion.CCharPointerHolder cName = CTypeConversion.toCString(name);
        try {
            PointerBase ptr = objc_getClass(cName.get());
            return new Pointer(ptr.rawValue());
        } finally {
            cName.close();
        }
    }

    @CFunction
    private static native long objc_msgSend(PointerBase theReceiver, PointerBase theSelector, PointerBase args);



    @Override
    public long objc_msgSend(Pointer theReceiver, Pointer theSelector, Object... arguments) {
        Thread.dumpStack();
        System.err.println("arguments: " + Arrays.toString(arguments));
        PointerBase args = WordFactory.nullPointer(); // StackValue.get(arguments.length, PointerBase.class);

        return objc_msgSend(toPtr(theReceiver), toPtr(theSelector), args);
    }

    private static PointerBase toPtr(Pointer theReceiver) {
        return WordFactory.pointer(Pointer.nativeValue(theReceiver));
    }

    @Override
    public Cocoa.Rct objc_msgSend_stret(Pointer theReceiver, Pointer theSelector, Object... arguments) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void objc_registerClassPair(Pointer cls) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @CFunction
    private static native PointerBase sel_getUid(CCharPointer name);

    @Override
    public Pointer sel_getUid(String name) {
        CTypeConversion.CCharPointerHolder cName = CTypeConversion.toCString(name);
        try {
            PointerBase ptr = sel_getUid(cName.get());
            return new Pointer(ptr.rawValue());
        } finally {
            cName.close();
        }
    }

    public static void main(String... args) throws Exception {
        String p = "Cocoa";
        if (args.length > 0) {
            p = args[0];
        }
        Show.show(p, new URI("http://netbeans.org"));
    }

    public static final class ObjcDirectives implements CContext.Directives {
        public ObjcDirectives() {
            Thread.dumpStack();
        }

        @Override
        public List<String> getHeaderFiles() {
            new Exception("getHeaderFiles").printStackTrace();
            return Arrays.asList("<objc/objc.h>");
        }

        @Override
        public List<String> getLibraries() {
            new Exception("objc").printStackTrace();
            return Arrays.asList("objc");
        }
    }
}
