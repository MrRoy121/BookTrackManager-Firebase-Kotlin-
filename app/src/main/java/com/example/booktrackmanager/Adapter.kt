package com.example.booktrackmanager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import java.util.*
import kotlin.collections.ArrayList

class Adapter(
    private val context: Context,
    private var logos: ArrayList<String>,
    private val log: ArrayList<Boolean>
) : BaseAdapter(), Filterable {

    private var exampleListFull: List<String> = ArrayList(logos)
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return logos.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = inflater.inflate(R.layout.custom, parent, false)
        val icon = view.findViewById<TextView>(R.id.icon)
        val icona = view.findViewById<TextView>(R.id.com)

        icona.text = if (!log[position]) {
            "Unread"
        } else {
            "Read"
        }
        icon.text = logos[position]
        return view
    }

    override fun getFilter(): Filter {
        return exampleFilter
    }

    private val exampleFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<String>()
            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(exampleListFull)
            } else {
                val filterPattern = constraint.toString().toLowerCase(Locale.ROOT).trim()
                for (item in exampleListFull) {
                    if (item.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            logos.clear()
            logos.addAll(results?.values as List<String>)
            notifyDataSetChanged()
        }
    }
}