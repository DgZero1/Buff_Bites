package com.example.buffbites

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.buffbites.ui.OrderViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.buffbites.data.Datasource
import com.example.buffbites.ui.ChooseDeliveryTimeScreen
import com.example.buffbites.ui.ChooseMenuScreen
import com.example.buffbites.ui.OrderSummaryScreen
import com.example.buffbites.ui.OrderUiState
import com.example.buffbites.ui.StartOrderScreen
import kotlinx.coroutines.selects.select
import javax.sql.DataSource

enum class BuffBitesScreen(@StringRes val title: Int){
    Start(title = R.string.app_name),
    Food(title = R.string.choose_meal),
    Pickup(title = R.string.choose_delivery_time),
    Summary(title = R.string.order_summary)
}

// TODO: Screen enum

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuffBitesAppBar(
    currentScreen: BuffBitesScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuffBitesApp(
    viewModel: OrderViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = BuffBitesScreen.valueOf(
        backStackEntry?.destination?.route ?: BuffBitesScreen.Start.name
    )
    // TODO: Add navigation dependency to gradle script
    // TODO: Create Controller and initialization

    Scaffold(
        topBar = {
            BuffBitesAppBar(
                currentScreen =currentScreen,
                canNavigateBack= navController.previousBackStackEntry !=null,
            navigateUp ={navController.navigateUp()}
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = BuffBitesScreen.Start.name,
            modifier = Modifier.padding(innerPadding)

        )
        {
            composable(route = BuffBitesScreen.Start.name){
                StartOrderScreen(
                    restaurantOptions = Datasource.restaurants,

                    onNextButtonClicked = {
                        viewModel.updateVendor(it)
                        navController.navigate(BuffBitesScreen.Food.name)
                    },




                )

            }
            composable(route= BuffBitesScreen.Food.name) {
                ChooseMenuScreen(
                    options = uiState.selectedMeal,
                    onNextButtonClicked = {navController.navigate(BuffBitesScreen.Pickup.name)
                    },
                    onCancelButtonClicked = {cancelOrderAndNavigateToStart(viewModel, navController)},


                )

            }
            composable(route= BuffBitesScreen.Pickup.name) {
                ChooseDeliveryTimeScreen(
                    options = uiState.availableDeliveryTimes,
                    subtotal = uiState.orderSubtotal,
                    onNextButtonClicked = {navController.navigate(BuffBitesScreen.Summary.name)
                    },
                    onCancelButtonClicked = {cancelOrderAndNavigateToStart(viewModel, navController)},
                )
            }
                composable(route= BuffBitesScreen.Summary.name) {
                   OrderSummaryScreen(
                       orderUiState = uiState,
                       onCancelButtonClicked = {
                           cancelOrderAndNavigateToStart(viewModel, navController)
                       },
                       onSendButtonClicked= {subject: String, summary: String) ->},

                   )

        }

        // TODO: Navigation host

    }
}
}
private fun cancelOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
){
    viewModel.resetOrder()
    navController.popBackStack(BuffBitesScreen.Start.name, inclusive = false)