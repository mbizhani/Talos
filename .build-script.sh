#!/bin/bash

OUT_FILE="target/$1.sh"
JAR_FILE="target/$1-$2.jar"

if [ -f ${JAR_FILE} ]; then
  rm -f ${OUT_FILE}

  cat > $OUT_FILE << EOF
#!/bin/bash

exec java -jar \$0 "\$@"

EOF

  cat $JAR_FILE >> $OUT_FILE
  chmod +x $OUT_FILE

  cp -f $OUT_FILE $HOME/.local/bin
else
  printf "'$JAR_FILE' Not Found"
  exit 1
fi
