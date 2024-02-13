package com.bakjoul.realestatemanager.ui.loan_simulator

import com.bakjoul.realestatemanager.ui.utils.NativeText

sealed class LoanSimulatorViewAction {
    object CloseDialog : LoanSimulatorViewAction()
    data class ShowToast(val message: NativeText) : LoanSimulatorViewAction()
}
