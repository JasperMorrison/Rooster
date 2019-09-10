package com.blikoon.rooster;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gakwaya on 4/16/2016.
 */
public class ContactModel {

    private static ContactModel sContactModel;
    private List<Contact> mContacts;

    public static ContactModel get(Context context)
    {
        if(sContactModel == null)
        {
            sContactModel = new ContactModel(context);
        }
        return  sContactModel;
    }

    private ContactModel(Context context)
    {
        mContacts = new ArrayList<>();
        populateWithInitialContacts(context);

    }

    private void populateWithInitialContacts(Context context)
    {
        //Create the Foods and add them to the list;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String owner = prefs.getString("xmpp_jid",null);

        Contact contact1 = new Contact("a", getDomain(context));
        mContacts.add(contact1);
        Contact contact2 = new Contact("b", getDomain(context));
        mContacts.add(contact2);
//        Contact contact3 = new Contact("User3@server.com");
//        mContacts.add(contact3);
//        Contact contact4 = new Contact("User4@server.com");
//        mContacts.add(contact4);
//        Contact contact5 = new Contact("User5@server.com");
//        mContacts.add(contact5);
//        Contact contact6 = new Contact("User6@server.com");
//        mContacts.add(contact6);
//        Contact contact7 = new Contact("User7@server.com");
//        mContacts.add(contact7);

        for (Contact contact : mContacts){
            if (contact.getName().equals(owner)){
                mContacts.remove(contact);
                break;
            }
        }
    }

    private String getDomain(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                .getString("xmpp_domain", null);
    }

    public List<Contact> getContacts()
    {
        return mContacts;
    }

}
