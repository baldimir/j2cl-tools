/*
 * Copyright © 2024 j2cl-maven-plugin authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vertispan.j2cl.tools;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

public class ModuleAccess {

    private static volatile boolean initialized = false;

    private static final String[] JDK_COMPILER_PACKAGES = {
            "com.sun.tools.javac.api",
            "com.sun.tools.javac.code",
            "com.sun.tools.javac.comp",
            "com.sun.tools.javac.file",
            "com.sun.tools.javac.main",
            "com.sun.tools.javac.model",
            "com.sun.tools.javac.parser",
            "com.sun.tools.javac.processing",
            "com.sun.tools.javac.tree",
            "com.sun.tools.javac.util"
    };

    public static void ensureJdkCompilerAccess() {
        if (initialized) {
            return;
        }
        synchronized (ModuleAccess.class) {
            if (initialized) {
                return;
            }

            Module jdkCompiler = ModuleLayer.boot().findModule("jdk.compiler").orElse(null);
            if (jdkCompiler == null) {
                initialized = true;
                return;
            }

            Module unnamed = ModuleAccess.class.getModule();

            if (jdkCompiler.isExported("com.sun.tools.javac.util", unnamed)) {
                initialized = true;
                return;
            }

            try {
                openModule(jdkCompiler, unnamed);
            } catch (Throwable t) {
                throw new RuntimeException(
                        "Cannot access jdk.compiler internals required by j2cl. "
                                + "Please create a .mvn/jvm.config file in your project root with the following content:\n"
                                + String.join("\n", getAddExportsFlags()),
                        t);
            }
            initialized = true;
        }
    }

    private static void openModule(Module jdkCompiler, Module target) throws Throwable {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);

        Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        long offset = unsafe.staticFieldOffset(implLookupField);
        MethodHandles.Lookup trustedLookup = (MethodHandles.Lookup) unsafe.getObject(
                MethodHandles.Lookup.class, offset);

        MethodHandle addExportsOrOpens = trustedLookup.findVirtual(
                Module.class, "implAddExportsOrOpens",
                MethodType.methodType(void.class, String.class, Module.class, boolean.class, boolean.class));

        for (String pkg : JDK_COMPILER_PACKAGES) {
            addExportsOrOpens.invoke(jdkCompiler, pkg, target, true, true);
        }
    }

    private static String[] getAddExportsFlags() {
        String[] flags = new String[JDK_COMPILER_PACKAGES.length];
        for (int i = 0; i < JDK_COMPILER_PACKAGES.length; i++) {
            flags[i] = "--add-exports jdk.compiler/" + JDK_COMPILER_PACKAGES[i] + "=ALL-UNNAMED";
        }
        return flags;
    }
}
