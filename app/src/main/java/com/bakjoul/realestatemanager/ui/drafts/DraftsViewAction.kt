package com.bakjoul.realestatemanager.ui.drafts

sealed class DraftsViewAction {
    object ShowProgressBar: DraftsViewAction()
    object CloseDialog : DraftsViewAction()
}
