package com.example.learn_opencv.adapters
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.learn_opencv.PuzzleData


private const val TAG = "PuzzleCardAdapter"

class PuzzleCardAdapter (
        private val context: Context?
    ): ListAdapter<PuzzleData, PuzzleCardAdapter.PuzzleCardViewHolder>(PuzzleComparator()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PuzzleCardViewHolder {
        return PuzzleCardViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: PuzzleCardViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.id)
        holder.button.setOnClickListener {
            Log.i(TAG,"navigating to solveFragment")
            //it.findNavController()
            val bundle = Bundle()
            bundle.putInt("puzzle_id",position)
            it.findNavController().navigate(com.example.learn_opencv.R.id.solveFragment,bundle)
      }
    }

    //override fun

    class PuzzleCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(com.example.learn_opencv.R.id.item_puzzle_name)
        private val itemImage: ImageView = itemView.findViewById(com.example.learn_opencv.R.id.item_image)
        val button: Button = itemView.findViewById(com.example.learn_opencv.R.id.item_button)

        fun bind(text: String?) {
            itemName.text = text
            itemImage.setImageResource(com.example.learn_opencv.R.drawable.lena)
        }

        companion object {
            fun create(parent: ViewGroup): PuzzleCardViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(com.example.learn_opencv.R.layout.puzzle_card, parent, false)
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