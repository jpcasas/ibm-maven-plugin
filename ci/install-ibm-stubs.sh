#!/usr/bin/env bash
# Builds compile-only stubs for the proprietary IBM ACE jars (IntegrationAPI, brokerutil)
# and installs them in the local Maven repository so the plugin can be compiled and
# unit-tested without an ACE installation (e.g. in CI).
# NEVER use these stubs at runtime: install the real jars as described in Install-ibm-jars.md.
set -euo pipefail
cd "$(dirname "$0")/ibm-stubs"
VERSION="${IBM_ACE_VERSION:-12.0.6}"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT

javac --release 8 -nowarn -d "$OUT/IntegrationAPI" $(find IntegrationAPI -name '*.java')
jar cf "$OUT/IntegrationAPI.jar" -C "$OUT/IntegrationAPI" .
javac --release 8 -nowarn -d "$OUT/brokerutil" $(find brokerutil -name '*.java')
jar cf "$OUT/brokerutil.jar" -C "$OUT/brokerutil" .

for a in IntegrationAPI brokerutil; do
  mvn -B -q install:install-file -Dfile="$OUT/$a.jar" -DgroupId=com.ibm -DartifactId="$a" \
      -Dversion="$VERSION" -Dpackaging=jar
done
echo "IBM stubs $VERSION installed"
