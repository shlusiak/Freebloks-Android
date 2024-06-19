package de.saschahlusiak.freebloks.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.theme.dimensions

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
            val cornerRadius = MaterialTheme.dimensions.dialogCornerRadius
            Surface(
                shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
            ) {
                StatisticsContent(
                    modifier = Modifier.padding(top = 16.dp),
                    gameMode = viewModel.gameMode.collectAsState().value,
                    data = viewModel.data.collectAsState(null).value,
                    onGameMode = { viewModel.gameMode.value = it },
                    onReset = ::onResetStatistic
                )
            }
        }
    }

    private fun onResetStatistic() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setMessage(R.string.clear_statistics_question)
            setPositiveButton(android.R.string.ok) { d, _ -> viewModel.clear() }
            setNegativeButton(android.R.string.cancel, null)
            show()
        }
    }
}