#!/bin/bash

comm -3 <(cat plugins.list | sort | uniq) <(ls -1 *.jar | sort)
