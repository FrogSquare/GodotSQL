
# GodotSQL

godot sql is a sql bridge for android

# Usage Api
```

[android]
modules="org/godotengine/godot/SQLBridge"

var sqlbridge = Globals.get_singleton("SQLBridge")

sqlbridge.setValue("key", "value") // set { key = value }.
sqlbridge.getValue("key") // returns String.

```

# Log events
```
adb -d logcat SQLBridge:V DEBUG:V AndroidRuntime:V *:S
```
