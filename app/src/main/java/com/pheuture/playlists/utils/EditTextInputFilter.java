package com.pheuture.playlists.utils;

import android.text.InputFilter;
import android.text.Spanned;

public class EditTextInputFilter implements InputFilter {
    private static final String TAG = EditTextInputFilter.class.getSimpleName();

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        StringBuilder characters = new StringBuilder();
        for (int i=0; i<source.length(); i++){
            String ch = String.valueOf(source.charAt(i));
            if (ch.matches("[0-9a-zA-Z \n+×÷=_!@#$%&()-:;,'?.|<>*\"]")) {
                characters.append(source.charAt(i));
            }
        }
        return characters.toString();
    }
}
