package com.pheuture.playlists.utils;

import android.text.InputFilter;
import android.text.Spanned;

public class EditTextInputFilter implements InputFilter {
    private static final String TAG = EditTextInputFilter.class.getSimpleName();

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        return (source.length()!=0 && dest.length()==0) ? String.valueOf(Character.toUpperCase(source.charAt(0))) : source;
    }
}
