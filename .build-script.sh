#!/bin/bash

OUT_FILE="target/talos.sh"
JAR_FILE="target/$1-$2.jar"

if [ -f "${JAR_FILE}" ]; then
  rm -f "${OUT_FILE}"

  cat > "$OUT_FILE" << EOF
#!/bin/bash

if [ "\$1" == "ssh" ]; then
  mkdir -p "\${HOME}/.talos"
  exec java -jar \$0 "\$@" > /dev/null 2>&1 &
  exit 0
else
  exec java -jar \$0 "\$@"
fi

EOF

  cat "$JAR_FILE" >> "$OUT_FILE"
  chmod +x "$OUT_FILE"

  mkdir -p "$HOME/.local/bin"
  cp -f "$OUT_FILE" "$HOME/.local/bin/"
else
  printf "'%s' Not Found" "$JAR_FILE"
  exit 1
fi
