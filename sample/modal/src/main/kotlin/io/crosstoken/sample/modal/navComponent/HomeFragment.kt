package io.crosstoken.sample.modal.navComponent

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import io.crosstoken.sample.modal.R
import io.crosstoken.sample.modal.databinding.FragmentHomeBinding
import io.crosstoken.sample.common.viewBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val binding by viewBinding(FragmentHomeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}