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

package androidx.navigation.compose.material.demos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.material.ModalBottomSheetLayout
import androidx.navigation.compose.material.bottomSheet
import androidx.navigation.compose.material.rememberBottomSheetNavigator
import androidx.navigation.compose.rememberNavController
import androidx.navigation.plusAssign
import java.util.UUID

private object Destinations {
    const val Home = "HOME"
    const val Feed = "FEED"
    const val Sheet = "SHEET"
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetNavDemo() {
    val navController = rememberNavController()
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    navController.navigatorProvider += bottomSheetNavigator
    ModalBottomSheetLayout(bottomSheetNavigator) {
        NavHost(navController, Destinations.Home) {
            composable(Destinations.Home) {
                HomeScreen(
                    showSheet = {
                        navController.navigate(Destinations.Sheet + "?arg=From Home Screen")
                    },
                    showFeed = { navController.navigate(Destinations.Feed) }
                )
            }
            composable(Destinations.Feed) { Text("Feed!") }
            bottomSheet(Destinations.Sheet + "?arg={arg}") { backstackEntry ->
                val arg = backstackEntry.arguments?.get("arg") as String
                BottomSheet(
                    showFeed = { navController.navigate(Destinations.Feed) },
                    showAnotherSheet = {
                        navController.navigate(Destinations.Sheet + "?arg=${UUID.randomUUID()}")
                    },
                    arg = arg
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(showSheet: () -> Unit, showFeed: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Body")
        Button(onClick = showSheet) {
            Text("Show sheet!")
        }
        Button(onClick = showFeed) {
            Text("Navigate to Feed")
        }
    }
}

@Composable
private fun BottomSheet(showFeed: () -> Unit, showAnotherSheet: () -> Unit, arg: String) {
    Text("Sheet with arg: $arg")
    Button(onClick = showFeed) {
        Text("Click me to navigate!")
    }
    Button(onClick = showAnotherSheet) {
        Text("Click me to show another sheet!")
    }
}
