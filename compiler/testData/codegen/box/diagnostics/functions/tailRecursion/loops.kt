// DONT_TARGET_EXACT_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// DONT_RUN_GENERATED_CODE: JS
// IGNORE_BACKEND: JS

tailrec fun test(x : Int) : Int {
    var z = if (x > 3) 3 else x
    while (z > 0) {
        if (z > 10) {
            return test(x - 1)
        }
        <!NON_TAIL_RECURSIVE_CALL!>test<!>(0)
        z = z - 1
    }

    return 1
}

fun box() : String = if (test(100000) == 1) "OK" else "FAIL"
