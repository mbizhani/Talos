#!/bin/bash

OUT_FILE="target/talos.sh"
JAR_FILE="target/$1-$2.jar"

if [ -f "${JAR_FILE}" ]; then
  rm -f "${OUT_FILE}"

  cat > "$OUT_FILE" << EOF
#!/bin/bash

exec java -jar \$0 "\$@"

EOF

  cat "$JAR_FILE" >> "$OUT_FILE"
  chmod +x "$OUT_FILE"

  mkdir -p "$HOME/.local/bin"
  cp -f "$OUT_FILE" "$HOME/.local/bin/"
else
  printf "'%s' Not Found" "$JAR_FILE"
  exit 1
fi
