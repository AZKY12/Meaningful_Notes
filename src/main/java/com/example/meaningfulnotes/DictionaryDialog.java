package com.example.meaningfulnotes;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class DictionaryDialog {

    public static void showDictionaryDialog(Context context, List<DictionaryResponse> dictionaryResponses) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_dictionary, null);
        dialogBuilder.setView(dialogView);

        TextView textViewWord = dialogView.findViewById(R.id.textViewWord);
        TextView textViewDefinitions = dialogView.findViewById(R.id.textViewDefinitions);

        StringBuilder definitionsBuilder = new StringBuilder();
        for (DictionaryResponse response : dictionaryResponses) {
            textViewWord.setText(response.getWord());

            List<Meaning> meanings = response.getMeanings();
            for (Meaning meaning : meanings) {
                String partOfSpeech = meaning.getPartOfSpeech();
                definitionsBuilder.append(partOfSpeech).append(":\n");

                List<Definition> definitions = meaning.getDefinitions();
                for (Definition definition : definitions) {
                    String def = definition.getDefinition();
                    String example = definition.getExample();
                    if (!TextUtils.isEmpty(def)) {
                        definitionsBuilder.append("- ").append(def).append("\n");
                        if (!TextUtils.isEmpty(example)) {
                            definitionsBuilder.append("  (e.g. ").append(example).append(")").append("\n");
                        }
                    }
                }
            }
        }

        textViewDefinitions.setText(definitionsBuilder.toString());

        dialogBuilder.setPositiveButton("OK", null);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
}
