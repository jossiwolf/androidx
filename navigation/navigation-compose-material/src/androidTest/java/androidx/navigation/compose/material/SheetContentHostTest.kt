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

package androidx.navigation.compose.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.TestMonotonicFrameClock
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavBackStackEntry
import androidx.navigation.testing.TestNavigatorState
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalMaterialApi::class, ExperimentalCoroutinesApi::class)
internal class SheetContentHostTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testClock = TestMonotonicFrameClock(CoroutineScope(testDispatcher))

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSheetShownAndHidden() = runBlockingTest {
        var backStackEntry by mutableStateOf<NavBackStackEntry?>(null)
        val sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)

        composeTestRule.setContent {
            ModalBottomSheetLayout(
                sheetContent = {
                    SheetContentHost(
                        columnHost = this,
                        backStackEntry,
                        sheetState,
                        onSheetDismissed = { backStackEntry = null }
                    )
                },
                sheetState = sheetState,
                content = { Box(Modifier.fillMaxSize()) }
            )
        }

        backStackEntry = createBackStackEntry(sheetState)
        composeTestRule.runOnIdle {
            assertWithMessage("Bottom sheet was shown")
                .that(sheetState.isVisible).isTrue()
        }

        backStackEntry = null
        composeTestRule.runOnIdle {
            assertWithMessage("Bottom sheet was hidden")
                .that(sheetState.isVisible).isFalse()
        }
    }

    @Test
    fun testOnSheetDismissedCalled_ManualDismiss(): Unit = runBlockingTest(testClock) {
        val sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val backStackEntry = createBackStackEntry(sheetState)

        val dismissedBackStackEntries = mutableListOf<NavBackStackEntry>()
        val bodyContentTag = "testBodyContent"

        composeTestRule.setContent {
            ModalBottomSheetLayout(
                sheetContent = {
                    SheetContentHost(
                        columnHost = this,
                        backStackEntry,
                        sheetState,
                        onSheetDismissed = { entry -> dismissedBackStackEntries.add(entry) }
                    )
                },
                sheetState = sheetState,
                content = { Box(Modifier.fillMaxSize().testTag(bodyContentTag)) }
            )
        }

        assertThat(sheetState.currentValue == ModalBottomSheetValue.Expanded)
        composeTestRule.onNodeWithTag(bodyContentTag).performClick()
        composeTestRule.runOnIdle {
            assertWithMessage("Sheet should be hidden")
                .that(sheetState.isVisible).isFalse()
            assertWithMessage("Back stack entry should be in the dismissed entries list")
                .that(dismissedBackStackEntries)
                .containsExactly(backStackEntry)
        }
    }

    @Test
    fun testOnSheetDismissedCalled_programmaticDismiss(): Unit = runBlockingTest(testClock) {
        val sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val backStackEntry = createBackStackEntry(sheetState)

        val dismissedBackStackEntries = mutableListOf<NavBackStackEntry>()
        val bodyContentTag = "testBodyContent"

        composeTestRule.setContent {
            ModalBottomSheetLayout(
                sheetContent = {
                    SheetContentHost(
                        columnHost = this,
                        backStackEntry,
                        sheetState,
                        onSheetDismissed = { entry -> dismissedBackStackEntries.add(entry) }
                    )
                },
                sheetState = sheetState,
                content = { Box(Modifier.fillMaxSize().testTag(bodyContentTag)) }
            )
        }

        assertThat(sheetState.currentValue == ModalBottomSheetValue.Expanded)

        testDispatcher.pauseDispatcher {
            launch { sheetState.hide() }
        }

        assertWithMessage("Sheet is visible")
            .that(sheetState.isVisible).isFalse()
        assertWithMessage("Back stack entry should be in the dismissed entries list")
            .that(dismissedBackStackEntries)
            .containsExactly(backStackEntry)
    }

    private fun createBackStackEntry(sheetState: ModalBottomSheetState): NavBackStackEntry {
        val navigatorState = TestNavigatorState()
        val navigator = BottomSheetNavigator(sheetState)
        navigator.onAttach(navigatorState)

        val destination = BottomSheetNavigator.Destination(navigator) {
            Text("Fake Sheet Content")
        }
        val backStackEntry = navigatorState.createBackStackEntry(destination, null)
        navigator.navigate(listOf(backStackEntry), null, null)
        return backStackEntry
    }
}
