#!/bin/sh
cd "$(dirname "$0")" || exit 1
exec java -jar bin/assistente-academico.jar --app.modo=trechos
