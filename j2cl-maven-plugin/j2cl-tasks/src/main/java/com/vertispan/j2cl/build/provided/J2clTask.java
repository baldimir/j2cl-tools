/*
 * Copyright © 2021 j2cl-maven-plugin authors
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
package com.vertispan.j2cl.build.provided;

import com.google.auto.service.AutoService;
import com.google.j2cl.common.SourceUtils;
import com.vertispan.j2cl.build.task.*;
import com.vertispan.j2cl.tools.J2CLModuleParser;
import com.vertispan.j2cl.tools.J2cl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoService(TaskFactory.class)
public class J2clTask extends TaskFactory {

    public static final PathMatcher JAVA_SOURCES = withSuffix(".java");
    public static final PathMatcher NATIVE_JS_SOURCES = withSuffix(".native.js");
    public static final PathMatcher JAVA_BYTECODE = withSuffix(".class");

    @Override
    public String getOutputType() {
        return OutputTypes.TRANSPILED_JS;
    }

    @Override
    public String getTaskName() {
        return "default";
    }

    @Override
    public String getVersion() {
        return "0";
    }

    @Override
    public Task resolve(Project project, Config config) {
        Input ownJavaSources = input(project, OutputTypes.STRIPPED_SOURCES).filter(JAVA_SOURCES, NATIVE_JS_SOURCES);
        List<Input> ownNativeJsSources = Collections.singletonList(input(project, OutputTypes.BYTECODE).filter(NATIVE_JS_SOURCES));

        List<Input> classpathHeaders = scope(project.getDependencies().stream()
                .filter(dep -> dep.getProject().getProcessors().isEmpty())
                .collect(Collectors.toSet()), Dependency.Scope.COMPILE)
                .stream()
                .map(inputs(OutputTypes.STRIPPED_BYTECODE_HEADERS))
                .map(input -> input.filter(JAVA_BYTECODE))
                .toList();

        File bootstrapClasspath = config.getBootstrapClasspath();
        List<File> extraClasspath = config.getExtraClasspath();
        Input inputDirs = input(project, OutputTypes.INPUT_SOURCES);

        return context -> {
            if (ownJavaSources.getFilesAndHashes().isEmpty()) {
                return;
            }

            List<File> classpathDirs = Stream.concat(
                    classpathHeaders.stream().flatMap(i -> i.getParentPaths().stream().map(Path::toFile)),
                    extraClasspath.stream()
            ).toList();

            List<File> sourcePaths = inputDirs.getParentPaths().stream().map(Path::toFile).toList();
            List<Path> superSourcePaths = J2CLModuleParser.getSuperSourcePaths(sourcePaths);

            List<? extends CachedPath> allJava = collectByMatcher(ownJavaSources);
            List<? extends CachedPath> allNative = collectByMatcher(ownNativeJsSources);

            Map<Boolean, List<CachedPath>> javaSplit = partitionBySuperSource(allJava, superSourcePaths);
            Map<Boolean, List<CachedPath>> nativeSplit = partitionBySuperSource(allNative, superSourcePaths);

            List<SourceUtils.FileInfo> javaSources = toFileInfos(javaSplit.get(false));
            List<SourceUtils.FileInfo> nativeSources = toFileInfos(nativeSplit.get(false));

            J2cl j2cl = new J2cl(classpathDirs, bootstrapClasspath, context.outputPath().toFile(), context);
            if (!j2cl.transpile(javaSources, nativeSources)) {
                throw new IllegalStateException("Error while running J2CL");
            }

            List<SourceUtils.FileInfo> superJava = toFileInfosStripped(javaSplit.get(true), superSourcePaths);
            List<SourceUtils.FileInfo> superNative = toFileInfosStripped(nativeSplit.get(true), superSourcePaths);

            if (!superJava.isEmpty()) {
                List<File> superClasspath = new ArrayList<>(classpathDirs);
                superClasspath.add(context.outputPath().toFile());

                j2cl = new J2cl(superClasspath, bootstrapClasspath, context.outputPath().toFile(), context);
                if (!j2cl.transpileSuperSource(superJava, superNative)) {
                    throw new IllegalStateException("Error while running J2CL");
                }
            }
        };
    }

    private static List<? extends CachedPath> collectByMatcher(Input input) {
        return input.getFilesAndHashes().stream()
                .filter(e -> J2clTask.JAVA_SOURCES.matches(e.getSourcePath()))
                .toList();
    }

    private static List<? extends CachedPath> collectByMatcher(List<Input> inputs) {
        return inputs.stream()
                .flatMap(i -> i.getFilesAndHashes().stream())
                .filter(e -> J2clTask.NATIVE_JS_SOURCES.matches(e.getSourcePath()))
                .toList();
    }

    private static Map<Boolean, List<CachedPath>> partitionBySuperSource(
            List<? extends CachedPath> paths, List<Path> superSourcePaths) {
        if (superSourcePaths.isEmpty()) {
            return Map.of(false, new ArrayList<>(paths), true, List.of());
        }
        Map<Boolean, List<CachedPath>> result = new HashMap<>();
        result.put(false, new ArrayList<>());
        result.put(true, new ArrayList<>());
        for (CachedPath p : paths) {
            boolean isSuper = superSourcePaths.stream().anyMatch(p.getSourcePath()::startsWith);
            result.get(isSuper).add(p);
        }
        return result;
    }

    private static List<SourceUtils.FileInfo> toFileInfos(List<CachedPath> paths) {
        return paths.stream()
                .map(p -> SourceUtils.FileInfo.create(p.getAbsolutePath().toString(), p.getSourcePath().toString()))
                .toList();
    }

    private static List<SourceUtils.FileInfo> toFileInfosStripped(List<CachedPath> paths, List<Path> superSourcePaths) {
        return paths.stream()
                .map(p -> {
                    Path superRoot = superSourcePaths.stream()
                            .filter(p.getSourcePath()::startsWith)
                            .findFirst()
                            .orElseThrow();
                    String targetPath = superRoot.relativize(p.getSourcePath()).toString();
                    return SourceUtils.FileInfo.create(p.getAbsolutePath().toString(), targetPath);
                })
                .toList();
    }
}
