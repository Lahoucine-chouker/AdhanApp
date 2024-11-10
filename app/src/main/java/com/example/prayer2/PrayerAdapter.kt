package com.example.prayer2


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class PrayerAdapter(private val context: Context, private val prayerTimes: List<String>) : BaseAdapter() {

    // Get the count of items in the prayer times list
    override fun getCount(): Int {
        return prayerTimes.size
    }

    // Get the item at the given position
    override fun getItem(position: Int): Any {
        return prayerTimes[position]
    }

    // Get the item ID (use the position of the item)
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // Create and return the view for each item in the list
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Inflate the layout for each list item
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)

        // Get the TextView to display the prayer time
        val textView: TextView = view.findViewById(android.R.id.text1)

        // Set the prayer time text for the current list item
        textView.text = prayerTimes[position]

        // Return the view
        return view
    }
}
