package com.example.tipclik4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kurs.R

class OrderViewAdapter(
    private val onEditClick: (Long, Boolean) -> Unit,
    private val onDeleteClick: (Long) -> Unit,
    private val onItemClick: (Order) -> Unit
) :
    ListAdapter<Order, OrderViewAdapter.OrderViewHolder>(OrderDiffCallback()) {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderDescription: TextView = itemView.findViewById(R.id.orderDescriptionTextView)
        val orderQuantity: TextView = itemView.findViewById(R.id.orderQuantityTextView)
        val orderEditButton: Button = itemView.findViewById(R.id.editOrderButton)
        val orderDeleteButton: Button = itemView.findViewById(R.id.deleteOrderButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.orderDescription.text = order.description
        holder.orderQuantity.text = "Кол-во: " + order.quantity.toString()
        val roleId =  holder.itemView.context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            .getInt("role_id", 0)
        val isManager = roleId == 2
        holder.orderEditButton.visibility = if(isManager) View.VISIBLE else View.GONE
        holder.orderEditButton.setOnClickListener {
            onEditClick(order.id, false)
        }

        holder.orderDeleteButton.setOnClickListener {
            onDeleteClick(order.id)
        }
        holder.itemView.setOnClickListener {
            onItemClick(order)
        }
    }


    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}