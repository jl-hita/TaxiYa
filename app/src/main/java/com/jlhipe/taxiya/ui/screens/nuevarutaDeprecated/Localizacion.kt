package com.jlhipe.taxiya.ui.screens.nuevarutaDeprecated

/*
@SuppressLint("MissingPermission")
@Composable
fun Localizacion() {

    Text("Ubicación: ")
    CurrentLocationContent(true)
}

@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)

@Composable
fun CurrentLocationContent(usePreciseLocation: Boolean) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var locationInfo by remember {
        mutableStateOf("")
    }

    Column(
        Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = {
                // getting last known location is faster and minimizes battery usage
                // This information may be out of date.
                // Location may be null as previously no client has access location
                // or location turned of in device setting.
                // Please handle for null case as well as additional check can be added before using the method
                scope.launch(Dispatchers.IO) {
                    val result = locationClient.lastLocation.await()
                    locationInfo = if (result == null) {
                        "No last known location. Try fetching the current location first"
                    } else {
                        "Current location is \n" + "lat : ${result.latitude}\n" +
                                "long : ${result.longitude}\n" + "fetched at ${System.currentTimeMillis()}"
                    }
                }
            },
        ) {
            Text("Get last known location")
        }

        Button(
            onClick = {
                //To get more accurate or fresher device location use this method
                scope.launch(Dispatchers.IO) {
                    val priority = if (usePreciseLocation) {
                        Priority.PRIORITY_HIGH_ACCURACY
                    } else {
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY
                    }
                    val result = locationClient.getCurrentLocation(
                        priority,
                        CancellationTokenSource().token,
                    ).await()
                    result?.let { fetchedLocation ->
                        locationInfo =
                            "Current location is \n" + "lat : ${fetchedLocation.latitude}\n" +
                                    "long : ${fetchedLocation.longitude}\n" + "fetched at ${System.currentTimeMillis()}"
                    }
                }
            },
        ) {
            Text(text = "Get current location")
        }
        Text(
            text = locationInfo,
        )
    }
}*/