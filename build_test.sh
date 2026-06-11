#!/usr/bin/env sh
# sh script to build maven sub modules

# build all modules
set -e
# read revision from the file
revision=$(cat revision.txt)

# build closure-compiler (bazel + maven)
echo "Building closure-compiler"
cd closure-compiler
./build_test.sh
cd maven
mvn clean install
cd ../..

# build j2cl (bazel + maven)
echo "Building j2cl"
cd j2cl
./build_test.sh
cd maven
./build.sh
mvn clean install
cd ../..

modules=(
      j2cl-maven-plugin
      gwt3-processors
      mapper-xml
      mapper-json
      mapper-yaml
      di
      bom
     )

for module in "${modules[@]}"
do
    echo "Building $module"
    cd $module
    mvn clean install -Drevision=$revision
    cd ..
    # stop on error
    if [ $? -ne 0 ]; then
        echo "Error building $module"
        exit 1
    fi

done
