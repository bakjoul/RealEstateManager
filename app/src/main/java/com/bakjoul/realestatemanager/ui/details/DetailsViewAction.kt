package com.bakjoul.realestatemanager.ui.details

sealed class DetailsViewAction {
    data class OpenPhoto(val photoId: Long) : DetailsViewAction()
}
