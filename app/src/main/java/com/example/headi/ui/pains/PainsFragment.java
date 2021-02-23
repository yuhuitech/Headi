package com.example.headi.ui.pains;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.headi.R;
import com.example.headi.db.HeadiDBContract;
import com.example.headi.db.HeadiDBSQLiteHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public class PainsFragment extends Fragment {

    private View view;
    private ListView PainsItems;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Context context = getActivity();
        view = inflater.inflate(R.layout.fragment_pains_medications, container, false);
        PainsItems = (ListView) view.findViewById(R.id.pains_list);
        setHasOptionsMenu(true);

        registerListeners(context);
        readFromDB();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_pains_medications, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_item:  {
                openAddItemDialog();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openAddItemDialog() {
        Context context = requireActivity();

        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.add_new_item_title));

        // set the save layout
        final View saveView = getLayoutInflater().inflate(R.layout.fragment_pains_medication_add_dialog, null);
        builder.setView(saveView);

        // add add button
        builder.setPositiveButton(context.getString(R.string.add_button), (dialog, which) -> {
            saveToDB(saveView);
        });

        // add cancel button
        builder.setNegativeButton(context.getString(R.string.cancel_button), (dialog, which) -> { });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveToDB(View view) {
        Context context = requireActivity();
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();

        EditText mEdit = (EditText) view.findViewById(R.id.pains_add_new_pain);
        values.put(HeadiDBContract.Pains.COLUMN_PAIN, mEdit.getText().toString());
        database.insert(HeadiDBContract.Pains.TABLE_NAME, null, values);

        Toast.makeText(context, context.getString(R.string.new_item_added), Toast.LENGTH_SHORT).show();
        readFromDB();
    }

    private void readFromDB() {
        Context context = getActivity();

        // Attach cursor adapter to the ListView
        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        PainsItems.setAdapter(helper.readPainsFromDB(context));
    }

    private void deleteFromDB(long id) {
        Context context = getActivity();
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getWritableDatabase();

        String selection = HeadiDBContract.Pains._ID + " = ?";
        String[] selectionArgs = {Long.toString(id)};

        database.delete(HeadiDBContract.Pains.TABLE_NAME, selection, selectionArgs);
        readFromDB();
    }

    private void registerListeners(Context context) {
        PainsItems.setOnItemLongClickListener((adapterView, view, position, id) -> {

            new MaterialAlertDialogBuilder(context)
                    .setTitle(context.getString(R.string.action_delete))
                    .setMessage(context.getString(R.string.delete_pains))
                    .setPositiveButton(context.getString(R.string.delete_button),
                            (dialogInterface, i) -> deleteFromDB(id))
                    .setNegativeButton(context.getString(R.string.cancel_button), (dialogInterface, i) -> {
                    })
                    .show();
            return true;
        });

    }
}