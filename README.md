# Real Estate Manager

## About this app
Real Estate Manager is an Android app designed to assist real estate agents in managing the properties they list for sale.\
The app provides basic features such as adding, editing, and listing properties, along with displaying them on a map and filtering them based on various criteria. Additionally, the app features an advanced loan simulator and allows users to store drafts for property creation and edition with an auto-save functionality.

The app is still under development. What's taking me so long?\
Besides life's little surprises, i pride myself on pushing this project further than what is asked, striving to adopt today's **good pratices**.\
Take a look at my code and feel free to give me your feedback!


>[!IMPORTANT]
>The app requires a valid Google API key (for Geocoding, Maps Static and Places APIs, and also Maps SDK), as well as a getgeoapi.com API key to have an up-to-date currency rate.
>[Getgeoapi.com](https://getgeoapi.com/) has a free plan that allows to have a currency rate updated every day.\
>API keys must be added to local.properties (`GOOGLE_API_KEY=[YOUR_KEY]` and `CURRENCY_API_KEY=[YOUR_KEY]`).

## Technical stack
* Fully written in `Kotlin`
* `MVVM architecture` with clear `domain layer` separation
* Dependency injection with `Hilt`
* Local database storage with `Room`
* API calls with `Retrofit`
* Reactive programming with `LiveData`, `Coroutines` and `Flows`
* Camera functionality with `CameraX`
* Includes `custom views` for enhanced UI/UX
* Supports both portrait and landscape orientations
* Unit testing with `MockK`
* XX code coverage (`Kover`)

## Demo
More to come soon...
