package com.example.learn_opencv.adapters

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.learn_opencv.PuzzleData
import com.example.learn_opencv.R
import kotlinx.coroutines.NonDisposableHandle.parent

private const val TAG = "PuzzleCardAdapter"

class PuzzleCardAdapter (
        private val context: Context?,
        //private val dataset: List<PuzzleData>
    ): ListAdapter<PuzzleData, PuzzleCardAdapter.PuzzleCardViewHolder>(PuzzleComparator()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PuzzleCardViewHolder {
        return PuzzleCardViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: PuzzleCardViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.id)
    }

    class PuzzleCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.item_puzzle_name)
        private val itemImage: ImageView = itemView.findViewById(R.id.item_image)

        fun bind(text: String?) {
            itemName.text = text
            itemImage.setImageResource(R.drawable.lena)
        }

        companion object {
            fun create(parent: ViewGroup): PuzzleCardViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.puzzle_card, parent, false)
                return PuzzleCardViewHolder(view)
            }
        }
    }

    class PuzzleComparator : DiffUtil.ItemCallback<PuzzleData>() {
        override fun areItemsTheSame(oldItem: PuzzleData, newItem: PuzzleData): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: PuzzleData, newItem: PuzzleData): Boolean {
            return oldItem.id == newItem.id
        }
    }
}