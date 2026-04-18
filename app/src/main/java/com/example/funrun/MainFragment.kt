package com.example.funrun

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.funrun.databinding.FragmentMainBinding
import com.example.funrun.databinding.FragmentSettingsDialogBinding
import com.example.runlibrary.Run
import java.util.Calendar

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingsIcon.setOnClickListener { openSettingsDialog() }
        binding.addIcon.setOnClickListener { showAddRunDialog() }
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply the top inset as padding so the content shifts below the status bar
            v.updatePadding(top = insets.top)
            windowInsets
        }
    }

    // Refresh data every time the fragment becomes visible
    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun refreshUI() {
        val app = requireActivity().application as MyApplication
        val runList = app.getAllRuns()
        val weeklyGoal = app.getWeeklyGoal() // use saved goal, not hardcoded
        val lastWeekRuns = filterRunsFromLastWeek(runList)

        val totalDistance = lastWeekRuns.sumOf { it.distance }.toFloat()
        val progressPercentage = ((totalDistance / weeklyGoal) * 100).toInt().coerceAtMost(100)

        binding.circularProgressBar.progress = progressPercentage.toFloat()
        binding.progressPercentageText.text = "$progressPercentage%"

        // Best stats this week
        val longestDistance = lastWeekRuns.maxOfOrNull { it.distance } ?: 0.0
        val bestPace = lastWeekRuns.minOfOrNull { it.pace } ?: 0.0
        val longestDuration = lastWeekRuns.maxOfOrNull { it.duration } ?: 0L

        binding.longestDistanceValue.text = "%.2f km".format(longestDistance)
        binding.bestPaceValue.text = "%.2f".format(bestPace)
        binding.longestDurationValue.text = "${longestDuration / 60000}"
    }

    private fun filterRunsFromLastWeek(runList: List<Run>): List<Run> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val oneWeekAgo = calendar.timeInMillis
        return runList.filter { it.timestamp >= oneWeekAgo }
    }

    private fun openSettingsDialog() {
        val dialogBinding = FragmentSettingsDialogBinding.inflate(layoutInflater)
        val app = requireActivity().application as MyApplication

        dialogBinding.darkModeSwitch.isChecked = app.isDarkModeEnabled()
        dialogBinding.weeklyGoalInput.setText(app.getWeeklyGoal().toString())

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val isDarkMode = dialogBinding.darkModeSwitch.isChecked
                app.setDarkMode(isDarkMode)
                AppCompatDelegate.setDefaultNightMode(
                    if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )

                val weeklyGoal = dialogBinding.weeklyGoalInput.text.toString().toFloatOrNull() ?: 50.0f
                app.setWeeklyGoal(weeklyGoal)
                refreshUI()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddRunDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_run, null)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val distanceInput = dialogView.findViewById<EditText>(R.id.etDistance).text.toString()
                val durationInput = dialogView.findViewById<EditText>(R.id.etDuration).text.toString()
                val paceInput = dialogView.findViewById<EditText>(R.id.etPace).text.toString()

                if (distanceInput.isNotEmpty() && durationInput.isNotEmpty() && paceInput.isNotEmpty()) {
                    val distance = distanceInput.toDoubleOrNull()
                    val durationMinutes = durationInput.toDoubleOrNull()
                    val pace = paceInput.toDoubleOrNull()

                    if (distance == null || durationMinutes == null || pace == null) {
                        Toast.makeText(requireContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // Convert minutes to milliseconds for storage
                    val durationMs = (durationMinutes * 60 * 1000).toLong()
                    val run = Run(pace, distance, durationMs, System.currentTimeMillis())
                    (requireActivity().application as MyApplication).addRun(run)

                    refreshUI()
                    Toast.makeText(requireContext(), "Run added!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
}