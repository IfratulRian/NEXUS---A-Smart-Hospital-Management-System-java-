#!/bin/bash
# Usage: ./build.sh [run|test]
set -e
mkdir -p out
javac -d out $(find src -name '*.java')
case "$1" in
  run)  java -cp out Main ;;
  test) javac -cp out -d out/test $(find test -name '*.java') && java -cp out:out/test nexus.NexusTests ;;
  *)    echo "Built into ./out. Use: ./build.sh run | ./build.sh test" ;;
esac
