package com.example.funrun

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DataFragment : Fragment(R.layout.fragment_data) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var runAdapter: RunAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewRuns)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Get the list of runs from MyApplication
        val runs = (requireActivity().application as MyApplication).getAllRuns()

        // Set up the adapter
        runAdapter = RunAdapter(runs)
        recyclerView.adapter = runAdapter
    }
}
