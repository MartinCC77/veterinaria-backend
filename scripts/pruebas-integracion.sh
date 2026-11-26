#!/usr/bin/env bash
# Pruebas de integración de la API de Veterinaria San Marcos.
# Uso:  ./scripts/pruebas-integracion.sh [URL_BASE]
# Requiere curl y python3, y la API corriendo con los 3 usuarios de demostración.
# Todo lo que crea lleva el prefijo "PRB-" o "prueba-" para poder limpiarlo después.

BASE="${1:-http://localhost:8080/api/v1}"
TMP=$(mktemp)
TOTAL=0
FALLAS=0
ID_PRUEBA=$(date +%s)

# ---------- utilidades ----------

# Saca un campo de un JSON que llega por la entrada estándar. Ej: extraer "['token']"
extraer() {
  python3 -c "import sys,json;d=json.load(sys.stdin);print(eval('d'+sys.argv[1]))" "$1"
}

# req METODO RUTA [TOKEN] [CUERPO]: llama a la API; deja el código HTTP en STATUS y el cuerpo en RESP
req() {
  local metodo="$1" ruta="$2" token="$3" cuerpo="$4"
  local args=(-s -o "$TMP" -w '%{http_code}' -X "$metodo" "$BASE$ruta" -H 'Content-Type: application/json')
  [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
  [ -n "$cuerpo" ] && args+=(-d "$cuerpo")
  STATUS=$(curl "${args[@]}")
  RESP=$(cat "$TMP")
}

# check "descripción" CODIGO_ESPERADO [TEXTO_QUE_DEBE_APARECER_EN_LA_RESPUESTA]
check() {
  TOTAL=$((TOTAL + 1))
  local ok=1
  [ "$STATUS" = "$2" ] || ok=0
  if [ -n "$3" ] && ! echo "$RESP" | grep -q -- "$3"; then ok=0; fi
  if [ "$ok" = 1 ]; then
    printf 'PASS   %02d  %-72s HTTP %s\n' "$TOTAL" "$1" "$STATUS"
  else
    FALLAS=$((FALLAS + 1))
    printf 'FALLA  %02d  %-72s esperado %s, obtenido %s\n' "$TOTAL" "$1" "$2" "$STATUS"
    echo "           respuesta: $RESP"
  fi
}

# verificar "descripción" 1|0: para comprobaciones que no son solo un código HTTP
verificar() {
  TOTAL=$((TOTAL + 1))
  if [ "$2" = "1" ]; then
    printf 'PASS   %02d  %s\n' "$TOTAL" "$1"
  else
    FALLAS=$((FALLAS + 1))
    printf 'FALLA  %02d  %s\n' "$TOTAL" "$1"
  fi
}

seccion() {
  echo
  echo "== $1"
}

# Devuelve el JSON del login
iniciar() {
  curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' \
    -d "{\"correo\":\"$1\",\"contrasena\":\"$2\"}"
}

# ---------- preparación ----------
echo "Pruebas de integración contra $BASE"
echo "Fecha: $(date '+%Y-%m-%d %H:%M:%S')"

L=$(iniciar admin@sanmarcos.cl admin123)
ADMIN_T=$(echo "$L" | extraer "['token']" 2>/dev/null)
ADMIN_ID=$(echo "$L" | extraer "['usuario']['id']" 2>/dev/null)
L=$(iniciar recepcion@sanmarcos.cl recep123)
RECEP_T=$(echo "$L" | extraer "['token']" 2>/dev/null)
L=$(iniciar juan@correo.cl dueno123)
JUAN_T=$(echo "$L" | extraer "['token']" 2>/dev/null)
JUAN_ID=$(echo "$L" | extraer "['usuario']['id']" 2>/dev/null)

if [ -z "$ADMIN_T" ] || [ -z "$RECEP_T" ] || [ -z "$JUAN_T" ]; then
  echo "No se pudo iniciar sesión con los usuarios de demostración. ¿Está corriendo la API en $BASE?"
  exit 1
fi

CORREO_A="prueba-a-$ID_PRUEBA@test.cl"
CORREO_B="prueba-b-$ID_PRUEBA@test.cl"
CORREO_C="prueba-c-$ID_PRUEBA@test.cl"
CODIGO="PRB-$((ID_PRUEBA % 100000))"
CODIGO2="PRB2-$((ID_PRUEBA % 100000))"
# Fecha futura al azar para no chocar con citas confirmadas de otras ejecuciones
FECHA=$(printf '2027-%02d-%02d' $((RANDOM % 12 + 1)) $((RANDOM % 28 + 1)))

# ---------- A. Autenticación y JWT ----------
seccion "A. Autenticación y JWT"

req POST /auth/login "" '{"correo":"admin@sanmarcos.cl","contrasena":"admin123"}'
check "Login con credenciales correctas entrega el token" 200 '"token"'

req POST /auth/login "" '{"correo":"admin@sanmarcos.cl","contrasena":"incorrecta"}'
check "Login con clave incorrecta responde 401 con mensaje genérico" 401 'Correo o contraseña incorrectos'

req POST /auth/login "" '{"correo":"noexiste@correo.cl","contrasena":"admin123"}'
check "Login con correo inexistente responde el mismo mensaje genérico" 401 'Correo o contraseña incorrectos'

req POST /auth/login "" '{}'
check "Login sin datos responde 400 (validación)" 400

req POST /auth/registro "" "{\"nombre\":\"Prueba A\",\"correo\":\"$CORREO_A\",\"contrasena\":\"clave123\"}"
check "Registro de un dueño nuevo responde 201 con rol dueno" 201 '"rol":"dueno"'

req POST /auth/registro "" "{\"nombre\":\"Prueba B\",\"correo\":\"$CORREO_B\",\"contrasena\":\"clave123\",\"rol\":\"administrador\"}"
check "Registro que intenta asignarse el rol administrador: el servidor lo ignora" 201 '"rol":"dueno"'

req POST /auth/registro "" "{\"nombre\":\"Prueba A\",\"correo\":\"$CORREO_A\",\"contrasena\":\"clave123\"}"
check "Registro con correo repetido responde 409" 409

req POST /auth/registro "" "{\"nombre\":\"Prueba D\",\"correo\":\"prueba-d-$ID_PRUEBA@test.cl\",\"contrasena\":\"123\"}"
check "Registro con contraseña muy corta responde 400" 400

req GET /usuarios ""
check "Ruta protegida sin token responde 401" 401

ALTERADO="${ADMIN_T:0:${#ADMIN_T}-4}AAAA"
req GET /usuarios "$ALTERADO"
check "Token con la firma alterada responde 401" 401

L=$(iniciar "$CORREO_A" clave123)
A_T=$(echo "$L" | extraer "['token']" 2>/dev/null)
A_ID=$(echo "$L" | extraer "['usuario']['id']" 2>/dev/null)

# ---------- B. Roles y propiedad de los datos ----------
seccion "B. Roles y propiedad de los datos"

req GET /usuarios "$JUAN_T"
check "Un dueño no puede listar usuarios (403)" 403

req GET /usuarios "$RECEP_T"
check "La recepcionista no puede listar usuarios (403)" 403

req GET /usuarios "$ADMIN_T"
check "El administrador lista usuarios (200)" 200

req GET "/usuarios/$A_ID" "$A_T"
check "Un usuario consulta su propia cuenta (200)" 200

req GET "/usuarios/$ADMIN_ID" "$A_T"
check "Cambiar el id en la URL no permite ver la cuenta de otra persona (403)" 403

req PUT "/usuarios/$A_ID" "$A_T" "{\"nombre\":\"Prueba A\",\"correo\":\"$CORREO_A\",\"rol\":\"administrador\"}"
check "Un dueño no puede cambiarse el rol a administrador (403)" 403

req DELETE "/usuarios/$ADMIN_ID" "$JUAN_T"
check "Un dueño no puede eliminar usuarios (403)" 403

# ---------- C. CRUD de usuarios ----------
seccion "C. CRUD de usuarios (administrador)"

req POST /usuarios "$ADMIN_T" "{\"nombre\":\"Prueba C\",\"correo\":\"$CORREO_C\",\"contrasena\":\"clave123\",\"rol\":\"recepcionista\"}"
check "Crear usuario con rol recepcionista (201)" 201 '"rol":"recepcionista"'
C_ID=$(echo "$RESP" | extraer "['id']" 2>/dev/null)

req POST /usuarios "$ADMIN_T" "{\"nombre\":\"Prueba C\",\"correo\":\"$CORREO_C\",\"contrasena\":\"clave123\",\"rol\":\"dueno\"}"
check "Crear usuario con correo repetido (409)" 409

req POST /usuarios "$ADMIN_T" "{\"nombre\":\"Prueba E\",\"correo\":\"prueba-e-$ID_PRUEBA@test.cl\",\"contrasena\":\"clave123\",\"rol\":\"superman\"}"
check "Crear usuario con un rol inexistente (400)" 400

req PUT "/usuarios/$C_ID" "$ADMIN_T" "{\"nombre\":\"Prueba C editada\",\"correo\":\"$CORREO_C\",\"rol\":\"dueno\"}"
check "Modificar nombre y rol de un usuario (200)" 200 'Prueba C editada'

req GET /usuarios/999999 "$ADMIN_T"
check "Consultar un usuario inexistente (404)" 404

req DELETE "/usuarios/$ADMIN_ID" "$ADMIN_T"
check "El administrador no puede eliminar su propia cuenta (409)" 409

req DELETE "/usuarios/$C_ID" "$ADMIN_T"
check "Eliminar un usuario (204)" 204

# ---------- D. Catálogo de servicios ----------
seccion "D. Catálogo de servicios"

req GET /servicios ""
check "Listar servicios es público (200)" 200 '"codigo"'

req GET /servicios/1 ""
check "Ver el detalle de un servicio es público (200)" 200

req GET /servicios/999999 ""
check "Servicio inexistente (404)" 404

SERV="{\"codigo\":\"$CODIGO\",\"categoria\":\"Consulta\",\"nombre\":\"Servicio de prueba\",\"especie\":\"Todas\",\"duracion\":30,\"precio\":20000,\"oferta\":false,\"observaciones\":\"Prueba\",\"imagen\":\"/Imagenes/prueba.jpg\"}"

req POST /servicios "" "$SERV"
check "Crear servicio sin sesión (401)" 401

req POST /servicios "$JUAN_T" "$SERV"
check "Un dueño no puede crear servicios (403)" 403

req POST /servicios "$ADMIN_T" "$SERV"
check "El administrador crea un servicio (201)" 201 "$CODIGO"
S_ID=$(echo "$RESP" | extraer "['id']" 2>/dev/null)

req POST /servicios "$ADMIN_T" "$SERV"
check "Crear servicio con código repetido (409)" 409

req POST /servicios "$ADMIN_T" "{\"codigo\":\"$CODIGO2\",\"categoria\":\"Consulta\",\"nombre\":\"Oferta invalida\",\"especie\":\"Todas\",\"duracion\":30,\"precio\":20000,\"oferta\":true,\"precioOferta\":30000,\"observaciones\":\"x\",\"imagen\":\"/Imagenes/x.jpg\"}"
check "Oferta con precio mayor al normal (400)" 400 'menor al precio normal'

req PUT "/servicios/$S_ID" "$ADMIN_T" "{\"codigo\":\"$CODIGO\",\"categoria\":\"Consulta\",\"nombre\":\"Servicio de prueba editado\",\"especie\":\"Todas\",\"duracion\":45,\"precio\":22000,\"oferta\":true,\"precioOferta\":15000,\"observaciones\":\"Prueba\",\"imagen\":\"/Imagenes/prueba.jpg\"}"
check "Modificar un servicio y ponerlo en oferta (200)" 200 '"precioOferta":15000'

req DELETE "/servicios/$S_ID" "$ADMIN_T"
check "Eliminar un servicio sin citas (204)" 204

# ---------- E. Citas y regla de negocio ----------
seccion "E. Citas y regla de negocio (horario ocupado = 409)"

req GET /citas ""
check "Listar citas sin sesión (401)" 401

CITA_A="{\"mascota\":\"PRB-Cachulo\",\"especie\":\"perro\",\"servicioId\":1,\"fecha\":\"$FECHA\",\"hora\":\"10:00\",\"motivo\":\"Control general\",\"urgencia\":true}"
req POST /citas "$JUAN_T" "$CITA_A"
check "Solicitar cita de urgencia: queda pendiente y el servidor calcula \$28.000" 201 '"precioFinal":28000'
ID_A=$(echo "$RESP" | extraer "['id']" 2>/dev/null)
verificar "La cita queda a nombre del usuario del token" "$(echo "$RESP" | python3 -c "import sys,json;print(1 if json.load(sys.stdin)['usuarioId']==$JUAN_ID else 0)")"

req POST /citas "$JUAN_T" "{\"mascota\":\"PRB-Rex\",\"especie\":\"perro\",\"servicioId\":1,\"fecha\":\"2020-01-01\",\"hora\":\"10:00\",\"motivo\":\"Control\"}"
check "Fecha anterior a hoy (400)" 400 'anterior a hoy'

req POST /citas "$JUAN_T" "{\"mascota\":\"PRB-Rex\",\"especie\":\"perro\",\"servicioId\":1,\"fecha\":\"$FECHA\",\"hora\":\"08:00\",\"motivo\":\"Control\"}"
check "Hora fuera del horario de atención (400)" 400

req POST /citas "$JUAN_T" "{\"mascota\":\"PRB-Rex\",\"especie\":\"dragon\",\"servicioId\":1,\"fecha\":\"$FECHA\",\"hora\":\"10:00\",\"motivo\":\"Control\"}"
check "Especie no permitida (400)" 400

req POST /citas "$JUAN_T" "{\"mascota\":\"PRB-Rex\",\"especie\":\"perro\",\"servicioId\":999999,\"fecha\":\"$FECHA\",\"hora\":\"10:00\",\"motivo\":\"Control\"}"
check "Servicio inexistente (400)" 400

CITA_B="{\"mascota\":\"PRB-Luna\",\"especie\":\"gato\",\"servicioId\":1,\"fecha\":\"$FECHA\",\"hora\":\"10:00\",\"motivo\":\"Revision\",\"urgencia\":false}"
req POST /citas "$A_T" "$CITA_B"
check "Otro dueño pide cita en el mismo horario: queda pendiente (201)" 201 '"estado":"pendiente"'
ID_B=$(echo "$RESP" | extraer "['id']" 2>/dev/null)

req PATCH "/citas/$ID_A/confirmar" "$JUAN_T"
check "Un dueño no puede confirmar citas (403)" 403

req PATCH "/citas/$ID_A/confirmar" "$RECEP_T"
check "La recepcionista confirma la primera cita (200)" 200 '"estado":"confirmada"'

req PATCH "/citas/$ID_B/confirmar" "$RECEP_T"
check "Confirmar otra cita en el mismo horario: CONFLICTO (409)" 409 'horario ya está ocupado'

req GET /citas "$JUAN_T"
verificar "Un dueño solo ve sus propias citas" "$(echo "$RESP" | python3 -c "import sys,json;d=json.load(sys.stdin);print(1 if len(d)>0 and all(c['usuarioId']==$JUAN_ID for c in d) else 0)")"

req GET "/citas/$ID_B" "$JUAN_T"
check "Un dueño no puede ver la cita de otra persona (403)" 403

req GET /citas "$RECEP_T"
verificar "La recepcionista ve las citas de todos" "$(echo "$RESP" | python3 -c "import sys,json;d=json.load(sys.stdin);print(1 if len({c['usuarioId'] for c in d})>=2 else 0)")"

req GET "/citas?estado=pendiente" "$RECEP_T"
check "Filtrar citas por estado (200)" 200

req GET "/citas?estado=inexistente" "$RECEP_T"
check "Filtrar por un estado inválido (400)" 400

req PATCH "/citas/$ID_B/reagendar" "$RECEP_T" "{\"fecha\":\"$FECHA\",\"hora\":\"10:00\"}"
check "Reagendar a un horario ocupado (409)" 409

req PATCH "/citas/$ID_B/reagendar" "$RECEP_T" "{\"fecha\":\"$FECHA\",\"hora\":\"10:30\"}"
check "Reagendar a un horario libre (200)" 200 '"estado":"reagendada"'

req PATCH "/citas/$ID_A/cancelar" "$JUAN_T"
check "El dueño cancela su propia cita (200)" 200 '"estado":"cancelada"'

req PATCH "/citas/$ID_A/cancelar" "$JUAN_T"
check "Cancelar una cita ya cancelada (409)" 409

req PATCH "/citas/$ID_A/confirmar" "$RECEP_T"
check "Confirmar una cita cancelada (409)" 409

req PATCH "/citas/$ID_B/cancelar" "$JUAN_T"
check "Un dueño no puede cancelar la cita de otra persona (403)" 403

req DELETE /servicios/1 "$ADMIN_T"
check "Eliminar un servicio que tiene citas asociadas (409)" 409

# Limpieza: libera el horario de la cita de prueba que quedó activa
req PATCH "/citas/$ID_B/cancelar" "$A_T"

# ---------- resumen ----------
echo
echo "=============================================================="
echo "RESUMEN: $TOTAL pruebas, $((TOTAL - FALLAS)) correctas, $FALLAS con falla"
echo "=============================================================="
echo "Datos de prueba (prefijos PRB- y prueba-). Para limpiarlos:"
echo "  sudo mysql veterinaria_db -e \"DELETE FROM citas WHERE mascota LIKE 'PRB-%'; DELETE FROM usuarios WHERE correo LIKE 'prueba-%@test.cl'; DELETE FROM servicios WHERE codigo LIKE 'PRB%';\""
rm -f "$TMP"
[ "$FALLAS" = 0 ]
