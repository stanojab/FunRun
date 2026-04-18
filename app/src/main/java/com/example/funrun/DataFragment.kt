package com.example.funrun

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.example.runlibrary.Run

class DataFragment : Fragment(R.layout.fragment_data) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View
    private lateinit var tvTotalRuns: TextView
    private lateinit var tvTotalDistance: TextView
    private lateinit var runAdapter: RunAdapter
    private val runs = mutableListOf<Run>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewRuns)
        emptyState = view.findViewById(R.id.emptyState)
        tvTotalRuns = view.findViewById(R.id.tvTotalRuns)
        tvTotalDistance = view.findViewById(R.id.tvTotalDistance)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        runAdapter = RunAdapter(runs)
        recyclerView.adapter = runAdapter

        setupSwipeToDelete()
    }

    // Refresh every time the tab is opened
    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        val app = requireActivity().application as MyApplication
        val allRuns = app.getAllRuns().sortedByDescending { it.timestamp } // newest first

        runs.clear()
        runs.addAll(allRuns)
        runAdapter.notifyDataSetChanged()

        // Update total stats header
        tvTotalRuns.text = "${allRuns.size}"
        tvTotalDistance.text = "%.2f km".format(allRuns.sumOf { it.distance })

        // Show/hide empty state
        emptyState.visibility = if (allRuns.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (allRuns.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val app = requireActivity().application as MyApplication

                // Find and delete the run by matching the displayed run
                val runToDelete = runs[position]
                val globalIndex = app.getAllRuns().indexOfFirst {
                    it.timestamp == runToDelete.timestamp && it.distance == runToDelete.distance
                }
                if (globalIndex >= 0) {
                    app.deleteRun(globalIndex)
                }

                runs.removeAt(position)
                runAdapter.notifyItemRemoved(position)

                // Update stats after delete
                tvTotalRuns.text = "${runs.size}"
                tvTotalDistance.text = "%.2f km".format(runs.sumOf { it.distance })

                if (runs.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            }

            // Draw red delete background while swiping
            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val paint = Paint().apply { color = Color.parseColor("#FF4757") }
                val background = RectF(
                    itemView.right + dX, itemView.top.toFloat(),
                    itemView.right.toFloat(), itemView.bottom.toFloat()
                )
                c.drawRoundRect(background, 16f, 16f, paint)

                // Draw trash icon text
                val textPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 40f
                    textAlign = Paint.Align.CENTER
                }
                val textX = itemView.right - 60f
                val textY = (itemView.top + itemView.bottom) / 2f + 15f
                c.drawText("🗑", textX, textY, textPaint)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)
    }
}