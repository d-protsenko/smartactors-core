#!/usr/bin/env bash
mkdir -p temp/corefeatures
root_dir=$PWD

echo "Install CoreFeatures into local repository..."
cd "$root_dir"/CoreFeatures
mvn clean install -Dmaven.test.skip=true
cd project-distribution
for file in $(cat "$root_dir"/corefeatures.txt); do
  cp "$file"* "$root_dir"/temp/corefeatures/;
done;
echo "End of installing CoreFeatures into local repository."

echo "Install Starters into local repository..."
cd "$root_dir"/Starters
mvn clean install -Dmaven.test.skip=true
cd project-distribution
for file in $(cat ../../starters.txt); do
  cp "$file"* "$root_dir"/temp/corefeatures/;
done;
echo "End of installing Starters into local repository."

echo "Assemble corepack..."
cd "$root_dir"/CorePacks/Core
mvn clean dependency:copy-dependencies assembly:assembly
cp ./target/core-pack* "$root_dir"/temp/
echo "End of assemble corepack."

echo "Start assemble server"
cd "$root_dir"/Servers/ServerWithFeatures
mvn clean install -Dmaven.test.skip=true
cp configuration.json "$root_dir"/temp/
cd target
cp server.jar "$root_dir"/temp/
echo "End asseble server."

echo "Start copy of common features..."
cd "$root_dir"/CommonFeatures
mvn clean install -Dmaven.test.skip=true
cp -R project-distribution/. "$root_dir"/temp/corefeatures/
echo "End copy of common features."

echo "Start copy of system actors..."
cd "$root_dir"/SystemActors
mvn clean install -Dmaven.test.skip=true
cp -R project-distribution/. "$root_dir"/temp/corefeatures/
echo "End copy of system actors."

cd "$root_dir"/temp/corefeatures
unzip "*.zip"
find . -name '*.zip' -delete

cd "$root_dir"/temp
unzip "core-pack*.zip" -d core/
find . -name 'core-pack*.zip' -delete

cd "$root_dir"/temp/corefeatures
for feature in */; do
    cd "$feature"

    mkdir tmp
    mkdir tmp/uz
    mkdir tmp/bu

    for fn in ./*.jar
    do
        unzip -uo "$fn" -d tmp/uz
        mv "$fn" tmp/bu
    done
    cd tmp/uz
    zip -r feature.jar .
    cd ../../
    mv tmp/uz/feature.jar .
    rm -r tmp
    cd ..
done


