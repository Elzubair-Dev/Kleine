package com.example.kleineyt.fragments.shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kleineyt.R
import com.example.kleineyt.adapters.SearchAdapter
import com.example.kleineyt.databinding.FragmentSearchBinding
import com.example.kleineyt.util.Resource
import com.example.kleineyt.util.showNavigationView
import com.example.kleineyt.viewmodel.SearchViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "SearchFragment"

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchAdapter: SearchAdapter
    private val viewModel by viewModels<SearchViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.searchBtn.setOnClickListener {
            val query: String = binding.searchET.text.toString()
            if (query.isNotEmpty()) {

                viewModel.fetchSearchProducts(query)

                showResult()
            }
        }
    }

    private fun showResult() {
        setupSearchProductsRv()

        searchAdapter.onClick = {
            val productBundle = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(
                R.id.action_searchFragment_to_productDetailsFragment,
                productBundle
            )
        }
        lifecycleScope.launchWhenStarted {
            viewModel.searchProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.searchProgressBar.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        searchAdapter.differ.submitList(it.data)
                        binding.searchProgressBar.visibility = View.GONE
                    }

                    is Resource.Error -> {
                        binding.searchProgressBar.visibility = View.GONE
                        Log.e(TAG, it.message.toString())
                        error()
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun setupSearchProductsRv() {
        searchAdapter = SearchAdapter()
        binding.rvBestProducts.apply {
            layoutManager = GridLayoutManager(
                requireContext(), 2,
                LinearLayoutManager.VERTICAL, false
            )
            adapter = searchAdapter
        }
    }

    private fun error() {
        Snackbar.make(requireView(), "Check your connection", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        showNavigationView()
    }

    override fun onStop() {
        super.onStop()
        binding.searchET.text.clear()
    }
}