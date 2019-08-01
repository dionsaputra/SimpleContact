package ds.hipecontact

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_contact_list.*
import kotlinx.android.synthetic.main.dialog_add_contact.view.*

class ContactListActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ContactListActivity::class.java))
        }
    }

    private lateinit var contactDatabase: ContactDatabase

    private lateinit var contactListAdapter: ContactListAdapter

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)

        contactDatabase = Room.databaseBuilder(this, ContactDatabase::class.java, getString(R.string.db_name)).build()
        contactListAdapter = ContactListAdapter(kotlin.collections.emptyList()) {
            val options = android.widget.ArrayAdapter<kotlin.String>(this, android.R.layout.simple_list_item_1).apply {
                add(getString(ds.hipecontact.R.string.call))
                add(getString(ds.hipecontact.R.string.send_email))
                add(getString(ds.hipecontact.R.string.edit))
                add(getString(ds.hipecontact.R.string.delete))
            }

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setAdapter(options) { dialog, which ->
                    when (which) {
                        0 -> callContact(it)
                        1 -> mailContact(it)
                        2 -> editContact(it)
                        3 -> deleteContact(it)
                    }
                    dialog.dismiss()
                }.show()
        }

        contactListRecycler.apply {
            layoutManager = LinearLayoutManager(this@ContactListActivity)
            adapter = contactListAdapter
        }

        retrieveContact()
        contactListFab.setOnClickListener { showAddContactDialog() }
    }

    @SuppressLint("CheckResult")
    private fun retrieveContact() {
        contactDatabase.getContactDao()
            .contactList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterTerminate { }
            .subscribe(
                { showContactList(it) },
                { showToast(it.message.orEmpty()) }
            )
    }

    private fun deleteContact(it: Contact) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_contact))
            .setMessage("Are you sure to delete " + it.name + "'s contact?")
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                performDeleteContact(it)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    @SuppressLint("InflateParams")
    private fun editContact(it: Contact) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
        view.addContactName.setText(it.name)
        view.addContactEmail.setText(it.email)
        view.addContactPhone.setText(it.phone)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.edit_contact))
            .setView(view)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                val name = view.addContactName.text.toString().trim()
                val phone = view.addContactPhone.text.toString().trim()
                val email = view.addContactEmail.text.toString().trim()

                if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                    showToast(getString(R.string.empty_form_error_message))
                } else {
                    performEditContact(Contact(name, phone, email, it.contactId))
                    dialog.dismiss()
                }
            }.show()
    }

    @SuppressLint("CheckResult")
    private fun performEditContact(contact: Contact) {
        contactDatabase.getContactDao()
            .update(contact)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    showToast(getString(R.string.contact_edit_success))
                    retrieveContact()
                },
                { showToast(it.message.orEmpty()) }
            )
    }

    private fun mailContact(it: Contact) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(it.email))
            type = "message/rfc822"
        }
        startActivity(Intent.createChooser(intent, getString(R.string.send_email_using)))
    }

    private fun callContact(it: Contact) {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", it.phone, null)))
    }

    @SuppressLint("CheckResult")
    private fun performDeleteContact(contact: Contact) {
        contactDatabase.getContactDao()
            .delete(contact)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    showToast(getString(R.string.delete_contact_success))
                    retrieveContact()
                },
                { showToast(it.message.orEmpty()) }
            )
    }

    private fun showContactList(contactList: List<Contact>?) {
        if (contactList.isNullOrEmpty()) {
            contactListEmptyText.visibility = View.VISIBLE
            contactListRecycler.visibility = View.GONE
            contactListAdapter.swapData(emptyList())
        } else {
            contactListEmptyText.visibility = View.GONE
            contactListRecycler.visibility = View.VISIBLE
            contactListAdapter.swapData(contactList)
        }
    }

    private fun showContactListProgress() {
        contactListProgress.visibility = View.VISIBLE
        contactListEmptyText.visibility = View.GONE
        contactListRecycler.visibility = View.GONE
    }

    @SuppressLint("InflateParams")
    private fun showAddContactDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_new_contact))
            .setView(view)
            .setPositiveButton("Add") { dialog, _ ->
                val name = view.addContactName.text.toString().trim()
                val phone = view.addContactPhone.text.toString().trim()
                val email = view.addContactEmail.text.toString().trim()

                if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                    showErrorDialog(getString(R.string.empty_form_error_message))
                } else {
                    saveContact(name, phone, email)
                    dialog.dismiss()
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    @SuppressLint("CheckResult")
    private fun saveContact(name: String, phone: String, email: String) {
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {

        } else {
            contactDatabase.getContactDao()
                .insert(Contact(name, phone, email))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showToast(getString(R.string.add_contact_success))
                    retrieveContact()
                }, { showToast(it.message.orEmpty()) })
        }
    }

    private fun showToast(message: String) {
        if (message.isNotEmpty()) Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(getString(R.string.close)) { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
