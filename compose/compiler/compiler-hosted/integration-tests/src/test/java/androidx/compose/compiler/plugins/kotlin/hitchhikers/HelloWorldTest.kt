/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.compiler.plugins.kotlin.hitchhikers

import androidx.compose.compiler.plugins.kotlin.AbstractCodegenTest
import androidx.compose.compiler.plugins.kotlin.AbstractCompilerTest
import androidx.compose.compiler.plugins.kotlin.ComposeIrTransformTest
import org.junit.Test

class HelloWorldTest: ComposeIrTransformTest() {

    @Test
    fun testGenerateHitchhikerHelloWorld() = ensureSetup {
        runIrTransform(
            """
                import androidx.compose.runtime.*                

               @Composable fun App() {
                   var greeting by remember { mutableStateOf("Hia") }
                   HelloWorld(greeting)
               }                

               @Composable fun HelloWorld(greeting: String) {
                   println(greeting)
               }
            """.trimIndent(),
            """
                
            """.trimIndent(),
            dumpActualTransformed = true
        )
    }

}
