/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.api

import java.io.File

// TODO KT-48758 get rid of this wrapper and move to reading compilation configuration from evaluation one
class FileWrapper {
    private var underlyingFile: File? = null

    fun get(): File? = underlyingFile
    fun set(file: File?) {
        underlyingFile = file
    }
}
