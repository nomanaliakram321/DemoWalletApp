package com.nduwallet.wallet.ui.sessions.active

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.nduwallet.sample_common.BottomVerticalSpaceItemDecoration
import com.nduwallet.sample_common.viewBinding
import com.nduwallet.wallet.R
import com.nduwallet.wallet.databinding.FragmentActiveSessionsBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ActiveSessionsFragment : Fragment(R.layout.fragment_active_sessions) {
    private val binding: FragmentActiveSessionsBinding by viewBinding(FragmentActiveSessionsBinding::bind)
    private val viewModel: ActiveSessionViewModel by viewModels()
    private val activeSessionsAdapter by lazy { ActiveSessionsAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.root) {
            adapter = activeSessionsAdapter
            addItemDecoration(BottomVerticalSpaceItemDecoration(16))
        }

        viewModel.activeSessionUI
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { listOfLatestSessions ->
                activeSessionsAdapter.submitList(listOfLatestSessions)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}