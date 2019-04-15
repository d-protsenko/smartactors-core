#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
GRAY='\033[1;30m'

update_rid_rurl() {
    local f_branch=$1
    local f_path=$2
    if [ "$f_branch" = "master" ]
    then
        if [ "$f_path" = "CoreFeatures" -o "$f_path" = "SystemActors" -o "$f_path" = "CorePack" -o "$f_path" = "Starters" ]
        then
            echo "1"
            rurl="https://features-library.7bits.it/artifactory/smartactors_core_and_core_features"
            rid="smartactors_core_and_core_features"
        elif [ "$f_path" = "CommonFeatures" ]
        then
            echo "2"
            rurl="https://features-library.7bits.it/artifactory/smartactors_common_features"
            rid="smartactors_common_features"
        elif [ "$f_path" = "ServerDevelopmentTools" -o "$f_path" = "das" ]
        then
            echo "3"
            rurl="https://features-library.7bits.it/artifactory/smartactors_development_tools"
            rid="smartactors_development_tools"
        elif [ "$f_path" = "Servers/Server2" ]
        then
            echo "4"
            rurl="https://features-library.7bits.it/artifactory/smartactors_servers"
            rid="smartactors_servers"
        fi
    elif [ "$f_branch" = "develop" ]
    then
        if [ "$f_path" = "CoreFeatures" -o "$f_path" = "SystemActors" -o "$f_path" = "CorePack" -o "$f_path" = "Starters" ]
        then
            echo "5"
            rurl="https://features-library.7bits.it/artifactory/smartactors_core_and_core_features_dev"
            rid="smartactors_core_and_core_features_dev"
        elif [ "$f_path" = "CommonFeatures" ]
        then
            echo "6"
            rurl="https://features-library.7bits.it/artifactory/smartactors_common_features_dev"
            rid="smartactors_common_features_dev"
        elif [ "$f_path" = "ServerDevelopmentTools" -o "$f_path" = "das" ]
        then
            echo "7"
            rurl="https://features-library.7bits.it/artifactory/smartactors_development_tools_dev"
            rid="smartactors_development_tools_dev"
        elif [ "$f_path" = "Servers/Server2" ]
        then
            echo "8"
            rurl="https://features-library.7bits.it/artifactory/smartactors_servers_dev"
            rid="smartactors_servers_dev"
        fi
    fi
}

build() {
    local f_path=$1
    echo "\n\n${GREEN}Start to build the feature '$f_path' ...${GRAY}\n\n"
    (cd $f_path && das make jar) || exit 1
}

deploy() {
    local f_path=$1
    local f_rid=$2
    local f_rurl=$3
    echo "\n\n${GREEN}Start to deploy the feature '$f_path' ..."
    echo "Deploy to repository with\nid - $f_rid\nurl - $f_rurl .${GRAY}\n\n"
    if [ "$f_path" = "CorePack" -o "$f_path" = "das" ]
    then
        (cd $f_path && mvn deploy:deploy-file -Dfeature.rurl="$f_rurl" -Dfeature.rid="$f_rid" -Ddeploy.classifier= ) || exit 1
    else
        (cd $f_path && mvn deploy -Dmaven.test.skip=true -DdeployOnly=true -Ddeploy.format=jar -Dfeature.rurl="$f_rurl" -Dfeature.rid="$f_rid" -Ddeploy.classifier= ) || exit 1
    fi
}

tag="$1"
version=$(echo "$tag" | awk -F[//] '{print $1}')
branch=$(echo "$tag" | awk -F[//] '{print $2}')
prefix="$version/$branch/"
path=${tag#"$prefix"}

rurl=""
rid=""

echo "${GREEN}"
echo "Tag with version '$version'".
echo "Tring to switch to branch '$branch' ..."
echo "Feature path: $path"

echo "${GRAY}"
commit_hash=$(git rev-list -n 1 $tag) || exit 1
git checkout $commit_hash || exit 1

if [ "$path" = "all" ]
then
    update_rid_rurl $branch CoreFeatures
    build CoreFeatures
    deploy CoreFeatures $rid $rurl

    if [ "$version" < "v0.5.0" ]
    then
        update_rid_rurl $branch Starters
        build Starters
        deploy Starters $rid $rurl
    fi

    update_rid_rurl $branch SystemActors
    build SystemActors
    deploy SystemActors $rid $rurl

    update_rid_rurl $branch CorePack
    build CorePack
    deploy CorePack $rid $rurl

    update_rid_rurl $branch CommonFeatures
    build CommonFeatures
    deploy CommonFeatures $rid $rurl

    update_rid_rurl $branch ServerDevelopmentTools
    build ServerDevelopmentTools
    deploy ServerDevelopmentTools $rid $rurl

    update_rid_rurl $branch das
    build das
    deploy das $rid $rurl

    update_rid_rurl $branch Servers/Server2
    build Servers/Server2
    deploy Servers/Server2 $rid $rurl
else
    update_rid_rurl $branch $path
    build "$path"
    deploy "$path" "$rid" "$rurl"
fi
