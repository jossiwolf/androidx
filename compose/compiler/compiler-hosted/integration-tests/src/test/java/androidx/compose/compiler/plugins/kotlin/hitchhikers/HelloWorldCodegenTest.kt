package androidx.compose.compiler.plugins.kotlin.hitchhikers

import androidx.compose.compiler.plugins.kotlin.AbstractCodegenTest
import androidx.compose.compiler.plugins.kotlin.AbstractLoweringTests
import androidx.compose.compiler.plugins.kotlin.ComposeIrTransformTest
import androidx.compose.compiler.plugins.kotlin.compose
import org.junit.Test

class HelloWorldCodegenTest: ComposeIrTransformTest() {

    @Test
    fun testTransformHelloWorldExample() = ensureSetup {
        testCompile("""
            import androidx.compose.runtime.*
            
            @Composable
            fun HelloWorld() {
                println("Hello")
            }
        """.trimIndent(), true)
    }
}
