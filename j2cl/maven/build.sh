#!/usr/bin/env bash

set -e
BAZEL=${BAZEL:-bazel}

${BAZEL} build //transpiler/java/...:*

${BAZEL} build //tools/java/com/google/j2cl/tools/gwtincompatible:*
${BAZEL} build //tools/java/com/google/j2cl/tools/minifier:*

${BAZEL} build //jre/java/javaemul/internal/primitives:primitives

${BAZEL} build //jre/java:*

${BAZEL} build //junit/emul/java:*
${BAZEL} build //junit/emul/java/com/google/gwt/junit:*
${BAZEL} build //junit/generator/java/com/google/j2cl/junit/async:*
${BAZEL} build //junit/generator/java/com/google/j2cl/junit/apt:*

${BAZEL} build //junit/generator/java/com/google/j2cl/junit/runtime:junit_runtime
${BAZEL} build //junit/generator/java/com/google/j2cl/junit/runtime:jsunit_helpers-j2cl

${BAZEL} build //junit/generator/java/com/google/j2cl/junit/runtime:*
${BAZEL} build //junit/generator/java/com/google/j2cl/junit/runtime:internal_assumption_violated_exception-javadoc