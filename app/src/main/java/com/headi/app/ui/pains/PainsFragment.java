package com.headi.app.ui.pains;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.headi.app.R;
import com.headi.app.db.HeadiDBContract;
import com.headi.app.db.HeadiDBSQLiteHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class PainsFragment extends Fragment {

    private ListView PainsItems;
    private View view;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_pains_medications, container, false);
        PainsItems = view.findViewById(R.id.pains_list);

        registerForContextMenu(PainsItems);
        registerListeners();
        readFromDB();
        return view;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_context_edit_delete, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long itemId = info.id;

        if (item.getItemId() == R.id.action_item_edit) {
            openItemUpdateDialog(item);
            return true;
        }

        if (item.getItemId() == R.id.action_item_delete) {
            deleteFromDB(itemId);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void openItemUpdateDialog(MenuItem item) {
        Context context = requireActivity();

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        long id = info.id;

        String oldPain = ((Cursor) PainsItems.getItemAtPosition(position)).getString(1);

        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.title_edit_item));

        // set the save layout
        final View saveView = getLayoutInflater().inflate(R.layout.fragment_pains_medication_add_dialog, null);
        builder.setView(saveView);

        // set the old pain text
        EditText mEdit = saveView.findViewById(R.id.pains_add_new_pain);
        mEdit.setText(oldPain);

        // add save button
        builder.setPositiveButton(context.getString(R.string.save_button), (dialog, which) -> updateDB(id, saveView, oldPain));

        // add cancel button
        builder.setNegativeButton(context.getString(R.string.cancel_button), (dialog, which) -> dialog.dismiss());

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
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
        builder.setPositiveButton(context.getString(R.string.add_button), (dialog, which) -> saveToDB(saveView));

        // add cancel button
        builder.setNegativeButton(context.getString(R.string.cancel_button), (dialog, which) -> { });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateDB(long id, View view, String oldPain) {
        Context context = requireActivity();

        EditText mEdit = view.findViewById(R.id.pains_add_new_pain);
        String newPain = mEdit.getText().toString();

        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();

        // Update pains table
        String selection = HeadiDBContract.Pains._ID + " = ?";
        String[] selectionArgs = {Long.toString(id)};
        values.put(HeadiDBContract.Pains.COLUMN_PAIN, newPain);
        database.update(HeadiDBContract.Pains.TABLE_NAME, values, selection, selectionArgs);

        // Update diary table
        selection = HeadiDBContract.Diary.COLUMN_PAIN + " = ?";
        selectionArgs[0] = oldPain;
        values.put(HeadiDBContract.Diary.COLUMN_PAIN, newPain);
        database.update(HeadiDBContract.Diary.TABLE_NAME, values, selection, selectionArgs);

        Toast.makeText(context, context.getString(R.string.item_saved), Toast.LENGTH_SHORT).show();
        readFromDB();
    }

    private void saveToDB(View view) {
        Context context = requireActivity();
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();

        EditText mEdit = view.findViewById(R.id.pains_add_new_pain);
        values.put(HeadiDBContract.Pains.COLUMN_PAIN, mEdit.getText().toString());
        database.insert(HeadiDBContract.Pains.TABLE_NAME, null, values);

        Toast.makeText(context, context.getString(R.string.new_item_added), Toast.LENGTH_SHORT).show();
        readFromDB();
    }

    private void readFromDB() {
        Context context = getActivity();

        // Attach cursor adapter to the ListView
        HeadiDBSQLiteHelper helper = new HeadiDBSQLiteHelper(context);
        PainsItems.setAdapter(helper.readPainsWithIconFromDB(context));
    }

    private void deleteFromDB(long id) {
        Context context = getActivity();
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getWritableDatabase();

        String selection = HeadiDBContract.Pains._ID + " = ?";
        String[] selectionArgs = {Long.toString(id)};

        database.delete(HeadiDBContract.Pains.TABLE_NAME, selection, selectionArgs);
        readFromDB();
    }

    private void registerListeners() {
        FloatingActionButton fab = view.findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(view -> openAddItemDialog());
    }
}