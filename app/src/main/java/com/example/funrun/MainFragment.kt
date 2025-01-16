package com.example.funrun

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.example.funrun.databinding.FragmentMainBinding
import com.example.funrun.databinding.FragmentSettingsDialogBinding
import com.example.runlibrary.Run
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var binding: FragmentMainBinding
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using view binding
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingsIcon.setOnClickListener {
            openSettingsDialog()
        }

        val app = requireActivity().application as MyApplication
        val runList = app.getAllRuns() // gets all runs from the global class MyApp

        val weeklyGoal = 50.0f
        val lastWeekRuns = filterRunsFromLastWeek(runList)

        val totalDistance= lastWeekRuns.sumOf { it.distance }

        val progressPercentage = ((totalDistance / weeklyGoal) * 100).toInt().coerceAtMost(100)

        binding.circularProgressBar.progress = progressPercentage
        binding.progressPercentageText.text = "$progressPercentage%"

        // Calculate metrics
        val longestDistance = lastWeekRuns.maxOfOrNull { it.distance } ?: 0f
        val bestPace = lastWeekRuns.minOfOrNull { it.pace } ?: 0f
        val longestDuration = lastWeekRuns.maxOfOrNull { it.duration } ?: 0L

        // Populate cards
        binding.longestDistanceValue.text = "%.1f km".format(longestDistance)
        binding.bestPaceValue.text = "%.1f min/km".format(bestPace)
        binding.longestDurationValue.text = "${longestDuration / 60000} min"
    }
    private fun filterRunsFromLastWeek(runList: List<Run>): List<Run> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val oneWeekAgo = calendar.timeInMillis

        return runList.filter { it.timestamp >= oneWeekAgo }
    }
    private fun openSettingsDialog() {
        // Inflate the settings dialog layout
        val dialogBinding = FragmentSettingsDialogBinding.inflate(layoutInflater)

        // Create and display the dialog
        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}