package cf.bautroixa.tripgether.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.ArrayList;

public class ContactHelper {
    public static ArrayList<Contact> getAllContacts(Context context) {
        ArrayList<Contact> contacts = new ArrayList<>();
        String[] projections = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
        };
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projections, null, null, null);
        if (cursor == null) return contacts;
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(projections[0]));
            String phoneNumber = cursor.getString(cursor.getColumnIndex(projections[1]));
            String photoUriString = cursor.getString(cursor.getColumnIndex(projections[2]));
            if (photoUriString != null) {
                Uri photoUri = Uri.parse(photoUriString);
                contacts.add(new Contact(name, phoneNumber, photoUri));
            } else {
                contacts.add(new Contact(name, phoneNumber, null));
            }
        }
        return contacts;
    }

    public static class Contact {
        String name, phoneNumber;
        Uri avatar;

        Contact(String name, String phoneNumber, Uri avatar) {
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.avatar = avatar;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public Uri getAvatar() {
            return avatar;
        }

        public void setAvatar(Uri avatar) {
            this.avatar = avatar;
        }

        public String getShortName() {
            String[] names = getName().split(" ");
            if (names.length >= 2) {
                return "" + names[0].charAt(0) + names[names.length - 1].charAt(0);
            } else {
                return getName();
            }
        }
    }
}
