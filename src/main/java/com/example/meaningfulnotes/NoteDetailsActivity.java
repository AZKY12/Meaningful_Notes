package com.example.meaningfulnotes;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NoteDetailsActivity extends AppCompatActivity {

    EditText titleEditText, contentEditText;
    ImageButton saveNoteBtn;

    TextView pageTitleTextView, deleteNoteTextViewBtn;
    String title, content, docId;
    boolean isEditMode = false;

    // Retrofit instance for API calls
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.dictionaryapi.dev/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    // DictionaryService instance
    DictionaryService service = retrofit.create(DictionaryService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        titleEditText = findViewById(R.id.notes_title_text);
        contentEditText = findViewById(R.id.notes_content_text);
        saveNoteBtn = findViewById(R.id.save_note_btn);
        pageTitleTextView = findViewById(R.id.page_title);
        deleteNoteTextViewBtn = findViewById(R.id.delete_note_text_view_btn);

        // Receive data
        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");

        if (docId != null && !docId.isEmpty()) {
            isEditMode = true;
        }

        titleEditText.setText(title);
        contentEditText.setText(content);
        if (isEditMode) {
            pageTitleTextView.setText("Edit your note");
            deleteNoteTextViewBtn.setVisibility(View.VISIBLE);
        }

        // Long press listener to fetch dictionary data
        contentEditText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String selectedWord = getSelectedWord(contentEditText);
                if (selectedWord != null && !selectedWord.isEmpty()) {
                    fetchDictionaryData(selectedWord);
                }
                return true; // Consume the long click event
            }
        });

        saveNoteBtn.setOnClickListener( (v)-> saveNote());

        deleteNoteTextViewBtn.setOnClickListener((v)-> deleteNoteFromFirebase() );
    }

    // Method to fetch selected word from EditText
    private String getSelectedWord(EditText editText) {
        int start = Math.max(editText.getSelectionStart(), 0);
        int end = Math.max(editText.getSelectionEnd(), 0);
        if (start == end) return null; // No word selected
        CharSequence selectedText = editText.getText().subSequence(start, end);
        return selectedText.toString().trim();
    }

    // Method to fetch dictionary data from API
    private void fetchDictionaryData(String word) {
        Call<List<DictionaryResponse>> call = service.getDictionaryData(word);
        call.enqueue(new Callback<List<DictionaryResponse>>() {
            @Override
            public void onResponse(Call<List<DictionaryResponse>> call, Response<List<DictionaryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DictionaryResponse> dictionaryResponses = response.body();
                    showDictionaryResults(dictionaryResponses);
                } else {
                    Toast.makeText(NoteDetailsActivity.this, "Failed to fetch dictionary data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DictionaryResponse>> call, Throwable t) {
                Toast.makeText(NoteDetailsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Method to display dictionary results
    private void showDictionaryResults(List<DictionaryResponse> dictionaryResponses) {
        DictionaryDialog.showDictionaryDialog(NoteDetailsActivity.this, dictionaryResponses);
    }

    // Method to save note to Firebase
    void saveNote() {
        String noteTitle = titleEditText.getText().toString();
        String noteContent = contentEditText.getText().toString();
        if (noteTitle == null || noteTitle.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }
        Note note = new Note();
        note.setTitle(noteTitle);
        note.setContent(noteContent);
        note.setTimestamp(Timestamp.now());

        saveNoteToFirebase(note);
    }

    // Method to save note to Firebase Firestore
    void saveNoteToFirebase(Note note){
        DocumentReference documentReference;
        if(isEditMode){
            //update the note
            documentReference = Utility.getCollectionReferenceForNotes().document(docId);
        }else{
            //create new note
            documentReference = Utility.getCollectionReferenceForNotes().document();
        }



        documentReference.set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //note is added
                    Utility.showToast(NoteDetailsActivity.this,"Note added successfully");
                    finish();
                }else{
                    Utility.showToast(NoteDetailsActivity.this,"Failed while adding note");
                }
            }
        });

    }

    // Method to delete note from Firebase Firestore
    void deleteNoteFromFirebase(){
        DocumentReference documentReference;
        documentReference = Utility.getCollectionReferenceForNotes().document(docId);
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //note is deleted
                    Utility.showToast(NoteDetailsActivity.this,"Note deleted successfully");
                    finish();
                }else{
                    Utility.showToast(NoteDetailsActivity.this,"Failed while deleting note");
                }
            }
        });
    }
}
