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
            v.updatePadding(top = insets.top)
            windowInsets
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun refreshUI() {
        val app = requireActivity().application as MyApplication
        val allRuns = app.getAllRuns()
        val weeklyGoal = app.getWeeklyGoal()
        val lastWeekRuns = filterRunsFromLastWeek(allRuns)

        // Progress ring — km remaining
        val totalDistance = lastWeekRuns.sumOf { it.distance }.toFloat()
        val remaining = (weeklyGoal - totalDistance).coerceAtLeast(0f)
        val progressPercentage = ((totalDistance / weeklyGoal) * 100).toInt().coerceAtMost(100)
        binding.circularProgressBar.progress = progressPercentage.toFloat()
        binding.progressPercentageText.text = "%.1f".format(remaining)

        // Days left in week
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val daysLeft = (Calendar.SATURDAY - dayOfWeek + 1).coerceAtLeast(0)
        binding.tvDaysLeft.text = "$daysLeft days left"

        // Best stats this week
        val longestDistance = lastWeekRuns.maxOfOrNull { it.distance } ?: 0.0
        val bestPace = lastWeekRuns.filter { it.pace > 0 }.minOfOrNull { it.pace } ?: 0.0
        val longestDuration = lastWeekRuns.maxOfOrNull { it.duration } ?: 0L

        binding.longestDistanceValue.text = "%.2f km".format(longestDistance)
        binding.bestPaceValue.text = "%.2f".format(bestPace)
        binding.longestDurationValue.text = "${longestDuration / 60000}"

        // Streak + calendar
        updateStreakAndCalendar(allRuns)
    }

    private fun updateStreakAndCalendar(allRuns: List<Run>) {
        // Build set of day-start timestamps that had a run
        val runDays = allRuns.map { run ->
            Calendar.getInstance().apply {
                timeInMillis = run.timestamp
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.toSet()

        // Count streak — consecutive days ending today going backwards
        var streak = 0
        val check = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        while (runDays.contains(check.timeInMillis)) {
            streak++
            check.add(Calendar.DAY_OF_YEAR, -1)
        }
        val fire = if (streak > 0) "🔥" else ""
        binding.tvStreakBadge.text = "$streak day streak $fire"

        // Calendar strip — highlight today and mark days with runs
        val todayDow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val dayViews = listOf(
            Triple(binding.dayMon, binding.dotMon, Calendar.MONDAY),
            Triple(binding.dayTue, binding.dotTue, Calendar.TUESDAY),
            Triple(binding.dayWed, binding.dotWed, Calendar.WEDNESDAY),
            Triple(binding.dayThu, binding.dotThu, Calendar.THURSDAY),
            Triple(binding.dayFri, binding.dotFri, Calendar.FRIDAY),
            Triple(binding.daySat, binding.dotSat, Calendar.SATURDAY),
            Triple(binding.daySun, binding.dotSun, Calendar.SUNDAY),
        )
        dayViews.forEach { (container, dot, dow) ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, dow)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            container.setBackgroundResource(
                if (dow == todayDow) R.drawable.bg_day_today else R.drawable.bg_card
            )
            dot.setBackgroundResource(
                if (runDays.contains(cal.timeInMillis)) R.drawable.bg_dot_active else R.drawable.bg_card
            )
        }
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

                if (distanceInput.isNotEmpty() && durationInput.isNotEmpty()) {
                    val distance = distanceInput.toDoubleOrNull()
                    val durationMinutes = durationInput.toDoubleOrNull()

                    if (distance == null || durationMinutes == null) {
                        Toast.makeText(requireContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // pace = min/km = duration(min) / distance(km)
                    val pace = if (distance > 0) durationMinutes / distance else 0.0
                    val durationMs = (durationMinutes * 60 * 1000).toLong()
                    val run = Run(pace, distance, durationMs, System.currentTimeMillis())
                    (requireActivity().application as MyApplication).addRun(run) {
                        refreshUI()
                        Toast.makeText(requireContext(), "Run added!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
}