package com.example.marvelapp.ui.HomeFragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.marvelapp.base.BaseFragment
import com.example.marvelapp.data.entity.ResultsData
import com.example.marvelapp.databinding.FragmentHomeBinding
import com.example.marvelapp.ui.HomeFragment.HomeAdapter
import com.example.marvelapp.ui.HomeFragment.HomeFragmentViewModel
import com.example.marvelapp.utils.Constants
import com.example.marvelapp.utils.Resource
import com.example.marvelapp.utils.showDialog
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel : HomeFragmentViewModel by viewModels()
    private val adapter by lazy { HomeAdapter() }
    private var totalCount = 0
    private var offset = Constants.offset
    private val heroList = arrayListOf<ResultsData>()

    override fun onStart() {
        super.onStart()
        showBottomBar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.heroRecyclerView.adapter = adapter
        binding.heroRecyclerView.layoutManager = GridLayoutManager(requireContext(),2)
        onScrollListener()
    }

    override fun onResume() {
        super.onResume()
        getHero(offset)
    }

    private fun getHero(offset : Int){
        viewModel.getHero(offset).observe(viewLifecycleOwner){response ->
            when(response.status){
                Resource.Status.SUCCESS ->{
                    totalCount = response.data?.characters?.total ?: 0
                    heroList.addAll(response.data?.characters?.results ?: arrayListOf())
                    val scrollDistance = heroList.size - (response.data?.characters?.results?.size ?: 0)
                    binding.heroRecyclerView.scrollToPosition(scrollDistance)
                    setData()
                }
                Resource.Status.ERROR -> {
                    showDialog(requireContext(), message = "${response.message}")
                }
                Resource.Status.LOADING ->{

                }
            }

        }
    }

    private fun onScrollListener(){
        binding.heroRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!binding.heroRecyclerView.canScrollHorizontally(1)&&
                        newState == RecyclerView.SCROLL_STATE_IDLE &&
                        adapter.itemCount < totalCount
                ){
                    offset += 1
                    getHero(offset)
                }
            }
        })
    }

    private fun setData(){
        adapter.updateHeroList(heroList)
    }

}