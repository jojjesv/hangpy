package com.hangpy.hangpy.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hangpy.hangpy.R;

/**
 * Dialog fragment for prompting category input.
 */
public abstract class CategoryInputFragment extends DialogFragment {
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

        return root;
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
}
