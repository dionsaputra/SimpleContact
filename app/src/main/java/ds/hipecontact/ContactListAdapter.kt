package ds.hipecontact

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_contact.view.*

class ContactListAdapter(
    private var data: List<Contact>,
    private val onItemClick: ((Contact) -> Unit)? = null
) : RecyclerView.Adapter<ContactListAdapter.ContactHolder>() {

    private val itemLayout = R.layout.item_contact

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ContactHolder(inflater.inflate(itemLayout, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        holder.bind(data[position], onItemClick)
    }

    fun swapData(data: List<Contact>) {
        this.data = data
        notifyDataSetChanged()
    }

    class ContactHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun bind(item: Contact, onItemClick: ((Contact) -> Unit)?) = with(itemView) {
            contactName.text = item.name
            contactPhone.text = "Phone: ${item.phone}"
            contactEmail.text = "Email: ${item.email}"

            onItemClick?.let { setOnClickListener { it(item) } }
        }

    }
}