package com.example.audriuskoncius.runtimepermissions

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import com.tbruyelle.rxpermissions.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            RxPermissions.getInstance(this)
                    .request(Manifest.permission.READ_CONTACTS, Manifest.permission.CAMERA)
                    .subscribe {
                        if (it) {

                            Log.e("Tag", it.toString())
                            getCursorData().subscribe { readContacts(it) }
                        } else {
                            RxPermissions.getInstance(this)
                                    .shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS, Manifest.permission.CAMERA)
                                    .subscribe {
                                        if (!it) {
                                            Snackbar.make(findViewById(android.R.id.content),
                                                    "You need to enable permissions to use this action",
                                                    Snackbar.LENGTH_LONG)
                                                    .setAction("Settings", View.OnClickListener {
                                                        startActivityForResult(Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS), 0);
                                                    }).show()
                                        }
                                    }
                        }

                    }
        }
    }

    private fun readContacts(list: List<String>) {

        val adapter = ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_list_item_1, list);

        listView.setAdapter(adapter);
        listView.visibility = View.VISIBLE
        button.visibility = View.INVISIBLE
    }

    fun getCursorData(): Observable<List<String>> {
        return Observable
                .create<List<String>> { s ->
                    val contacts = ArrayList<String>()
                    val c = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            null, null, null)
                    while (c.moveToNext()) {
                        val contactName = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                        val phNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        contacts.add(contactName + ":" + phNumber)
                    }
                    c.close()

                    s.onNext(contacts)
                    s.onCompleted()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}
