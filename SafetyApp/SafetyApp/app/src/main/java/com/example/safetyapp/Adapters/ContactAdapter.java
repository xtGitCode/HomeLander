package com.example.safetyapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.safetyapp.Models.Contacts;
import com.example.safetyapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactHolder> {

    private Context context;
    private List<Contacts> contactsList;
    private FirebaseUser firebaseUser;

    public ContactAdapter(Context context, List<Contacts> contactsList) {
        this.context = context;
        this.contactsList = contactsList;
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contactitem, parent, false);
        return new ContactHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactHolder holder, int position) {
        Contacts contacts = contactsList.get(position);

        holder.contactName.setText(contacts.getContactName());
        holder.contactNum.setText(contacts.getContactNum());

        // Handle delete button click
        holder.deleteContact.setOnClickListener(v -> deleteContact(position));

        // Handle edit button click
        holder.editContact.setOnClickListener(v -> editContact(contacts, position));
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    static class ContactHolder extends RecyclerView.ViewHolder {
        TextView contactName;
        TextView contactNum;
        Button deleteContact, editContact;

        public ContactHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contactName);
            contactNum = itemView.findViewById(R.id.contactNum);
            deleteContact = itemView.findViewById(R.id.btn_deleteContact);
            editContact = itemView.findViewById(R.id.btn_editContact);
        }
    }

    public void deleteContact(int position) {
        Contacts deletedContact = contactsList.remove(position);
        notifyItemRemoved(position);

        // Update Firebase by removing the specific contact
        updateFirebaseDeleteContact(deletedContact);
    }

    private void updateFirebaseDeleteContact(Contacts deletedContact) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("contacts")
                    .child(firebaseUser.getUid());

            // Remove the contact with the specific UID from Firebase
            reference.child(deletedContact.getUid()).removeValue();
        } else {
            Log.e("FirebaseUser", "User is not authenticated");
        }
    }

    public void editContact(Contacts contacts, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Contact");

        // Inflate the layout for the dialog
        View view = LayoutInflater.from(context).inflate(R.layout.edit_contact_dialog, null);
        builder.setView(view);

        // Access the views in the dialog layout
        EditText editName = view.findViewById(R.id.editContactName);
        EditText editNumber = view.findViewById(R.id.editContactNumber);

        // Set the existing contact information
        editName.setText(contacts.getContactName());
        editNumber.setText(contacts.getContactNum());

        // Set up buttons in the dialog
        builder.setPositiveButton("Save", (dialog, which) -> {
            // Retrieve the edited information from the dialog
            String editedName = editName.getText().toString().trim();
            String editedNumber = editNumber.getText().toString().trim();

            // Update the contactsList
            Contacts editedContact = new Contacts(contacts.getUid(), editedName, editedNumber);
            contactsList.set(position, editedContact);

            // Notify the adapter that the data has changed
            notifyItemChanged(position);

            // Update Firebase with the modified contact
            updateFirebaseEditContact(editedContact);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateFirebaseEditContact(Contacts editedContact) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("contacts")
                .child(firebaseUser.getUid());

        // Update the specific contact in Firebase using its UID
        reference.child(editedContact.getUid()).setValue(editedContact);
    }
}
