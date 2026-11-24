# CallShield

App Android que rechaza sin sonar las llamadas de números que no están en tus contactos (incluidos los números ocultos), con un interruptor y pausas temporales para cuando esperas una entrega.

## Cómo abrirla y probarla

1. Instala [Android Studio](https://developer.android.com/studio).
2. `File > Open` y elige esta carpeta (`callshield`). Deja que sincronice Gradle (necesita internet la primera vez).
3. Conecta tu teléfono con **depuración USB** activada (o usa un emulador) y pulsa **Run**.
4. En la app: concede el permiso de contactos y elige CallShield como app de identificación de llamadas y spam.
5. Enciende el interruptor.

Para generar un APK que puedas instalar: `Build > Build Bundle(s) / APK(s) > Build APK(s)`.

## Cómo funciona

- `CallFilterService` usa la API `CallScreeningService` de Android. Por cada llamada entrante consulta tus contactos; si el número no está (o viene oculto), la rechaza y omite la notificación.
- Las llamadas bloqueadas **sí quedan en el historial** del teléfono, por si luego quieres revisar alguna.
- Las llamadas de emergencia nunca pasan por este filtro.
- Si falta el permiso de contactos o hay algún error, la llamada **se deja pasar** (mejor un spam de más que perder una llamada importante).

## Pausa para entregas

Con el filtro encendido aparecen botones para pausarlo 2, 6 o 24 horas. Al terminar el tiempo se reactiva solo, así no dependes de acordarte. También puedes reactivarlo a mano con "Ya llegó mi paquete".

## Limitaciones

- Una llamada de un número que guardaste con un formato muy distinto puede no coincidir; revisa que tus contactos tengan el número completo.
- Algunos fabricantes (Xiaomi, Huawei, etc.) matan apps en segundo plano; si notas que no filtra, desactiva el ahorro de batería para CallShield.
- Mínimo Android 10 (API 29).

## Ideas para después

- Lista blanca de números que no son contactos (ej. el banco).
- Horario automático (bloquear solo de noche).
- Lista negra por prefijos de call centers.
