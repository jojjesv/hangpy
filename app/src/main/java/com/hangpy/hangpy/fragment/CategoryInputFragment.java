package com.hangpy.hangpy.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hangpy.hangpy.R;
import com.hangpy.hangpy.backend.User;

/**
 * Dialog fragment for prompting category input.
 */
public abstract class CategoryInputFragment extends DialogFragment implements User.RequestCallback {
    private ListView suggestionsResult;
    private ArrayAdapter<String> suggestionsAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.input_category, container, false);

        View acceptView = root.findViewById(R.id.accept);
        acceptView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptAndHide(root, v);
            }
        });

        EditText input = (EditText)root.findViewById(android.R.id.inputArea);
        input.addTextChangedListener(createInputTextWatcher());

        suggestionsResult = (ListView)root.findViewById(R.id.suggestions);
        suggestionsResult.setAdapter(suggestionsAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1));
        suggestionsResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onSuggestionClicked((TextView)view);
            }
        });
        suggestionsAdapter.setNotifyOnChange(false);

        return root;
    }

    private void onSuggestionClicked(TextView suggestionView){
        
    }

    /**
     * @return A text watcher which fetches category suggestions on change.
     */
    private TextWatcher createInputTextWatcher(){
       return new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {

           }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {
               fetchSuggestedCategories(s.toString());
           }

           @Override
           public void afterTextChanged(Editable s) {

           }
       };
    }

    /**
     * Called by the accept button. Stores the inputted category and hides the dialog.
     */
    protected void acceptAndHide(View root, View v){
        TextView input = (TextView)root.findViewById(android.R.id.inputArea);
        dismiss();

        String inputText = input.getText().toString();
        handleInputOnDismiss(inputText);
    }

    /**
     * Called once input has finally been obtained.
     * @param input
     */
    protected abstract void handleInputOnDismiss(String input);

    public void fetchSuggestedCategories(String input){
        User.request("out=categories&input=" + User.encode(input), new byte[0], this);
    }

    @Override
    public void onResponse(String response) {
        //  Got suggested categories response
        String[] suggestions = response.split(",");

        //  Each string needs to be decoded
        for (int i = 0, n = suggestions.length; i < n; i++) {
            suggestions[i] = User.decode(suggestions[i]);
        }

        suggestionsAdapter.clear();
        suggestionsAdapter.addAll(suggestions);
        suggestionsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFailed() {
        //  Failed to fetch categories response
    }
}
