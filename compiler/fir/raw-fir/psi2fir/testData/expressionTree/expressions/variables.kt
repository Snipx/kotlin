fun foo() = expressionTree {
    val x = 1
    var y = x + 1
    val z = y * 2
    y = y + z
    val w = y - x
    return w
}

fun expressionTree(block: () -> Unit) {
    TODO("intrinsic")
}