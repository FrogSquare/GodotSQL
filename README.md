
# GodotSQL

godot sql is a sql bridge for android

# Usage Api
```

[android]
modules="org/godotengine/godot/SQLBridge"

var sqlbridge = Globals.get_singleton("SQLBridge")

sqlbridge.setBool("key", True/False) 
sqlbridge.getBool("key") // returns bool

sqlbridge.setInt("key", Int)
sqlbridge.getInt("key") // returns int

sqlbridge.setFloat("key", Float)
sqlbridge.getFloat("key") // returns float

sqlbridge.setValue("key", "value") // set { key = value }.
sqlbridge.getValue("key") // returns String.

```

# Log events
```
adb -d logcat SQLBridge:V DEBUG:V AndroidRuntime:V *:S
```
