# java2lua

Java code:
```
Globals.print("Pi equals ", LuaMath.PI);
for (double x = 0; x < 10; x += LuaMath.PI) {
    Globals.print("x: " + x, "sin: " + LuaMath.sin(x), "cos: " + LuaMath.cos(x));
}
```
transformed into Lua:
```lua
local _locals = {}
--Starting method <init>()V
do
local _stack = {}
end
--Starting method run()V
do
local _stack = {}
_stack[1] = 2
_stack[2] = {}
_stack[3] = _stack[2]
_stack[4] = 0
_stack[5] = "Pi equals "
_stack[3][1 + _stack[4]] = _stack[5]
_stack[3] = _stack[2]
_stack[4] = 1
_stack[5] = math.pi
_stack[3][1 + _stack[4]] = _stack[5]
print(table.unpack(_stack[2]))
_stack[2] = 0.0
_locals[1] = _stack[2]
::l0::
_stack[2] = _locals[1]
_stack[3] = 10.0
_stack[2] = (function(x, y) if x == y then return 0 elseif x >= y then return -1 else return 1 end end)(_stack[3], _stack[2])
if _stack[2] >= 0 then goto l1 end
_stack[2] = 3
_stack[3] = {}
_stack[4] = _stack[3]
_stack[5] = 0
_stack[6] = {}
_stack[7] = _stack[6]
_stack[7] = "x: "
table.insert(_stack[6], _stack[7])
_stack[7] = _locals[1]
table.insert(_stack[6], _stack[7])
_stack[6] = table.concat(_stack[6], "")
_stack[4][1 + _stack[5]] = _stack[6]
_stack[4] = _stack[3]
_stack[5] = 1
_stack[6] = {}
_stack[7] = _stack[6]
_stack[7] = "sin: "
table.insert(_stack[6], _stack[7])
_stack[7] = _locals[1]
_stack[7] = math.sin(_stack[7])
table.insert(_stack[6], _stack[7])
_stack[6] = table.concat(_stack[6], "")
_stack[4][1 + _stack[5]] = _stack[6]
_stack[4] = _stack[3]
_stack[5] = 2
_stack[6] = {}
_stack[7] = _stack[6]
_stack[7] = "cos: "
table.insert(_stack[6], _stack[7])
_stack[7] = _locals[1]
_stack[7] = math.cos(_stack[7])
table.insert(_stack[6], _stack[7])
_stack[6] = table.concat(_stack[6], "")
_stack[4][1 + _stack[5]] = _stack[6]
print(table.unpack(_stack[3]))
_stack[3] = _locals[1]
_stack[4] = math.pi
_stack[3] = _stack[4] + _stack[3]
_locals[1] = _stack[3]
goto l0
::l1::
end
```
