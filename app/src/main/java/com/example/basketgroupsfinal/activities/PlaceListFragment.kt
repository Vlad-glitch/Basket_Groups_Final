package com.example.basketgroupsfinal.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.basketgroupsfinal.R
import com.example.basketgroupsfinal.adapters.BasketPlacesAdapter
import com.example.basketgroupsfinal.databinding.ActivityMainBinding
import com.example.basketgroupsfinal.databinding.FragmentPlaceListBinding
import com.example.basketgroupsfinal.models.Place
import com.example.basketgroupsfinal.models.PlacesViewModel
import com.example.basketgroupsfinal.utils.Constants

class PlaceListFragment: Fragment() {

    private var binding: FragmentPlaceListBinding? = null
    private lateinit var placesViewModel: PlacesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPlaceListBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.floatingActionButton?.setOnClickListener {
            val intent = Intent(context, AddBasketPlaceActivity::class.java)
            startActivity(intent)
        }

        placesViewModel = ViewModelProvider(requireActivity()).get(PlacesViewModel::class.java)

        placesViewModel.places.observe(viewLifecycleOwner, Observer { places ->
            // Update your RecyclerView with the new places
            setupBasketPlaces(places as ArrayList<Place>?)
        })

    }

    fun setupBasketPlaces(basketPlaceList: ArrayList<Place>?){
        //hideProgressDialog()
        val rvBasketPlaceList: RecyclerView = binding?.root!!.findViewById(R.id.rv_basket_place_list)
        if (basketPlaceList != null) {
            if (basketPlaceList.size > 0) {
                rvBasketPlaceList.visibility = View.VISIBLE
                rvBasketPlaceList.layoutManager = LinearLayoutManager(context)
                rvBasketPlaceList.setHasFixedSize(true)
                val adapter = context?.let { BasketPlacesAdapter(it, basketPlaceList) }
                rvBasketPlaceList.adapter = adapter

                adapter?.setOnClickListener(object: BasketPlacesAdapter.OnClickListener {
                    override fun onClick(position: Int, model: Place) {
                        val intent = Intent(context, PlaceDetailsActivity::class.java)
                        intent.putExtra(Constants.DOCUMENT_ID, model.id)
                        startActivity(intent)
                    }
                })
            } else {
                rvBasketPlaceList.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}