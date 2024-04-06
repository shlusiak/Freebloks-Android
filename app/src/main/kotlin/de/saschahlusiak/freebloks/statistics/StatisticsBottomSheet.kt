package de.saschahlusiak.freebloks.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.app.AppTheme

@AndroidEntryPoint
class StatisticsBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view as ComposeView
        view.setContent { Content() }
    }

    @Composable
    private fun Content() {
        AppTheme {
            StatisticsContent(
                modifier = Modifier.padding(top = 16.dp),
                gameMode = viewModel.gameMode.collectAsState().value,
                data = viewModel.data.collectAsState().value,
                onGameMode = { viewModel.gameMode.value = it }
            )
        }
    }
}